package pl.training.toolshop.phase7.simulations;

import io.gatling.javaapi.core.PopulationBuilder;
import pl.training.toolshop.etap7.config.Config;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

// Population — definiuje profile wstrzykiwania (ile uzytkownikow, jak szybko)
public final class TestPopulation {

  private TestPopulation() {}

  public static final PopulationBuilder instant =
      TestScenario.defaultLoadTest.injectOpen(nothingFor(2), atOnceUsers(Config.USERS));

  public static final PopulationBuilder ramp =
      TestScenario.defaultLoadTest.injectOpen(rampUsers(Config.USERS).during(Config.RAMP_DURATION));

  public static final PopulationBuilder constantRate =
      TestScenario.defaultLoadTest.injectOpen(
          constantUsersPerSec(Config.USERS).during(Config.TEST_DURATION).randomized());

  public static final PopulationBuilder closed =
      TestScenario.highPurchaseLoadTest.injectClosed(
          constantConcurrentUsers(Config.USERS).during(Duration.ofSeconds(15)),
          rampConcurrentUsers(Config.USERS).to(Config.USERS * 2).during(Duration.ofSeconds(15)));

  public static final PopulationBuilder throttled =
      TestScenario.defaultLoadTest.injectOpen(
          constantUsersPerSec(Config.USERS * 2).during(Config.TEST_DURATION));
}
