package org.elasticsoftware.elasticactors.operator.cache;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ListerWatcher can list() and watch().
 */
public class ListerWatcher<T extends HasMetadata, L extends KubernetesResourceList<T>> {
    private static final Logger logger = LoggerFactory.getLogger(ListerWatcher.class);
    private final FilterWatchListDeletable<T, L> op;
    private volatile String resourceVersion;

    ListerWatcher(FilterWatchListDeletable<T, L> op) {
        this.op = op;
    }

    public KubernetesResourceList<T> list() {
        KubernetesResourceList<T> resourceList = op.list();
        resourceVersion = resourceList.getMetadata().getResourceVersion();
        return resourceList;
    }

    public Watch watch(Watcher<T> watcher) {
        logger.info("Starting Watch on {} with resourceVersion {}", op.toString(), resourceVersion);
        return op.withResourceVersion(resourceVersion).watch(watcher);
    }
}