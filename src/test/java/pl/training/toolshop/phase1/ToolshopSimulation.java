package pl.training.toolshop.phase1;

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
                    .contentTypeHeader("application/json");

    private final ScenarioBuilder scenario =
            scenario("get products")
                    .exec(
                            http("get products")
                                    .get("/products")
                                    .check(status().is(200))
                                    .check(jsonPath("$.data").exists())
                                    .check(jsonPath("$.total").ofInt().is(50))
                                    .check(jsonPath("$.per_page").ofInt().is(9)));

    {
        setUp(scenario.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }

}
