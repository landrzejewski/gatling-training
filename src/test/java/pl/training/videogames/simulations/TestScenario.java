package pl.training.videogames.simulations;

import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.ScenarioBuilder;
import pl.training.videogames.config.Config;

import static io.gatling.javaapi.core.CoreDsl.*;

// Scenariusze z konfigurowalnymi 4 wagami
// Uzycie: -DBROWSE_WEIGHT=40 -DCREATE_VIEW_WEIGHT=30 -DFULL_CRUD_WEIGHT=20 -DSEARCH_WEIGHT=10
public final class TestScenario {

  private TestScenario() {}

  // Domyslny test: 4 Journey z konfigurowalnymi wagami
  public static final ScenarioBuilder defaultLoadTest =
      scenario("VideoGame Load Test")
          .during(Config.TEST_DURATION)
          .on(
              randomSwitch()
                  .on(
                      new Choice.WithWeight(Config.BROWSE_WEIGHT, exec(UserJourney.browseGames)),
                      new Choice.WithWeight(
                          Config.CREATE_VIEW_WEIGHT, exec(UserJourney.createAndView)),
                      new Choice.WithWeight(Config.FULL_CRUD_WEIGHT, exec(UserJourney.fullCrud)),
                      new Choice.WithWeight(
                          Config.SEARCH_WEIGHT, exec(UserJourney.searchByCategory))));

  // Test z duza iloscia CRUD
  public static final ScenarioBuilder highCrudLoadTest =
      scenario("High CRUD Load Test")
          .during(Config.TEST_DURATION)
          .on(
              randomSwitch()
                  .on(
                      new Choice.WithWeight(20.0, exec(UserJourney.browseGames)),
                      new Choice.WithWeight(20.0, exec(UserJourney.createAndView)),
                      new Choice.WithWeight(40.0, exec(UserJourney.fullCrud)),
                      new Choice.WithWeight(20.0, exec(UserJourney.searchByCategory))));
}
