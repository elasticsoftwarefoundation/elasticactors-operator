package org.elasticsoftware.elasticactors.operator.cache;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.elasticsoftware.elasticactors.operator.FatalErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.elasticsoftware.elasticactors.operator.cache.ExceptionHandlerWrapper.exitOnError;

class ListThenWatchOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListThenWatchOperation.class);

    /**
     * Note that the callback is called in the executor thread.
     */
    static <T extends HasMetadata, L extends KubernetesResourceList<T>> Watch run(ExecutorService executor, ResourceMap<T> map,
                                                                                  ListerWatcher<T, L> op, FatalErrorHandler fatalErrorHandler,
                                                                                  BiConsumer<Watcher.Action, String> onEventCallback,
                                                                                  Consumer<Exception> onErrorCallback) {

        // list
        op.list()
                .getItems()
                .forEach(resource -> {
                            map.putIfNewer(resource.getMetadata().getUid(), resource);
                            String uid = resource.getMetadata().getUid();
                            executor.execute(() -> exitOnError(onEventCallback, fatalErrorHandler).accept(Watcher.Action.ADDED, uid));
                        }
                );

        // watch
        return op.watch(new Watcher<T>() {
            @Override
            public void eventReceived(Action action, T resource) {
                try {
                    String uid = resource.getMetadata().getUid();
                    boolean updated = false;
                    switch (action) {
                        case ADDED:
                            // fall through
                        case MODIFIED:
                            updated = map.putIfNewer(uid, resource);
                            break;
                        case DELETED:
                            updated = map.remove(uid);
                            break;
                        default:
                            LOGGER.error("Received unexpected " + action + " event for " + resource.getMetadata().getName());
                            fatalErrorHandler.systemExit(-1);
                    }
                    if (updated) {
                        executor.execute(() -> exitOnError(onEventCallback, fatalErrorHandler).accept(action, uid));
                    }
                } catch (Exception e) {
                    // This happens if executor.execute() throws a RejectedExecutionException, so it doesn't make sense to
                    // schedule an onErrorCallback here. Just terminate the JVM and have Kubernetes restart the Pod.
                    LOGGER.error(e.getMessage(), e);
                    fatalErrorHandler.systemExit(-1);
                }
            }

            @Override
            public void onClose(WatcherException cause) {
                if (cause != null) { // null means normal close, i.e. Watch.close() was called.
                    try {
                        // We call the onErrorCallback to allow for cleanup, but after that we terminate the JVM
                        // and have Kubernetes restart the Pod, because there is no way to recover from this.
                        executor.execute(() -> exitOnError(onErrorCallback, fatalErrorHandler)
                                .andThen(c -> fatalErrorHandler.systemExit(-1))
                                .accept(cause));
                    } catch (Exception e) {
                        // This happens if executor.execute() throws a RejectedExecutionException, so it doesn't make sense to
                        // schedule an onErrorCallback here. Just terminate the JVM and have Kubernetes restart the Pod.
                        LOGGER.error(e.getMessage(), e);
                        fatalErrorHandler.systemExit(-1);
                    }
                }
            }
        });
    }
}