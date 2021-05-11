package org.elasticsoftware.elasticactors.operator.clients;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BindingParameters {
    private final String routingKey;

    public BindingParameters(String routingKey) {
        this.routingKey = routingKey;
    }

    @JsonProperty("routing_key")
    public String getRoutingKey() {
        return routingKey;
    }
}
