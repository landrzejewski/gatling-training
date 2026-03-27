package pl.training.videogames;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.videogames.config.Keys;

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
            System.out.println("=== Debug sesji ===");
            System.out.println("  authenticated: " + session.getBoolean(Keys.IS_AUTHENTICATED));
            if (session.contains(Keys.JWT_TOKEN)) {
              System.out.println("  jwtToken: " + session.getString(Keys.JWT_TOKEN));
            }
            if (session.contains(Keys.GAME_ID)) {
              System.out.println("  gameId: " + session.getString(Keys.GAME_ID));
            }
            if (session.contains(Keys.GAME_NAME)) {
              System.out.println("  name: " + session.getString(Keys.GAME_NAME));
            }
            return session;
          });
}
