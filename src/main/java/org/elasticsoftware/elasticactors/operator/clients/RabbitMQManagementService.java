package org.elasticsoftware.elasticactors.operator.clients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.ws.rs.*;

@Path("/api")
@RegisterRestClient(configKey="rabbitmq-api")
@Consumes("application/json")
public interface RabbitMQManagementService {
    @GET
    @Path("/vhosts/{name}")
    @Produces("application/json")
    String getVhost(@HeaderParam("Authorization") String authorizationHeader, @PathParam("name") String name);

    @PUT
    @Path("/vhosts/{name}")
    @Produces("application/json")
    String createOrUpdateVhost(@HeaderParam("Authorization") String authorizationHeader, @PathParam("name") String name);

    @DELETE
    @Path("/vhosts/{name}")
    @Produces("application/json")
    String deleteVhost(@HeaderParam("Authorization") String authorizationHeader, @PathParam("name") String name);

    @GET
    @Path("/whoami")
    @Produces("application/json")
    String whoAmI(@HeaderParam("Authorization") String authorizationHeader);

    @PUT
    @Path("/exchanges/{vhost}/{name}")
    @Produces("application/json")
    String createExchange(@HeaderParam("Authorization") String authorizationHeader,
                          @PathParam("vhost") String vhostName,
                          @PathParam("name") String exchangeName,
                          ExchangeParameters exchangeParameters);
    @PUT
    @Path("/queues/{vhost}/{name}")
    @Produces("application/json")
    String createQueue(@HeaderParam("Authorization") String authorizationHeader,
                       @PathParam("vhost") String vhostName,
                       @PathParam("name") String queueName,
                       QueueParameters parameters);

    @POST
    @Path("/bindings/{vhost}/e/{exchange}/q/{queue}")
    @Produces("application/json")
    String bindQueue(@HeaderParam("Authorization") String authorizationHeader,
                     @PathParam("vhost") String vhostName,
                     @PathParam("exchange") String exchangeName,
                     @PathParam("queue") String queueName,
                     BindingParameters parameters);

    @PUT
    @Path("/policies/{vhost}/{name}")
    @Produces("application/json")
    String createPolicy(@HeaderParam("Authorization") String authorizationHeader,
                        @PathParam("vhost") String vhostName,
                        @PathParam("name") String policyName,
                        String parameters);
    @PUT
    @Path("/users/{name}")
    @Produces("application/json")
    String createUser(@HeaderParam("Authorization") String authorizationHeader,
                      @PathParam("name") String userName,
                      UserParameters parameters);

    @DELETE
    @Path("/users/{name}")
    @Produces("application/json")
    String deleteUser(@HeaderParam("Authorization") String authorizationHeader,
                      @PathParam("name") String userName);

    @PUT
    @Path("/permissions/{vhost}/{user}")
    @Produces("application/json")
    String assignPermissions(@HeaderParam("Authorization") String authorizationHeader,
                             @PathParam("vhost") String vhostName,
                             @PathParam("user") String userName,
                             String permissions);
}
