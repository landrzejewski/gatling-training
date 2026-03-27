package pl.training.videogames;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import pl.training.videogames.config.Config;
import pl.training.videogames.simulations.TestPopulation;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class VideoGameSimulation extends Simulation {

  private final HttpProtocolBuilder httpProtocol =
      http.baseUrl(Config.BASE_URL)
          .acceptHeader("application/json")
          .contentTypeHeader("application/json")
          .userAgentHeader("Gatling/Etap8-Production");

  @Override
  public void before() {
    System.out.println("VideoGame Performance Test - START");
    System.out.println("Base URL:    " + Config.BASE_URL);
    System.out.println("Test Type:   " + Config.TEST_TYPE);
    System.out.println("Users:       " + Config.USERS);
    System.out.println("Duration:    " + Config.TEST_DURATION.toSeconds() + "s");
    System.out.println(
        "Weights:     browse="
            + Config.BROWSE_WEIGHT
            + " createView="
            + Config.CREATE_VIEW_WEIGHT
            + " fullCrud="
            + Config.FULL_CRUD_WEIGHT
            + " search="
            + Config.SEARCH_WEIGHT);
  }

  @Override
  public void after() {
    System.out.println("VideoGame Performance Test - END");
  }

  {
    var population =
        switch (Config.TEST_TYPE) {
          case "RAMP" -> TestPopulation.ramp;
          case "CONSTANT_RATE" -> TestPopulation.constantRate;
          case "STAIRCASE" -> TestPopulation.staircase;
          case "STRESS_PEAK" -> TestPopulation.stressPeak;
          case "CLOSED" -> TestPopulation.closed;
          case "THROTTLED" -> TestPopulation.throttled;
          default -> TestPopulation.instant;
        };

    var setup =
        setUp(population)
            .protocols(httpProtocol)
            .maxDuration(Duration.ofMinutes(5))
            .assertions(
                global().failedRequests().percent().lt(5.0),
                global().responseTime().percentile(95.0).lt(3000),
                global().responseTime().percentile(99.0).lt(5000),
                details("Przegladanie gier", "Lista gier").responseTime().percentile(95.0).lt(2000),
                forAll().failedRequests().percent().lt(20.0));

    if (Config.TEST_TYPE.equals("THROTTLED")) {
      setup.throttle(
          reachRps(Config.MAX_RPS).in(Duration.ofSeconds(10)), holdFor(Duration.ofSeconds(20)));
    }
  }
}
