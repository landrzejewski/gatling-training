package pl.training.toolshop.phase7.simulations;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.toolshop.etap7.SessionHelper;
import pl.training.toolshop.etap7.config.Config;
import pl.training.toolshop.etap7.pages.*;

import java.time.Duration;

// Journey — definiuje kompletne sciezki uzytkownika skladajace sie z Page Objects
// Separacja: Pages (pojedyncze akcje) -> Journeys (przeplyw biznesowy)
// Toolshop: koszyk 2-step, JWT Bearer auth, ULID IDs
public final class UserJourney {

  private UserJourney() {}

  private static final Duration MIN_PAUSE = Duration.ofSeconds(1);
  private static final Duration MAX_PAUSE = Duration.ofSeconds(3);

  // Przegladanie sklepu: koszyk → produkty → szczegoly → powiazane → strona 2
  public static final ChainBuilder browseStore =
      pace(Config.PACE_DURATION)
          .group("Przegladanie sklepu")
          .on(
              exec(SessionHelper.initSession)
                  .exec(CartPage.create)
                  .pause(MIN_PAUSE, MAX_PAUSE)
                  .exec(ProductsPage.list)
                  .pause(MIN_PAUSE, MAX_PAUSE)
                  .exec(ProductsPage.details)
                  .pause(MIN_PAUSE, MAX_PAUSE)
                  .exec(ProductsPage.related)
                  .pause(MIN_PAUSE, MAX_PAUSE)
                  .exec(ProductsPage.listPage(2)));

  // Porzucony koszyk: koszyk → produkty → szczegoly → dodaj (bez checkout)
  public static final ChainBuilder abandonCart =
      pace(Config.PACE_DURATION)
          .group("Porzucony koszyk")
          .on(
              exec(SessionHelper.initSession)
                  .exec(CartPage.create)
                  .pause(MIN_PAUSE, MAX_PAUSE)
                  .exec(ProductsPage.list)
                  .pause(MIN_PAUSE, MAX_PAUSE)
                  .exec(ProductsPage.details)
                  .pause(MIN_PAUSE, MAX_PAUSE)
                  .exec(CartPage.addProduct));

  // Pelny zakup: koszyk → produkty → szczegoly → dodaj → login → faktura
  // exitBlockOnFail() chroni przed kontynuacja po bledzie logowania/faktury
  public static final ChainBuilder completePurchase =
      pace(Config.PACE_DURATION)
          .group("Pelny zakup")
          .on(
              exec(SessionHelper.initSession)
                  .exec(CartPage.create)
                  .pause(MIN_PAUSE, MAX_PAUSE)
                  .exec(ProductsPage.list)
                  .pause(MIN_PAUSE, MAX_PAUSE)
                  .exec(ProductsPage.details)
                  .pause(MIN_PAUSE, MAX_PAUSE)
                  .exec(CartPage.addProduct)
                  .pause(MIN_PAUSE, MAX_PAUSE)
                  .exitBlockOnFail()
                  .on(exec(AuthPage.login).pause(MIN_PAUSE, MAX_PAUSE).exec(InvoicePage.create)));
}
