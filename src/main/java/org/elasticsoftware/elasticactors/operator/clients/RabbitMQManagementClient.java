package org.elasticsoftware.elasticactors.operator.clients;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Base64;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

@ApplicationScoped
public class RabbitMQManagementClient {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQManagementClient.class);
    @ConfigProperty(name = "rabbitmq.username", defaultValue = "guest")
    String username;
    @ConfigProperty(name = "rabbitmq.password", defaultValue = "guest")
    String password;
    // avoid thead visibility issues
    volatile String authorizationHeader;
    @Inject
    @RestClient
    RabbitMQManagementService rabbitMQAdminClient;

    void onStartup(@Observes @Priority(APPLICATION - 1) StartupEvent _ev) {
        authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((username+":"+password).getBytes());
    }

    public String whoAmI() {
        return rabbitMQAdminClient.whoAmI(authorizationHeader);
    }

    public String getVhost(String name) {
        return rabbitMQAdminClient.getVhost(authorizationHeader, name);
    }

    public String createVhost(String name) {
        return rabbitMQAdminClient.createOrUpdateVhost(authorizationHeader, name);
    }

    public String deleteVhost(String name) {
        return rabbitMQAdminClient.createOrUpdateVhost(authorizationHeader, name);
    }
}
