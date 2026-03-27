package pl.training.ecommerce.phase7;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.ecommerce.phase7.config.Keys;

import static io.gatling.javaapi.core.CoreDsl.exec;

public class SessionHelper {

    public static final ChainBuilder initSession = exec(session -> {
        session.set(Keys.IS_AUTHENTICATED, false);
        return session;
    });

    public static ChainBuilder setAuthenticated(boolean value) {
        return exec(session -> session.set(Keys.IS_AUTHENTICATED, value));
    }

    public static ChainBuilder debugSession() {
        return exec(session -> {
            System.out.println("=== Session Debug ===");
            System.out.println("sessionId: " + session.get(Keys.SESSION_ID));
            System.out.println("isAuthenticated: " + session.get(Keys.IS_AUTHENTICATED));
            return session;
        });
    }

}
