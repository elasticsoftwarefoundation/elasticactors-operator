package org.elasticsoftware.elasticactors.operator.clients;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
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
        try {
            return rabbitMQAdminClient.getVhost(authorizationHeader, name);
        } catch (WebApplicationException e) {
            if(e.getResponse().getStatus() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    public String createVhost(String name) {
        String vhost = rabbitMQAdminClient.createOrUpdateVhost(authorizationHeader, name);
        rabbitMQAdminClient.createPolicy(authorizationHeader, name, "ha-shards", "{\"pattern\":\"^shards-\", \"definition\": {\"ha-mode\":\"all\",\"ha-sync-mode\":\"automatic\"}}");
        return vhost;
    }

    public String deleteVhost(String name) {
        return rabbitMQAdminClient.deleteVhost(authorizationHeader, name);
    }

    public void createExchange(String vhost, String exchangeName, ExchangeParameters parameters)  {
        rabbitMQAdminClient.createExchange(authorizationHeader, vhost, exchangeName, parameters);
    }

    public void createQueue(String vhost, String queueName, QueueParameters parameters) {
        rabbitMQAdminClient.createQueue(authorizationHeader, vhost, queueName, parameters);
        rabbitMQAdminClient.bindQueue(authorizationHeader, vhost, "shards", queueName, new BindingParameters(queueName));
    }

    public void createUser(String vhost, String username, String password) {
        rabbitMQAdminClient.createUser(authorizationHeader, username, new UserParameters(password));
        rabbitMQAdminClient.assignPermissions(authorizationHeader, vhost, username, "{\"configure\":\"\",\"write\":\".*\",\"read\":\".*\"}");
    }

    public void deleteUser(String username) {
        rabbitMQAdminClient.deleteUser(authorizationHeader, username);
    }

}
