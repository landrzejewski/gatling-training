/**
 * Etap 2 — Lancuchy requestow, konfiguracja HTTP i asercje globalne.
 *
 * <p>Cel: Sekwencyjne laczenie wielu requestow w scenariusz z think time (pause), pelna
 * konfiguracja HttpProtocolBuilder i asercje na poziomie testu.
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>Base URL: {@code https://api.practicesoftwaretesting.com}
 *   <li>Produkty w {@code $.data[]}, paginacja od strony 1, ULID IDs
 *   <li>{@code $.last_page} = 6, {@code $.total} = 50
 * </ul>
 *
 * <h2>Endpointy</h2>
 *
 * <ol>
 *   <li>{@code GET /products?page=1} → status 200
 *       <ul>
 *         <li>check: {@code jsonPath("$.last_page").ofInt().is(6)}
 *         <li>check: {@code jsonPath("$.total").ofInt().is(50)}
 *       </ul>
 *   <li>{@code GET /products?page=2} → status 200, check: {@code jsonPath("$.data").exists()}
 *   <li>{@code GET /categories} → status 200, check: {@code jsonPath("$").exists()}
 *   <li>{@code GET /brands} → status 200, check: {@code jsonPath("$").exists()}
 * </ol>
 *
 * <h2>HTTP Protocol (HttpProtocolBuilder)</h2>
 *
 * <ul>
 *   <li>{@code http.baseUrl("https://api.practicesoftwaretesting.com")}
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
 *   <li>{@code assertions()} — kryteria sukcesu testu
 *   <li>{@code global().failedRequests().percent().lt(1.0)} — max 1% bledow
 *   <li>{@code global().responseTime().percentile(95.0).lt(3000)} — p95 &lt; 3000ms
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
package pl.training.toolshop.phase2;
