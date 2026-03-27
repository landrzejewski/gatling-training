package pl.training.ecommerce.phase7.simulations;

import io.gatling.javaapi.core.PopulationBuilder;
import pl.training.ecommerce.phase7.config.Config;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

// Population — definiuje profile wstrzykiwania (ile uzytkownikow, jak szybko)
public final class TestPopulation {

  private TestPopulation() {}

  public static final PopulationBuilder INSTANT = TestScenario.defaultLoadTest
          .injectOpen(nothingFor(2), atOnceUsers(Config.USERS));

  public static final PopulationBuilder RAMP = TestScenario.defaultLoadTest
          .injectOpen(rampUsers(Config.USERS).during(Config.RAMP_DURATION));

  public static final PopulationBuilder CONSTANT_RATE = TestScenario.defaultLoadTest
          .injectOpen(constantUsersPerSec(Config.USERS).during(Config.TEST_DURATION).randomized());

  public static final PopulationBuilder CLOSED = TestScenario.highPurchaseLoadTest
          .injectClosed(constantConcurrentUsers(Config.USERS).during(Duration.ofSeconds(15)),
          rampConcurrentUsers(Config.USERS).to(Config.USERS * 2).during(Duration.ofSeconds(15)));

  public static final PopulationBuilder THROTTLED = TestScenario.defaultLoadTest
          .injectOpen(constantUsersPerSec(Config.USERS * 2).during(Config.TEST_DURATION));

}
