package org.elasticsoftware.elasticactors.operator.customresources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class ActorSystemSpec {
    private Integer shards;
    private Integer nodes;
    private Type type;

    public Integer getShards() {
        return shards;
    }

    public void setShards(Integer shards) {
        this.shards = shards;
    }

    public Integer getNodes() {
        return nodes;
    }

    public void setNodes(Integer nodes) {
        this.nodes = nodes;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        RABBITMQ_CASSANDRA, KAFKA
    }
}
