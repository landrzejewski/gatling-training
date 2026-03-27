package pl.training.toolshop.phase7;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.toolshop.etap7.config.Keys;

import static io.gatling.javaapi.core.CoreDsl.exec;

// Utilki sesji — inicjalizacja i manipulacja stanem sesji uzytkownika
public final class SessionHelper {

  private SessionHelper() {}

  public static final ChainBuilder initSession =
      exec(session -> session.set(Keys.IS_AUTHENTICATED, false));

  public static ChainBuilder setAuthenticated(boolean value) {
    return exec(session -> session.set(Keys.IS_AUTHENTICATED, value));
  }

  public static final ChainBuilder debugSession =
      exec(
          session -> {
            System.out.println("=== Stan sesji ===");
            System.out.println("  authenticated: " + session.getBoolean(Keys.IS_AUTHENTICATED));
            if (session.contains(Keys.CART_ID)) {
              System.out.println("  cartId: " + session.getString(Keys.CART_ID));
            }
            if (session.contains(Keys.ACCESS_TOKEN)) {
              System.out.println("  token: " + session.getString(Keys.ACCESS_TOKEN));
            }
            return session;
          });
}
