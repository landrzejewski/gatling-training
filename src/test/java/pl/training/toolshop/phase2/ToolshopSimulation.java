package pl.training.toolshop.phase2;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ToolshopSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol =
            http.baseUrl("https://api.practicesoftwaretesting.com")
                    .acceptHeader("application/json")
                    .contentTypeHeader("application/json")
                    .userAgentHeader("Mozilla/5.0");

    private final ScenarioBuilder scenario =
            scenario("get products")
                    .exec(
                            http("get products - page 1")
                                    .get("/products?page=1")
                                    .check(status().is(200))
                                    .check(jsonPath("$.last_page").ofInt().is(6))
                                    .check(jsonPath("$.total").ofInt().is(50)))
                    .pause(1, 3)
                    .exec(
                            http("get products - page 2")
                                    .get("/products?page=2")
                                    .check(status().is(200))
                                    .check(jsonPath("$.data").exists()))
                    .pause(1, 3)
                    .exec(
                            http("get categories")
                                    .get("/categories")
                                    .check(status().is(200))
                                    .check(jsonPath("$").exists()))
                    .pause(1, 3)
                    .exec(
                            http("get brands")
                                    .get("/brands")
                                    .check(status().is(200))
                                    .check(jsonPath("$").exists()));

    {
        setUp(scenario.injectOpen(rampUsers(3).during(5)))
                .protocols(httpProtocol)
                .assertions(
                        global().failedRequests().percent().lt(1.0),
                        global().responseTime().percentile(95.0).lt(3000)
                );
    }

}
