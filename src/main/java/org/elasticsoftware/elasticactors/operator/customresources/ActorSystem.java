package org.elasticsoftware.elasticactors.operator.customresources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.client.CustomResource;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class ActorSystem extends CustomResource {
    private ActorSystemSpec spec;

    public ActorSystemSpec getSpec() {
        return spec;
    }

    public void setSpec(ActorSystemSpec spec) {
        this.spec = spec;
    }
}
