package pl.training.ecommerce.phase1;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class EcommerceSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://api-ecomm.gatling.io")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private final ScenarioBuilder scenario = scenario("Get products")
            .exec(
                    http("getProducts")
                            .get("/products")
                            .check(status().is(200))
                            .check(jsonPath("$.products").exists())
                            .check(jsonPath("$.totalPages").is("4"))
            );

    {
        setUp(scenario.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }

}
