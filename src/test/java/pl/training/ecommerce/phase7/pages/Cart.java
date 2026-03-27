package pl.training.ecommerce.phase7.pages;

import io.gatling.javaapi.core.ChainBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class Cart {

    private static final String PATH = "/cart";

    public static ChainBuilder addProduct(int productId, int quantity) {
        return exec(
                http("add product to cart with id: " + productId)
                        .post(PATH)
                        .body(StringBody("""
                                {"sessionId":"#{sessionId}","cart":[{"productId":%d,"quantity":%d}]}
                                """
                                .formatted(productId, quantity))
                        )
                        .check(status().is(200))
                        .check(jsonPath("$.message").is("Cart updated"))
        );
    }

    public static final ChainBuilder addProduct = exec(
            http("add product to cart with id: #{productId}")
                    .post(PATH)
                    .body(StringBody("""
                            {"sessionId":"#{sessionId}","cart":[{"productId":%d,"quantity":%d}]}
                            """
                    ))
                    .check(status().is(200))
                    .check(jsonPath("$.message").is("Cart updated"))
    );

}
