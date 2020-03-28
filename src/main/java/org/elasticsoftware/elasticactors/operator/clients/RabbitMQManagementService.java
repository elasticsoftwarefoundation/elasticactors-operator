package org.elasticsoftware.elasticactors.operator.clients;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.ws.rs.*;

@Path("/api")
@RegisterRestClient(configKey="rabbitmq-api")
@ClientHeaderParam(name = "Accept", value = "application/json")
public interface RabbitMQManagementService {
    @GET
    @Path("/vhost/{name}")
    @Produces("application/json")
    String getVhost(@HeaderParam("Authorization") String authorizationHeader, @PathParam("name") String name);

    @PUT
    @Path("/vhost/{name}")
    @Produces("application/json")
    String createOrUpdateVhost(@HeaderParam("Authorization") String authorizationHeader, @PathParam("name") String name);

    @DELETE
    @Path("/vhost/{name}")
    @Produces("application/json")
    String deleteVhost(@HeaderParam("Authorization") String authorizationHeader, @PathParam("name") String name);

    @GET
    @Path("/whoami")
    @Produces("application/json")
    String whoAmI(@HeaderParam("Authorization") String authorizationHeader);
}
