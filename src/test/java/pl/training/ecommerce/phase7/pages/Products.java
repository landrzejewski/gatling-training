package pl.training.ecommerce.phase7.pages;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.ecommerce.phase7.config.Keys;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public final class Products {

    private static final String PATH = "/products";

    public static final ChainBuilder list =
            exec(
                    http("get products")
                            .get(PATH)
                            .check(status().is(200))
                            .check(jsonPath("$.products[0].id").saveAs(Keys.PRODUCT_ID))
                            .check(jsonPath("$.products[0].name").saveAs(Keys.PRODUCT_NAME))
            );

    public static ChainBuilder listPage(int page) {
        return exec(
                http("get products page " + page)
                        .get(PATH + "?page=" + page)
                        .check(status().is(200))
        );
    }

    public static final ChainBuilder details =
            exec(
                    http("get product #{productId}")
                            .get(PATH + "/#{productId}")
                            .check(status().is(200))
                            .check(jsonPath("$.price").saveAs(Keys.PRODUCT_PRICE))
            );

    public static ChainBuilder search(String term) {
        return exec(
                http("search products: " + term)
                        .get(PATH + "?search=" + term)
                        .check(status().is(200))
                        .check(jsonPath("$.products").exists())
        );
    }

    public static final ChainBuilder searchFromSession =
            exec(
                    http("search products: #{searchTerm}")
                            .get(PATH + "?search=#{searchTerm}")
                            .check(status().is(200))
                            .check(jsonPath("$.products").exists())
            );

}
