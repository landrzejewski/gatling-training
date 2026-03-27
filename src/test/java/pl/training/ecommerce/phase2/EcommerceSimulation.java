package pl.training.ecommerce.phase2;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class EcommerceSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://api-ecomm.gatling.io")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Mozilla/5.0");

    private final ScenarioBuilder scenario = scenario("Get products")
            .exec(
                    http("getProducts")
                            .get("/products")
                            .check(status().is(200))
                            .check(jsonPath("$.products").exists())
                            .check(jsonPath("$.products[0].id").ofInt().gte(0))
                            .check(jsonPath("$.totalPages").ofInt().is(4))
            )
            .pause(1, 3)
            .exec(
                    http("getProducts page1")
                            .get("/products?page=1")
                            .check(status().is(200))
                            .check(jsonPath("$.products").exists())
            )
            .pause(2)
            .exec(
                    http("getProduct details")
                            .get("/products/0")
                            .check(status().is(200))
                            .check(jsonPath("$.name").exists())
                            .check(jsonPath("$.price").ofDouble().gt(0.0))
            )
            .pause(Duration.ofMillis(1_000))
            .exec(
                    http("getProduct by search")
                            .get("/products?search=shirt")
                            .check(status().is(200))
                            .check(jsonPath("$.products").exists())
            );

    {
        setUp(scenario.injectOpen(rampUsers(3).during(5)))
                .protocols(httpProtocol)
                .assertions(
                        global().failedRequests().percent().lt(1.0),
                        global().responseTime().percentile(95.0).lt(3_000)
                );
    }

}
