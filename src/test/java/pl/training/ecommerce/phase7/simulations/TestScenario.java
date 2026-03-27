package pl.training.ecommerce.phase7.simulations;

import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.ScenarioBuilder;
import pl.training.ecommerce.phase7.config.Config;

import static io.gatling.javaapi.core.CoreDsl.*;

public class TestScenario {

    // Domyslny test: 60% przegladanie, 30% porzucony koszyk, 10% zakup
    public static final ScenarioBuilder defaultLoadTest = scenario("Default Load Test")
            .during(Config.TEST_DURATION)
            .on(
                    randomSwitch()
                            .on(
                                    new Choice.WithWeight(60.0, exec(UserJourney.browseProducts)),
                                    new Choice.WithWeight(30.0, exec(UserJourney.abandonCart)),
                                    new Choice.WithWeight(10.0, exec(UserJourney.completePurchase))
                            )
            );

    // Test z duza iloscia zakupow
    public static final ScenarioBuilder highPurchaseLoadTest = scenario("High Purchase Load Test")
            .during(Config.TEST_DURATION)
            .on(
                    randomSwitch()
                            .on(
                                    new Choice.WithWeight(30.0, exec(UserJourney.browseProducts)),
                                    new Choice.WithWeight(30.0, exec(UserJourney.abandonCart)),
                                    new Choice.WithWeight(40.0, exec(UserJourney.completePurchase))
                            )
            );

}
