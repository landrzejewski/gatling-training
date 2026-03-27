package pl.training.toolshop.phase7;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import pl.training.toolshop.etap7.config.Config;
import pl.training.toolshop.etap7.simulations.TestPopulation;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

// Etap 7 — Page Object Pattern
// Organizacja kodu: Config -> Pages -> Journeys -> Scenarios -> Populations -> Simulation
// Kazdy poziom ma jasna odpowiedzialnosc, komponenty sa reuzywalne
// Toolshop API: JWT Bearer auth, ULID IDs, koszyk 2-step, faktury
public class ToolshopSimulation extends Simulation {

  private final HttpProtocolBuilder httpProtocol =
      http.baseUrl(Config.BASE_URL)
          .acceptHeader("application/json")
          .contentTypeHeader("application/json")
          .userAgentHeader("Gatling/Etap7");

  {
    var population =
        switch (Config.TEST_TYPE) {
          case "RAMP" -> TestPopulation.ramp;
          case "CONSTANT_RATE" -> TestPopulation.constantRate;
          case "CLOSED" -> TestPopulation.closed;
          case "THROTTLED" -> TestPopulation.throttled;
          default -> TestPopulation.instant;
        };

    var setup =
        setUp(population)
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
