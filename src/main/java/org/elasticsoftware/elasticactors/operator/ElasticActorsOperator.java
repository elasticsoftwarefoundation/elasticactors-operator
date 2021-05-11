package org.elasticsoftware.elasticactors.operator;

import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.elasticsoftware.elasticactors.operator.actorsystems.ManagedActorSystem;
import org.elasticsoftware.elasticactors.operator.actorsystems.RabbitMQCassandraActorSystem;
import org.elasticsoftware.elasticactors.operator.clients.RabbitMQManagementClient;
import org.elasticsoftware.elasticactors.operator.customresources.ActorSystem;
import org.elasticsoftware.elasticactors.operator.customresources.ActorSystemList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class ElasticActorsOperator {
    private static Logger logger = LoggerFactory.getLogger(ElasticActorsOperator.class);
    @Inject
    MixedOperation<ActorSystem, ActorSystemList, Resource<ActorSystem>> actorSystemClient;
    @Inject
    RabbitMQManagementClient rabbitMQAdminClient;
    @ConfigProperty(name = "elasticactors.operator.version")
    String version;

    private final ConcurrentMap<String, ManagedActorSystem> managedActorSystems = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setDaemon(true);
        thread.setName("k8s-handler");
        thread.setUncaughtExceptionHandler((t, e) -> {
            logger.error("UncaughtException in thread {}", t.getName() , e);
        });
        return thread;
    });

    void onStartup(@Observes StartupEvent _ev) {
        logger.info("Starting up");
        logger.info("rabbitMQAdminClient.whoAmI {}", rabbitMQAdminClient.whoAmI());
        watchActorSystems(true);
    }

    private void handleActorSystemEvent(Watcher.Action action, ActorSystem actorSystemResource) {
        logger.info("Watcher.Action {} for ActorSystem Custom Resource with uuid {}",
                action.name(),
                actorSystemResource.getMetadata().getUid());
        logger.info("ActorSystem[name={}, type={}, shards={}, nodes={}]",
                actorSystemResource.getMetadata().getName(),
                actorSystemResource.getSpec().getType().name(),
                actorSystemResource.getSpec().getShards(),
                actorSystemResource.getSpec().getNodes());
        logger.info("resourceVersion {}", actorSystemResource.getMetadata().getResourceVersion());

        switch (action) {
            case ADDED:
                // TODO: need to have a factory in between that creates the right type
                ManagedActorSystem managedActorSystem = new RabbitMQCassandraActorSystem(
                        actorSystemResource,
                        actorSystemResource.getMetadata().getUid(),
                        rabbitMQAdminClient);
                managedActorSystems.putIfAbsent(managedActorSystem.getUuid(), managedActorSystem);
                managedActorSystem.ensureInfrastructure();
                break;
            case MODIFIED:
                // TODO: need to support modification
                break;
            case DELETED:
                managedActorSystems.remove(actorSystemResource.getMetadata().getUid()).disposeInfrastructure();
                break;
        }
    }

    private void watchActorSystems(boolean start) {
        ActorSystemList actorSystemList = actorSystemClient.inAnyNamespace().list();
        if(start) {
            // we need to generate initial added events
            actorSystemList.getItems().forEach(actorSystem -> actorSystemWatcher.eventReceived(Watcher.Action.ADDED, actorSystem));
        }
        logger.info("Watching for ActorSystem CustomResources in every namespace: resourceVersion={}",actorSystemList.getMetadata().getResourceVersion());
        actorSystemClient.inAnyNamespace().withResourceVersion(actorSystemList.getMetadata().getResourceVersion()).watch(actorSystemWatcher);
    }

    private final Watcher<ActorSystem> actorSystemWatcher = new Watcher<>() {
        @Override
        public void eventReceived(Action action, ActorSystem resource) {
            executor.submit(() -> handleActorSystemEvent(action, resource));
        }

        @Override
        public void onClose(WatcherException cause) {
            if(cause != null) {
                watchActorSystems(false);
            }
        }
    };
}
