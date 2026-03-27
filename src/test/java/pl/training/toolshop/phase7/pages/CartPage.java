package pl.training.toolshop.phase7.pages;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.toolshop.etap7.config.Keys;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

// Page Object: /carts — tworzenie i dodawanie produktow
// Toolshop koszyk 2-step: POST /carts (201) → POST /carts/{id} (200)
// ULID product_id w cudzyslowach w JSON body
public final class CartPage {

  private CartPage() {}

  // Utworzenie koszyka — POST /carts zwraca 201 z {id: "ULID"}
  public static final ChainBuilder create =
      exec(
          http("Utworz koszyk")
              .post("/carts")
              .check(status().is(201))
              .check(jsonPath("$.id").saveAs(Keys.CART_ID)));

  // Dodanie produktu — POST /carts/{cartId} z product_id jako ULID string
  public static final ChainBuilder addProduct =
      exec(
          http("Dodaj do koszyka")
              .post("/carts/#{cartId}")
              .body(
                  StringBody(
                      """
                  {"product_id":"#{productId}","quantity":1}
                  """))
              .check(status().is(200))
              .check(jsonPath("$.result").is("item added or updated")));

  // Pobranie zawartosci koszyka
  public static final ChainBuilder getCart =
      exec(
          http("Pobierz koszyk")
              .get("/carts/#{cartId}")
              .check(status().is(200))
              .check(jsonPath("$.id").exists()));
}
