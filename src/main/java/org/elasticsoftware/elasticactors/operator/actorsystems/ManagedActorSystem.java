package org.elasticsoftware.elasticactors.operator.actorsystems;

public abstract class ManagedActorSystem {
    private String name;
    private String uuid;

    public ManagedActorSystem(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public abstract void ensureInfrastructure();
}
