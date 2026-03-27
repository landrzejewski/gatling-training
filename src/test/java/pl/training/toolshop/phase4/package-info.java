/**
 * Etap 4 — Feedery (CSV, JSON, custom Iterator) i parametryzacja requestow.
 *
 * <p>Cel: Zewnetrzne zrodla danych testowych — trzy typy feederow, ElFileBody do szablonow z pliku,
 * repeat().on() z feederem w petli.
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>Base URL: {@code https://api.practicesoftwaretesting.com}
 *   <li>Produkty w {@code $.data[]}, ID w formacie ULID (string)
 *   <li>Koszyk 2-step: {@code POST /carts} (201, response: {@code {id: "ULID"}}) → {@code POST
 *       /carts/{cartId}} (200, response: {@code {result: "item added or updated"}})
 * </ul>
 *
 * <h2>Endpointy</h2>
 *
 * <ol>
 *   <li>{@code POST /carts} → status 201, {@code jsonPath("$.id").saveAs("cartId")}
 *   <li>{@code GET /products} → status 200, {@code saveAs("productId")} z {@code $.data[0].id},
 *       {@code saveAs("productName")} z {@code $.data[0].name}
 *   <li>{@code GET /products/#{productId}} → status 200, check: {@code jsonPath("$.name").exists()}
 *   <li>{@code GET /products/search?q=#{searchTerm}} → status 200, check: {@code
 *       jsonPath("$.data").exists()}
 *   <li>{@code POST /carts/#{cartId}} → status 200, check: {@code jsonPath("$.result").is("item
 *       added or updated")}
 *       <ul>
 *         <li>body: {@code ElFileBody("bodies/toolshop-cart-item.json")}
 *         <li>szablon: {@code {"product_id":"#{productId}","quantity":#{quantity}}}
 *       </ul>
 * </ol>
 *
 * <h2>Feedery</h2>
 *
 * <ul>
 *   <li>{@code csv("data/toolshop-products.csv").circular()} — kolumny: productId, productName
 *       (UWAGA: ULID w CSV staja sie nieaktualne po resecie bazy — dynamiczne pobieranie z API
 *       preferowane)
 *   <li>{@code jsonFile("data/toolshop-search-terms.json").random()} — format: {@code
 *       [{"searchTerm":"pliers","minResults":4}, ...]}
 *   <li>Custom {@code Iterator<Map<String, Object>>} — {@code Stream.generate()} z {@code
 *       ThreadLocalRandom.current().nextInt(1, 4)} generujacy {@code {quantity: 1-3}}
 * </ul>
 *
 * <h2>Szablony body</h2>
 *
 * <ul>
 *   <li>{@code bodies/toolshop-cart-item.json}: {@code
 *       {"product_id":"#{productId}","quantity":#{quantity}}}
 * </ul>
 *
 * <h2>Gatling API</h2>
 *
 * <ul>
 *   <li>{@code FeederBuilder} — typ feedera CSV/JSON
 *   <li>{@code csv()}, {@code jsonFile()} — ladowanie danych z plikow
 *   <li>{@code .circular()}, {@code .random()} — strategie pobierania rekordow
 *   <li>{@code feed(feeder)} — wstrzykniecie danych do sesji
 *   <li>{@code ElFileBody("sciezka")} — szablon body z pliku z interpolacja EL
 *   <li>{@code repeat(3).on(feed(customFeeder)...)} — petla z feederem
 *   <li>{@code exec(session -> ...)} — debug sesji
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
package pl.training.toolshop.phase4;
