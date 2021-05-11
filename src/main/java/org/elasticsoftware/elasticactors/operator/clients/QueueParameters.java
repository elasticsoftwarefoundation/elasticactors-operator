package org.elasticsoftware.elasticactors.operator.clients;

public class QueueParameters {
    private final Boolean durable;

    public QueueParameters() {
        this(true);
    }

    public QueueParameters(Boolean durable) {
        this.durable = durable;
    }

    public Boolean getDurable() {
        return durable;
    }
}
