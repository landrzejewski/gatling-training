package pl.training.ecommerce.phase7;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import pl.training.ecommerce.phase7.config.Config;
import pl.training.ecommerce.phase7.simulations.TestPopulation;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class EcommSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol =
            http.baseUrl("https://api-ecomm.gatling.io")
                    .acceptHeader("application/json")
                    .contentTypeHeader("application/json");

    {
        var population = switch (Config.TEST_TYPE) {
            case "RAMP" -> TestPopulation.RAMP;
            case "CONSTANT_RATE" -> TestPopulation.CONSTANT_RATE;
            case "CLOSED" -> TestPopulation.CLOSED;
            default -> TestPopulation.THROTTLED;
        };

        var setup = setUp(population)
                .protocols(httpProtocol)
                .maxDuration(Duration.ofMinutes(2))
                .assertions(
                        global().failedRequests().percent().lt(5.0),
                        global().responseTime().percentile(95.0).lt(3000));

        if (Config.TEST_TYPE.equals("THROTTLED")) {
            setup.throttle(
                    reachRps(Config.MAX_RPS).in(Duration.ofSeconds(10)), holdFor(Duration.ofSeconds(20)));
        }
    }

}
