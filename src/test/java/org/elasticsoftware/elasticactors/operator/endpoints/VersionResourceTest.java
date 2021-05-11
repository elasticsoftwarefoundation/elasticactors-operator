package org.elasticsoftware.elasticactors.operator.endpoints;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

//@QuarkusTest
public class VersionResourceTest {
    //@Test
    public void testVersionEndpoint() {
        given()
                .when().get("/version")
                .then()
                .statusCode(200)
                .log();
    }
}
