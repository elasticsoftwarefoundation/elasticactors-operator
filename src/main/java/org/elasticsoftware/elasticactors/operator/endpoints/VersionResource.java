package org.elasticsoftware.elasticactors.operator.endpoints;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@ApplicationScoped
@Path("/version")
public class VersionResource {

    @ConfigProperty(name = "elasticactors.operator.version")
    String version;

    public String getVersion() {
        return version;
    }

    @GET
    @Produces(TEXT_PLAIN)
    public String version() {
        return version;
    }

}
