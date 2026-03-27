package pl.training.toolshop.phase7.pages;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.toolshop.etap7.config.Keys;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

// Page Object: /products — lista, szczegoly, powiazane, wyszukiwanie
// Toolshop: ULID IDs, $.data[] zamiast $.products[], paginacja od 1
public final class ProductsPage {

  private ProductsPage() {}

  // Lista produktow — ekstrakcja ULID pierwszego produktu
  public static final ChainBuilder list =
      exec(
          http("Lista produktow")
              .get("/products")
              .check(status().is(200))
              .check(jsonPath("$.data[0].id").saveAs(Keys.PRODUCT_ID))
              .check(jsonPath("$.data[0].name").saveAs(Keys.PRODUCT_NAME)));

  // Lista produktow na konkretnej stronie (Toolshop: strony od 1)
  public static ChainBuilder listPage(int page) {
    return exec(
        http("Lista produktow - strona " + page)
            .get("/products?page=" + page)
            .check(status().is(200)));
  }

  // Szczegoly produktu — uzywa ULID z sesji
  public static final ChainBuilder details =
      exec(
          http("Szczegoly produktu #{productId}")
              .get("/products/#{productId}")
              .check(status().is(200))
              .check(jsonPath("$.price").saveAs(Keys.PRODUCT_PRICE)));

  // Powiazane produkty
  public static final ChainBuilder related =
      exec(
          http("Powiazane produkty #{productId}")
              .get("/products/#{productId}/related")
              .check(status().is(200)));

  // Wyszukiwanie z parametrem
  public static ChainBuilder search(String term) {
    return exec(
        http("Wyszukiwanie: " + term)
            .get("/products/search?q=" + term)
            .check(status().is(200))
            .check(jsonPath("$.data").exists()));
  }
}
