package pl.training.videogames.pages;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.videogames.SessionHelper;
import pl.training.videogames.config.Keys;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

// Page Object: POST /authenticate — logowanie z JSON body
// VideoGame API: endpoint /authenticate, response $.token (nie $.access_token)
public final class AuthPage {

  private AuthPage() {}

  public static final ChainBuilder login =
      exec(http("Logowanie")
              .post("/authenticate")
              .body(StringBody("{\"username\":\"admin\",\"password\":\"admin\"}"))
              .check(status().is(200))
              .check(jsonPath("$.token").saveAs(Keys.JWT_TOKEN)))
          .exec(SessionHelper.setAuthenticated(true));
}
