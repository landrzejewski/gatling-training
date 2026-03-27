/**
 * Etap 1 — Podstawy Gatling: pojedynczy request HTTP z walidacja odpowiedzi.
 *
 * <p>Cel: Wprowadzenie do struktury testu Gatling — Simulation jako klasa bazowa, scenariusz z
 * jednym requestem GET, walidacja status code i JSON response.
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>Base URL: {@code https://api-ecomm.gatling.io}
 *   <li>Produkty zwracane w {@code $.products[]} (tablica obiektow), ID numeryczne (int)
 *   <li>Paginacja od strony 0, {@code $.totalPages} = 4
 * </ul>
 *
 * <h2>Endpointy</h2>
 *
 * <ul>
 *   <li>{@code GET /products} → status 200
 *       <ul>
 *         <li>check: {@code jsonPath("$.products").exists()}
 *         <li>check: {@code jsonPath("$.totalPages").isEL("4")}
 *       </ul>
 * </ul>
 *
 * <h2>HTTP Protocol (HttpProtocolBuilder)</h2>
 *
 * <ul>
 *   <li>{@code http.baseUrl("https://api-ecomm.gatling.io")}
 *   <li>{@code .acceptHeader("application/json")}
 * </ul>
 *
 * <h2>Gatling API</h2>
 *
 * <ul>
 *   <li>{@code Simulation} — klasa bazowa testu
 *   <li>{@code ScenarioBuilder} — {@code scenario("Etap 1 - Podstawy")}
 *   <li>{@code exec(http("nazwa").get("/path"))} — pojedynczy request GET
 *   <li>{@code check(status().is(200))} — walidacja HTTP status code
 *   <li>{@code check(jsonPath("$.pole").exists())} — sprawdzenie istnienia pola
 *   <li>{@code check(jsonPath("$.pole").isEL("wartosc"))} — sprawdzenie wartosci jako EL string
 *   <li>{@code setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol)}
 * </ul>
 *
 * <h2>Injection</h2>
 *
 * <ul>
 *   <li>{@code atOnceUsers(1)} — natychmiastowe uruchomienie 1 uzytkownika
 * </ul>
 *
 * <h2>Assertions</h2>
 *
 * Brak jawnych asercji (implicit: wszystkie check() musza przejsc).
 */
package pl.training.ecommerce.phase1;
