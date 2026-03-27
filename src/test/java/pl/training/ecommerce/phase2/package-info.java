/**
 * Etap 2 — Lancuchy requestow, konfiguracja HTTP i asercje globalne.
 *
 * <p>Cel: Sekwencyjne laczenie wielu requestow w scenariusz z think time (pause), pelna
 * konfiguracja HttpProtocolBuilder z userAgent i asercje na poziomie testu.
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>Base URL: {@code https://api-ecomm.gatling.io}
 *   <li>Produkty w {@code $.products[]}, numeryczne ID (int), {@code $.totalPages} = 4
 *   <li>Paginacja od strony 0
 * </ul>
 *
 * <h2>Endpointy</h2>
 *
 * <ol>
 *   <li>{@code GET /products} → status 200
 *       <ul>
 *         <li>check: {@code jsonPath("$.totalPages").ofInt().is(4)}
 *         <li>check: {@code jsonPath("$.products[0].id").ofInt().gte(0)}
 *       </ul>
 *   <li>{@code GET /products?page=1} → status 200, check: {@code jsonPath("$.products").exists()}
 *   <li>{@code GET /products/0} → status 200
 *       <ul>
 *         <li>check: {@code jsonPath("$.name").exists()}
 *         <li>check: {@code jsonPath("$.price").ofDouble().gt(0.0)}
 *       </ul>
 *   <li>{@code GET /products?search=shirt} → status 200, check: {@code
 *       jsonPath("$.products").exists()}
 * </ol>
 *
 * <h2>HTTP Protocol (HttpProtocolBuilder)</h2>
 *
 * <ul>
 *   <li>{@code http.baseUrl("https://api-ecomm.gatling.io")}
 *   <li>{@code .acceptHeader("application/json")}
 *   <li>{@code .contentTypeHeader("application/json")}
 *   <li>{@code .userAgentHeader("Gatling/Etap2")}
 * </ul>
 *
 * <h2>Gatling API</h2>
 *
 * <ul>
 *   <li>{@code pause(1, 3)} — losowy think time 1-3s miedzy requestami
 *   <li>{@code rampUsers(3).during(5)} — 3 uzytkownikow wstrzykiwanych liniowo przez 5s
 *   <li>{@code ofInt()}, {@code ofDouble()} — konwersja typow w check
 *   <li>{@code gte(0)}, {@code gt(0.0)} — porownania (greater than or equal / greater than)
 *   <li>{@code assertions()} — kryteria sukcesu testu
 * </ul>
 *
 * <h2>Injection</h2>
 *
 * <ul>
 *   <li>{@code rampUsers(3).during(5)}
 * </ul>
 *
 * <h2>Assertions</h2>
 *
 * <ul>
 *   <li>{@code global().failedRequests().percent().lt(1.0)}
 *   <li>{@code global().responseTime().percentile(95.0).lt(3000)}
 * </ul>
 */
package pl.training.ecommerce.phase2;
