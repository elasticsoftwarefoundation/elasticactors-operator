package org.elasticsoftware.elasticactors.operator;

import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.elasticsoftware.elasticactors.operator.actorsystems.ManagedActorSystem;
import org.elasticsoftware.elasticactors.operator.cache.Cache;
import org.elasticsoftware.elasticactors.operator.cache.CacheEvent;
import org.elasticsoftware.elasticactors.operator.cache.CacheService;
import org.elasticsoftware.elasticactors.operator.clients.RabbitMQManagementClient;
import org.elasticsoftware.elasticactors.operator.clients.RabbitMQManagementService;
import org.elasticsoftware.elasticactors.operator.customresources.ActorSystem;
import org.elasticsoftware.elasticactors.operator.customresources.ActorSystemList;
import org.elasticsoftware.elasticactors.operator.customresources.DoneableActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class ElasticActorsOperator {
    private static Logger logger = LoggerFactory.getLogger(ElasticActorsOperator.class);
    @Inject
    MixedOperation<ActorSystem, ActorSystemList, DoneableActorSystem, Resource<ActorSystem, DoneableActorSystem>> actorSystemClient;
    @Inject
    CacheService cacheService;
    @Inject
    RabbitMQManagementClient rabbitMQAdminClient;
    @ConfigProperty(name = "elasticactors.operator.version")
    String version;

    private final ConcurrentMap<String, ManagedActorSystem> managedActorSystems = new ConcurrentHashMap<>();

    private Cache<ActorSystem, ActorSystemList> actorSystemCache;

    void onStartup(@Observes StartupEvent _ev) {
        logger.info("Starting up");
        logger.info("rabbitMQAdminClient.whoAmI {}",rabbitMQAdminClient.whoAmI());
        // create the caches
        actorSystemCache = cacheService.newCache(ActorSystem.class, ActorSystemList.class);
        // start listening to updates for the ActorSystem CRD
        actorSystemCache.listThenWatch(actorSystemClient.inAnyNamespace())
                .subscribe(this::handleActorSystemEvent);
    }

    private void handleActorSystemEvent(CacheEvent cacheEvent) {
        logger.info("CacheEvent {} for ActorSystem Custom Resource with uuid {}",
                cacheEvent.getAction().name(),
                cacheEvent.getUid());
        actorSystemCache.get(cacheEvent.getUid()).ifPresent(actorSystem -> {
            logger.info("ActorSystem[name={}, type={}, shards={}, nodes={}]",
                    actorSystem.getMetadata().getName(),
                    actorSystem.getSpec().getType().name(),
                    actorSystem.getSpec().getShards(),
                    actorSystem.getSpec().getNodes());
            logger.info("resourceVersion {}", actorSystem.getMetadata().getResourceVersion());
            try {
                switch (cacheEvent.getAction()) {
                    case ADDED:
                    case MODIFIED:
                        // need to check whether we already have created the exchanges and queues
                        if (rabbitMQAdminClient.getVhost(actorSystem.getMetadata().getName()) == null) {
                            logger.info("Creating RabbitMQ vhost for ActorSytem {}", actorSystem.getMetadata().getName());
                            rabbitMQAdminClient.createVhost(actorSystem.getMetadata().getName());
                        }
                        break;
                    case DELETED:
                        if (rabbitMQAdminClient.getVhost(actorSystem.getMetadata().getName()) != null) {
                            logger.info("Deleting RabbitMQ vhost for ActorSytem {}", actorSystem.getMetadata().getName());
                            rabbitMQAdminClient.deleteVhost(actorSystem.getMetadata().getName());
                        }
                        break;
                }
            } catch(Exception e) {
                logger.error("Unhandled Exception", e);
            }
        });

    }
}
