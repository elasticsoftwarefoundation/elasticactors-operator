package org.elasticsoftware.elasticactors.operator.endpoints;

import org.eclipse.microprofile.health.*;

import javax.enterprise.context.ApplicationScoped;

@Readiness
@ApplicationScoped
public class HealthCheckResource implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder b = HealthCheckResponse.named("elasticactors-operator");
        return b.up().build();
    }
}
