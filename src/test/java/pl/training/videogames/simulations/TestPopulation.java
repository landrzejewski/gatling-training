package pl.training.videogames.simulations;

import io.gatling.javaapi.core.PopulationBuilder;
import pl.training.videogames.config.Config;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

// Population — 7 profili wstrzykiwania uzytkownikow
public final class TestPopulation {

  private TestPopulation() {}

  public static final PopulationBuilder instant =
      TestScenario.defaultLoadTest.injectOpen(nothingFor(2), atOnceUsers(Config.USERS));

  public static final PopulationBuilder ramp =
      TestScenario.defaultLoadTest.injectOpen(rampUsers(Config.USERS).during(Config.RAMP_DURATION));

  public static final PopulationBuilder constantRate =
      TestScenario.defaultLoadTest.injectOpen(
          constantUsersPerSec(Config.USERS).during(Config.TEST_DURATION).randomized());

  // Staircase — schodkowy wzrost obciazenia
  public static final PopulationBuilder staircase =
      TestScenario.defaultLoadTest.injectOpen(
          incrementUsersPerSec(2.0)
              .times(4)
              .eachLevelLasting(Duration.ofSeconds(10))
              .separatedByRampsLasting(Duration.ofSeconds(5))
              .startingFrom(1.0));

  // Stress Peak — krzywa S symulujaca skok ruchu
  public static final PopulationBuilder stressPeak =
      TestScenario.defaultLoadTest.injectOpen(
          stressPeakUsers(Config.USERS * 5).during(Duration.ofSeconds(30)));

  public static final PopulationBuilder closed =
      TestScenario.highCrudLoadTest.injectClosed(
          constantConcurrentUsers(Config.USERS).during(Duration.ofSeconds(15)),
          rampConcurrentUsers(Config.USERS).to(Config.USERS * 2).during(Duration.ofSeconds(15)));

  public static final PopulationBuilder throttled =
      TestScenario.defaultLoadTest.injectOpen(
          constantUsersPerSec(Config.USERS * 2).during(Config.TEST_DURATION));
}
