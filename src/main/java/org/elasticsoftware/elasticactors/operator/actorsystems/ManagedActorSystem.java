package org.elasticsoftware.elasticactors.operator.actorsystems;

import org.elasticsoftware.elasticactors.operator.customresources.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.stream.Collectors;

public abstract class ManagedActorSystem {
    protected static final Logger logger = LoggerFactory.getLogger(ManagedActorSystem.class);
    private final ActorSystem resource;
    private final String uuid;

    public ManagedActorSystem(ActorSystem resource, String uuid) {
        this.resource = resource;
        this.uuid = uuid;
    }

    public String getName() {
        return resource.getMetadata().getName();
    }

    public String getUuid() {
        return uuid;
    }

    public ActorSystem getResource() {
        return resource;
    }

    public abstract void ensureInfrastructure();

    public abstract void disposeInfrastructure();

    protected String generatePassword() {
        return new Random().ints(20, 33, 123).mapToObj(i -> String.valueOf((char)i)).collect(Collectors.joining());
    }
}
