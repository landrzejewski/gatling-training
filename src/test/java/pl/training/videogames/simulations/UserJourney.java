package pl.training.videogames.simulations;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.videogames.SessionHelper;
import pl.training.videogames.config.Config;
import pl.training.videogames.pages.AuthPage;
import pl.training.videogames.pages.VideoGamePage;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.pace;

// Cztery journey: browse, createAndView, fullCrud, searchByCategory
// VideoGame API: mock — POST/PUT/DELETE nie mutuja danych, gry 1-10 zawsze dostepne
public final class UserJourney {

  private UserJourney() {}

  // 1. Przegladanie gier (40%) — read-only, bez auth
  public static final ChainBuilder browseGames =
      pace(Config.PACE_DURATION)
          .group("Przegladanie gier")
          .on(
              exec(SessionHelper.initSession)
                  .exec(VideoGamePage.list)
                  .pause(Config.MIN_PAUSE, Config.MAX_PAUSE)
                  .exec(VideoGamePage.details)
                  .pause(Config.MIN_PAUSE, Config.MAX_PAUSE)
                  .exec(VideoGamePage.list));

  // 2. Tworzenie i podglad (25%) — auth + create + details
  public static final ChainBuilder createAndView =
      pace(Config.PACE_DURATION)
          .group("Tworzenie i podglad")
          .on(
              exec(SessionHelper.initSession)
                  .exitBlockOnFail()
                  .on(
                      exec(AuthPage.login)
                          .pause(Config.MIN_PAUSE, Config.MAX_PAUSE)
                          .exec(VideoGamePage.create)
                          .pause(Config.MIN_PAUSE, Config.MAX_PAUSE)
                          .exec(VideoGamePage.details)
                          .pause(Config.MIN_PAUSE, Config.MAX_PAUSE)
                          .exec(VideoGamePage.list)));

  // 3. Pelny CRUD (20%) — mapuje oryginalny flow z VideoGameFullTest
  public static final ChainBuilder fullCrud =
      pace(Config.PACE_DURATION)
          .group("Pelny CRUD")
          .on(
              exec(SessionHelper.initSession)
                  .exec(VideoGamePage.list)
                  .pause(Config.MIN_PAUSE, Config.MAX_PAUSE)
                  .exitBlockOnFail()
                  .on(
                      exec(AuthPage.login)
                          .pause(Config.MIN_PAUSE, Config.MAX_PAUSE)
                          .exec(VideoGamePage.create)
                          .pause(Config.MIN_PAUSE, Config.MAX_PAUSE)
                          .exec(VideoGamePage.details)
                          .pause(Config.MIN_PAUSE, Config.MAX_PAUSE)
                          .exec(VideoGamePage.update)
                          .pause(Config.MIN_PAUSE, Config.MAX_PAUSE)
                          .exec(VideoGamePage.delete)));

  // 4. Przegladanie kategorii (15%) — dwa losowe details (kazdy feeduje nowy rekord)
  public static final ChainBuilder searchByCategory =
      pace(Config.PACE_DURATION)
          .group("Przegladanie kategorii")
          .on(
              exec(SessionHelper.initSession)
                  .exec(VideoGamePage.list)
                  .pause(Config.MIN_PAUSE, Config.MAX_PAUSE)
                  .exec(VideoGamePage.details)
                  .pause(Config.MIN_PAUSE, Config.MAX_PAUSE)
                  .exec(VideoGamePage.details)
                  .pause(Config.MIN_PAUSE, Config.MAX_PAUSE)
                  .exec(VideoGamePage.list));
}
