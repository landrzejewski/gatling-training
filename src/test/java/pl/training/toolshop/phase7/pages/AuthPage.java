package pl.training.toolshop.phase7.pages;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.toolshop.etap7.SessionHelper;
import pl.training.toolshop.etap7.config.Keys;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

// Page Object: POST /users/login — logowanie z JSON body (NIE formParam!)
// Toolshop uzywa JWT Bearer token: Authorization: Bearer <token>
public final class AuthPage {

  private AuthPage() {}

  // Login przez JSON body — Toolshop wymaga {email, password} jako JSON
  // Odpowiedz: {access_token, token_type:"bearer", expires_in:300}
  public static final ChainBuilder login =
      exec(http("Logowanie")
              .post("/users/login")
              .body(
                  StringBody(
                      """
                      {"email":"customer@practicesoftwaretesting.com","password":"welcome01"}
                      """))
              .check(status().is(200))
              .check(jsonPath("$.access_token").saveAs(Keys.ACCESS_TOKEN)))
          .exec(SessionHelper.setAuthenticated(true));
}
