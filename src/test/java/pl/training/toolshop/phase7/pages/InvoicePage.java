package pl.training.toolshop.phase7.pages;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.toolshop.etap7.config.Keys;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

// Page Object: POST /invoices — finalizacja zamowienia (checkout)
// Toolshop: wymaga JWT Bearer token, payment_method z myslnikami
// POST /invoices zwraca 201
public final class InvoicePage {

  private InvoicePage() {}

  // Utworzenie faktury — checkout przez POST /invoices
  // Header: Authorization: Bearer #{accessToken} (z prefixem Bearer!)
  public static final ChainBuilder create =
      exec(
          http("Utworz fakture")
              .post("/invoices")
              .header("Authorization", "Bearer #{accessToken}")
              .body(
                  StringBody(
                      """
                      {"cart_id":"#{cartId}","payment_method":"cash-on-delivery","payment_details":{},"billing_street":"123 Main St","billing_city":"New York","billing_state":"NY","billing_country":"US","billing_postcode":"10001"}
                      """))
              .check(status().is(201))
              .check(jsonPath("$.id").saveAs(Keys.INVOICE_ID)));

  // Lista faktur (wymaga JWT Bearer)
  public static final ChainBuilder list =
      exec(
          http("Lista faktur")
              .get("/invoices")
              .header("Authorization", "Bearer #{accessToken}")
              .check(status().is(200)));
}
