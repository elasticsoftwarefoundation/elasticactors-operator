package org.elasticsoftware.elasticactors.operator.customresources;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableActorSystem extends CustomResourceDoneable<ActorSystem> {
    public DoneableActorSystem(ActorSystem resource, Function<ActorSystem, ActorSystem> function) {
        super(resource, function);
    }
}
