package org.elasticsoftware.elasticactors.operator.actorsystems;

import org.elasticsoftware.elasticactors.operator.clients.ExchangeParameters;
import org.elasticsoftware.elasticactors.operator.clients.QueueParameters;
import org.elasticsoftware.elasticactors.operator.clients.RabbitMQManagementClient;
import org.elasticsoftware.elasticactors.operator.customresources.ActorSystem;

public class RabbitMQCassandraActorSystem extends ManagedActorSystem {
    private final RabbitMQManagementClient rabbitMQManagementClient;

    public RabbitMQCassandraActorSystem(ActorSystem resource,
                                        String uuid,
                                        RabbitMQManagementClient rabbitMQManagementClient) {
        super(resource, uuid);
        this.rabbitMQManagementClient = rabbitMQManagementClient;
    }

    @Override
    public void ensureInfrastructure() {
        // make sure we have all the rabbitmq objects
        // need to check whether we already have created the exchanges and queues
        logger.info("{}[name:{}] {}", getClass().getSimpleName(), getName(), rabbitMQManagementClient.getVhost(getName()));
        if (rabbitMQManagementClient.getVhost(getName()) == null) {
            logger.info("{}[name:{}] Creating RabbitMQ vhost", getClass().getSimpleName(), getName());
            // vhost
            rabbitMQManagementClient.createVhost(getName());
            // TODO: password must be saved in secret
            rabbitMQManagementClient.createUser(getName(), getName(), generatePassword());
            // shards exchange
            rabbitMQManagementClient.createExchange(getName(), "shards", new ExchangeParameters("direct", true));
            // create queues
            QueueParameters parameters = new QueueParameters();
            logger.info("Going to create {} queues", getResource().getSpec().getShards());
            for (int i = 0; i < getResource().getSpec().getShards(); i++) {
                logger.info("Creating queue {}", String.format("shard-%d",i));
                rabbitMQManagementClient.createQueue(getName(), String.format("shard-%d",i), parameters);
            }

        }
    }

    @Override
    public void disposeInfrastructure() {
        if (rabbitMQManagementClient.getVhost(getName()) != null) {
            logger.info("{}[name:{}] Deleting RabbitMQ vhost", getClass().getSimpleName(),getName());
            rabbitMQManagementClient.deleteVhost(getName());
            rabbitMQManagementClient.deleteUser(getName());
        }
    }
}
