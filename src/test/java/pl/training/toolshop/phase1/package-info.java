/**
 * Etap 1 — Podstawy Gatling: pojedynczy request HTTP z walidacja odpowiedzi.
 *
 * <p>Cel: Wprowadzenie do struktury testu Gatling — Simulation jako klasa bazowa, scenariusz z
 * jednym requestem GET, walidacja status code i JSON response.
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>Base URL: {@code https://api.practicesoftwaretesting.com}
 *   <li>Produkty zwracane w {@code $.data[]} (tablica obiektow), ID w formacie ULID (string)
 *   <li>Paginacja od strony 1, domyslnie 9 produktow na strone, 50 lacznie
 * </ul>
 *
 * <h2>Endpointy</h2>
 *
 * <ul>
 *   <li>{@code GET /products} → status 200
 *       <ul>
 *         <li>check: {@code jsonPath("$.data").exists()}
 *         <li>check: {@code jsonPath("$.total").ofInt().is(50)}
 *         <li>check: {@code jsonPath("$.per_page").ofInt().is(9)}
 *       </ul>
 * </ul>
 *
 * <h2>HTTP Protocol (HttpProtocolBuilder)</h2>
 *
 * <ul>
 *   <li>{@code http.baseUrl("https://api.practicesoftwaretesting.com")}
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
 *   <li>{@code check(jsonPath("$.pole").ofInt().is(N))} — sprawdzenie wartosci int
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
package pl.training.toolshop.phase1;
