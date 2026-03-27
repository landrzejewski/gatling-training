package pl.training.ecommerce.phase7.pages;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.ecommerce.phase7.config.Keys;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class SessionManagement {

    private static final String PATH = "/session";

    public static final ChainBuilder create = exec(
            http("create session")
                    .get(PATH)
                    .check(status().is(200))
                    .check(jsonPath("$.sessionId").saveAs(Keys.SESSION_ID))
    );

}
