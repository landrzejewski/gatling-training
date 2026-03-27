package pl.training.toolshop.phase7.pages;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.toolshop.etap7.config.Keys;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

// Page Object: /favorites — dodawanie, lista, usuwanie ulubionych
// Wszystkie endpointy wymagaja JWT Bearer token
// POST → 201, DELETE → 204 (rozne kody!)
public final class FavoritesPage {

  private FavoritesPage() {}

  // Dodanie do ulubionych — POST /favorites zwraca 201
  public static final ChainBuilder add =
      exec(
          http("Dodaj do ulubionych")
              .post("/favorites")
              .header("Authorization", "Bearer #{accessToken}")
              .body(
                  StringBody(
                      """
                  {"product_id":"#{productId}"}
                  """))
              .check(status().is(201))
              .check(jsonPath("$.id").saveAs(Keys.FAVORITE_ID)));

  // Lista ulubionych
  public static final ChainBuilder list =
      exec(
          http("Lista ulubionych")
              .get("/favorites")
              .header("Authorization", "Bearer #{accessToken}")
              .check(status().is(200)));

  // Usuniecie z ulubionych — DELETE zwraca 204 (brak body)
  public static final ChainBuilder remove =
      exec(
          http("Usun z ulubionych")
              .delete("/favorites/#{favoriteId}")
              .header("Authorization", "Bearer #{accessToken}")
              .check(status().is(204)));
}
