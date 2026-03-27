package pl.training.ecommerce.phase7.pages;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.ecommerce.phase7.config.Keys;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class Authentication {

    private static final String PATH = "/login";

    public static ChainBuilder login(String username, String password) {
        return exec(
                http("Login")
                        .post(PATH)
                        .formParam("username", username)
                        .formParam("password", password)
                        .check(status().is(200))
                        .check(jsonPath("$.accessToken").saveAs(Keys.ACCESS_TOKEN))
        );
    }

}
