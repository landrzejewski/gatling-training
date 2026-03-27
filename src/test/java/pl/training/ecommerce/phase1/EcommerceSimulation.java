package pl.training.ecommerce.phase1;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class EcommerceSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://api-ecomm.gatling.io")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private final ScenarioBuilder scenario = scenario("get products")
            .exec(
                    http("get products")
                            .get("/products")
                            .check(status().is(200))
                            .check(jsonPath("$.products").exists())
                            .check(jsonPath("$.totalPages").is("4"))
            );

    {
        setUp(scenario.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }

}
