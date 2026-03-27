package pl.training.ecommerce.phase4;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class EcommerceSimulation extends Simulation {

    private final FeederBuilder<String> productFeeder = csv("data/ecomm-products.csv").circular();

    private final FeederBuilder<Object> searchFeeder = jsonFile("data/ecomm-search-terms.json").random();

    private final Iterator<Map<String, Object>> customFeeder = Stream.generate(() -> {
                int productId = ThreadLocalRandom.current().nextInt(32);
                int quantity = ThreadLocalRandom.current().nextInt(1, 8);
                return Map.<String, Object>of("randomProductId", productId, "randomQuantity", quantity);
            })
            .iterator();

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://api-ecomm.gatling.io")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Mozilla/5.0");

    private final ScenarioBuilder scenario = scenario("feeders")
            .exec(
                    http("set session value")
                            .get("/session")
                            .check(status().is(200))
                            .check(jsonPath("$.sessionId").saveAs("sessionId"))
            )
            .pause(1)
            .feed(searchFeeder)
            .exec(
                    http("search product by #{searchTerm}")
                            .get("/products?search=#{searchTerm}")
                            .check(status().is(200))
                            .check(jsonPath("$.products").exists())
            )
            .pause(1)
            .repeat(3)
            .on(
                    feed(customFeeder)
                            .exec(
                                    http("get product by id:  #{randomProductId}")
                                            .get("/products/#{randomProductId}")
                                            .check(status().is(200))
                            )
            )
            .feed(customFeeder)
            .exec(
                    http("add to cart")
                            .post("/cart")
                            .body(ElFileBody("bodies/ecomm-cart.json"))
                            .check(status().is(200))
                            .check(jsonPath("$.message").is("Cart updated"))
            );

    {
        setUp(scenario.injectOpen(atOnceUsers(1)))
                .protocols(httpProtocol);
    }

}
