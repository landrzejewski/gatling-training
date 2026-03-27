package pl.training.toolshop.phase4;

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

public class ToolshopSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol =
            http.baseUrl("https://api.practicesoftwaretesting.com")
                    .acceptHeader("application/json")
                    .contentTypeHeader("application/json");

    private final FeederBuilder<String> productFeeder = csv("data/toolshop-products.csv").circular();

    private final FeederBuilder<Object> searchFeeder = jsonFile("data/toolshop-search-terms.json").random();

    private final Iterator<Map<String, Object>> customFeeder =
            Stream.generate(
                            () -> {
                                int quantity = ThreadLocalRandom.current().nextInt(1, 4);
                                return Map.<String, Object>of("quantity", quantity);
                            })
                    .iterator();

    private final ScenarioBuilder scenario = scenario("feeders")
            .exec(
                    http("set session value")
                            .post("/carts")
                            .check(status().is(201))
                            .check(jsonPath("$.id").saveAs("cartId")))
            .exec(
                    session -> {
                        System.out.println("Koszyk ULID: " + session.getString("cartId"));
                        return session;
                    })
            .pause(1)
            .exec(
                    http("get products")
                            .get("/products")
                            .check(status().is(200))
                            .check(jsonPath("$.data[0].id").saveAs("productId"))
                            .check(jsonPath("$.data[0].name").saveAs("productName")))
            .exec(
                    session -> {
                        System.out.println(
                                "Dynamiczny produkt: "
                                        + session.getString("productName")
                                        + " (ULID: "
                                        + session.getString("productId")
                                        + ")");
                        return session;
                    })
            .pause(1)
            .exec(
                    http("get product details")
                            .get("/products/#{productId}")
                            .check(status().is(200))
                            .check(jsonPath("$.name").exists()))
            .pause(1)
            .feed(searchFeeder)
            .exec(
                    http("Search: #{searchTerm}")
                            .get("/products/search?q=#{searchTerm}")
                            .check(status().is(200))
                            .check(jsonPath("$.data").exists()))
            .pause(1)
            .repeat(3)
            .on(
                    feed(customFeeder)
                            .exec(
                                    http("add to cart: #{productName}")
                                            .post("/carts/#{cartId}")
                                            .body(ElFileBody("bodies/toolshop-cart-item.json"))
                                            .check(status().is(200))
                                            .check(jsonPath("$.result").is("item added or updated")))
                            .pause(1));

    {
        setUp(scenario.injectOpen(rampUsers(3).during(5)))
                .protocols(httpProtocol)
                .assertions(global().failedRequests().percent().lt(5.0));
    }

}
