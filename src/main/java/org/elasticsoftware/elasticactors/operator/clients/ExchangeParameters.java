package org.elasticsoftware.elasticactors.operator.clients;

public final class ExchangeParameters {
    private final String type;
    private final Boolean durable;

    public ExchangeParameters() {
        this("direct", true);
    }

    public ExchangeParameters(String type, Boolean durable) {
        this.type = type;
        this.durable = durable;
    }

    public String getType() {
        return type;
    }

    public Boolean getDurable() {
        return durable;
    }
}
