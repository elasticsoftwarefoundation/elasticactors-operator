package org.elasticsoftware.elasticactors.operator.env;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@ApplicationScoped
public class EnvironmentProducer {
    @Produces
    @Singleton
    Environment getEnvironment() {
        return Environment.fromMap(System.getenv());
    }
}
