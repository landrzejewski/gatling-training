package pl.training.ecommerce.phase7.pages;

import io.gatling.javaapi.core.ChainBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class CheckoutProcess {

    private static final String PATH = "/checkout";

    public static final ChainBuilder checkout = exec(
            http("checkout")
                    .post(PATH)
                    .header("Authorization", "#{accessToken}")
                    .body(ElFileBody("bodies/cart.json"))
                    .check(status().is(200))
                    .check(jsonPath("$.message").is("Checkout completed"))
    );

}
