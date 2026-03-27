/**
 * Etap 4 — Feedery (CSV, JSON, custom Iterator) i parametryzacja requestow.
 *
 * <p>Cel: Zewnetrzne zrodla danych testowych — trzy typy feederow, ElFileBody do szablonow z pliku,
 * repeat().on() z feederem w petli.
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>Base URL: {@code https://api-ecomm.gatling.io}
 *   <li>Produkty w {@code $.products[]}, numeryczne ID (0-31)
 *   <li>Sesja: {@code GET /session} → {@code {sessionId: "uuid"}}
 *   <li>Koszyk: {@code POST /cart} → {@code {message: "Cart updated"}}
 * </ul>
 *
 * <h2>Endpointy</h2>
 *
 * <ol>
 *   <li>{@code GET /session} → {@code jsonPath("$.sessionId").saveAs("sessionId")}
 *   <li>{@code GET /products/#{productId}} (z CSV feedera) → status 200, check: {@code $.name}
 *       exists
 *   <li>{@code GET /products?search=#{searchTerm}} (z JSON feedera) → status 200, check: {@code
 *       $.products} exists
 *   <li>{@code GET /products/#{randomProductId}} (z custom feedera, repeat 3x) → status 200
 *   <li>{@code POST /cart} → status 200, check: {@code jsonPath("$.message").is("Cart updated")}
 *       <ul>
 *         <li>body: {@code ElFileBody("bodies/ecomm-cart.json")}
 *       </ul>
 * </ol>
 *
 * <h2>Feedery</h2>
 *
 * <ul>
 *   <li>{@code csv("data/ecomm-products.csv").circular()} — kolumny: productId, productName,
 *       expectedPrice
 *   <li>{@code jsonFile("data/ecomm-search-terms.json").random()} — format: {@code
 *       [{"searchTerm":"shirt","minResults":1}, ...]}
 *   <li>Custom {@code Iterator<Map<String, Object>>} — {@code Stream.generate()} z {@code
 *       ThreadLocalRandom}: {@code randomProductId} (0-31), {@code quantity} (1-3)
 * </ul>
 *
 * <h2>Dane testowe</h2>
 *
 * <ul>
 *   <li>{@code data/ecomm-products.csv}: productId (0-31), productName, expectedPrice
 *   <li>{@code data/ecomm-search-terms.json}: searchTerm, minResults
 * </ul>
 *
 * <h2>Szablony body</h2>
 *
 * <ul>
 *   <li>{@code bodies/ecomm-cart.json}: {@code
 *       {"sessionId":"#{sessionId}","cart":[{"productId":#{productId},"quantity":#{quantity}}]}}
 * </ul>
 *
 * <h2>Gatling API</h2>
 *
 * <ul>
 *   <li>{@code FeederBuilder} — typ feedera CSV/JSON
 *   <li>{@code csv()}, {@code jsonFile()} — ladowanie danych z plikow
 *   <li>{@code .circular()}, {@code .random()} — strategie pobierania rekordow (inne: {@code
 *       .queue()}, {@code .shuffle()})
 *   <li>{@code feed(feeder)} — wstrzykniecie danych do sesji
 *   <li>{@code ElFileBody("sciezka")} — szablon body z pliku z interpolacja EL
 *   <li>{@code repeat(3).on(feed(customFeeder)...)} — petla z feederem
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
 *   <li>{@code global().failedRequests().percent().lt(5.0)}
 * </ul>
 */
package pl.training.ecommerce.phase4;
