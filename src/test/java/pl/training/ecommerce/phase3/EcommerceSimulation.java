package pl.training.ecommerce.phase3;

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

    private final ScenarioBuilder scenario = scenario("session management")
            .exec(
                    http("set session value")
                            .get("/session")
                            .check(status().is(200))
                            .check(jsonPath("$.sessionId").saveAs("sessionId"))
            )
            .exec(session -> {
                System.out.println("### sessionId: " + session.get("sessionId"));
                return session;
            })
            .exec(
                    http("get products")
                            .get("/products")
                            .check(status().is(200))
                            .check(jsonPath("$.products[0].id").saveAs("productId"))
                            .check(jsonPath("$.products[0].name").saveAs("productName"))
            )
            .pause(Duration.ofSeconds(1))
            .exec(
                    http("get product with id #{productId}")
                            .get("/products/#{productId}")
                            .check(status().is(200))
                            .check(jsonPath("$.name").isEL("#{productName}"))
                            .check(jsonPath("$.price").saveAs("productPrice"))
            )
            .pause(1, 2)
            .exec(
                    http("add to cart")
                            .post("/cart")
                            .body(StringBody("""
                                    {
                                        "sessionId": "#{sessionId}",
                                        "cart": [{"productId": #{productId}, "quantity": 1}]
                                    }
                                    """))
                            .check(status().is(200))
                            .check(jsonPath("$.message").is("Cart updated"))
            );

    {
        setUp(scenario.injectOpen(atOnceUsers(1)))
                .protocols(httpProtocol);
    }

}
