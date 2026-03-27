package pl.training.toolshop.phase5;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ToolshopSimulation extends Simulation {

    private static final String TEST_TYPE = System.getProperty("TEST_TYPE", "INSTANT");
    private static final int USERS = Integer.parseInt(System.getProperty("USERS", "3"));
    private static final Duration DURATION = Duration.ofSeconds(Integer.parseInt(System.getProperty("DURATION", "30")));
    private static final int MAX_RPS = Integer.parseInt(System.getProperty("MAX_RPS", "10"));

    private final HttpProtocolBuilder httpProtocol =
            http.baseUrl("https://api.practicesoftwaretesting.com")
                    .acceptHeader("application/json")
                    .contentTypeHeader("application/json");

    private final ScenarioBuilder scenario = scenario("profiles")
            .exec(
                    http("get products")
                            .get("/products")
                            .check(status().is(200))
                            .check(jsonPath("$.data[0].id").saveAs("productId")))
            .pause(1, 2)
            .exec(http("Kategorie").get("/categories").check(status().is(200)))
            .pause(1, 2)
            .exec(http("Szczegoly produktu").get("/products/#{productId}").check(status().is(200)));

    @Override
    public void before() {
        System.out.println("=== START TESTU === Profil: " + TEST_TYPE + ", Uzytkownicy: " + USERS);
    }

    @Override
    public void after() {
        System.out.println("=== KONIEC TESTU ===");
    }

    {
        var population = switch (TEST_TYPE) {
            case "INSTANT" -> scenario.injectOpen(atOnceUsers(USERS));
            case "RAMP" -> scenario.injectOpen(rampUsers(USERS).during(DURATION));
            case "CONSTANT_RATE" -> scenario.injectOpen(constantUsersPerSec(USERS).during(DURATION).randomized());

            case "RAMP_RATE" -> scenario.injectOpen(rampUsersPerSec(1).to(USERS).during(DURATION).randomized());

            case "STAIRCASE" -> scenario.injectOpen(
                    incrementUsersPerSec(2.0)
                            .times(4)
                            .eachLevelLasting(Duration.ofSeconds(10))
                            .separatedByRampsLasting(Duration.ofSeconds(5))
                            .startingFrom(1.0));

            case "STRESS_PEAK" -> scenario.injectOpen(stressPeakUsers(USERS * 5).during(DURATION));

            case "CLOSED" -> scenario.injectClosed(
                    constantConcurrentUsers(USERS).during(Duration.ofSeconds(15)),
                    rampConcurrentUsers(USERS).to(USERS * 2).during(Duration.ofSeconds(15)));

            case "THROTTLED" -> scenario.injectOpen(constantUsersPerSec(USERS * 2).during(DURATION));

            case "COMPLEX" -> scenario.injectOpen(
                    nothingFor(Duration.ofSeconds(5)),
                    atOnceUsers(1),
                    rampUsers(USERS).during(Duration.ofSeconds(10)),
                    constantUsersPerSec(USERS).during(Duration.ofSeconds(10)).randomized(),
                    rampUsersPerSec(1).to(USERS).during(Duration.ofSeconds(10)));

            default -> scenario.injectOpen(atOnceUsers(1));
        };

        var setup =
                setUp(population)
                        .protocols(httpProtocol)
                        .maxDuration(Duration.ofMinutes(2))
                        .assertions(
                                global().failedRequests().percent().lt(5.0),
                                global().responseTime().percentile(95.0).lt(3000)
                        );

        if (TEST_TYPE.equals("THROTTLED")) {
            setup.throttle(reachRps(MAX_RPS).in(Duration.ofSeconds(10)), holdFor(Duration.ofSeconds(20)));
        }
    }

}
