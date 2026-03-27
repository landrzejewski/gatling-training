/**
 * Etap 7 — Page Object Pattern: modularna architektura testu Gatling.
 *
 * <p>Cel: Organizacja kodu w warstwy — Config → Pages → Journeys → Scenarios → Populations →
 * Simulation. Kazdy poziom ma jasna odpowiedzialnosc, komponenty sa reuzywalne.
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>Base URL: {@code Config.BASE_URL} (default: {@code
 *       https://api.practicesoftwaretesting.com})
 *   <li>Produkty w {@code $.data[]}, ULID IDs, paginacja od 1
 *   <li>Auth: {@code POST /users/login} z JSON body (NIE formParam), zwraca {@code {access_token:
 *       "JWT"}}
 *   <li>Koszyk 2-step: {@code POST /carts} (201) → {@code POST /carts/{cartId}} (200)
 *   <li>Faktura: {@code POST /invoices} (201) z {@code Authorization: Bearer #{accessToken}}
 *   <li>Ulubione: {@code POST /favorites} (201), {@code DELETE /favorites/{id}} (204) — JWT Bearer
 * </ul>
 *
 * <h2>Struktura pakietow</h2>
 *
 * <pre>
 * etap7/
 *   ToolshopSimulation.java  — glowna klasa Simulation, wybor population, setUp
 *   SessionHelper.java       — initSession, setAuthenticated, debugSession
 *   config/
 *     Config.java            — System.getProperty() parametry
 *     Keys.java              — stale kluczy sesji (eliminacja hardkodowanych stringow)
 *   pages/                   — Page Objects (ChainBuilder): pojedyncze akcje HTTP
 *     AuthPage.java          — POST /users/login
 *     ProductsPage.java      — GET /products (list, listPage, details, related, search)
 *     CartPage.java          — POST /carts (create), POST /carts/{id} (addProduct), GET /carts/{id}
 *     InvoicePage.java       — POST /invoices (create), GET /invoices (list)
 *     FavoritesPage.java     — POST /favorites (add), GET /favorites (list), DELETE /favorites/{id} (remove)
 *   simulations/
 *     UserJourney.java       — kompletne sciezki uzytkownika skladajace sie z Page Objects
 *     TestScenario.java      — scenariusze z wagami (randomSwitch + Choice.WithWeight)
 *     TestPopulation.java    — profile wstrzykiwania (PopulationBuilder)
 * </pre>
 *
 * <h2>Konfiguracja (Config.java — System.getProperty)</h2>
 *
 * <ul>
 *   <li>{@code BASE_URL} (default: "https://api.practicesoftwaretesting.com")
 *   <li>{@code TEST_TYPE} (default: "INSTANT") — INSTANT / RAMP / CONSTANT_RATE / CLOSED /
 *       THROTTLED
 *   <li>{@code USERS} (default: "3")
 *   <li>{@code RAMP_DURATION} (default: "10") — sekundy
 *   <li>{@code DURATION} (default: "30") — sekundy
 *   <li>{@code MAX_RPS} (default: "10")
 *   <li>{@code PACE} (default: "10") — sekundy
 * </ul>
 *
 * <h2>Klucze sesji (Keys.java)</h2>
 *
 * ACCESS_TOKEN, PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_IDS, CART_ID, INVOICE_ID,
 * FAVORITE_ID, IS_AUTHENTICATED, CURRENT_PAGE, HAS_MORE_PAGES
 *
 * <h2>Pages — endpointy</h2>
 *
 * <ul>
 *   <li><b>AuthPage.login</b>: {@code POST /users/login}, body: {@code
 *       {"email":"customer@practicesoftwaretesting.com", "password":"welcome01"}}, status 200,
 *       saveAs(ACCESS_TOKEN) z {@code $.access_token}
 *   <li><b>ProductsPage.list</b>: {@code GET /products}, saveAs productId/productName z {@code
 *       $.data[0]}
 *   <li><b>ProductsPage.listPage(int)</b>: {@code GET /products?page=N}
 *   <li><b>ProductsPage.details</b>: {@code GET /products/#{productId}}, saveAs productPrice z
 *       {@code $.price}
 *   <li><b>ProductsPage.related</b>: {@code GET /products/#{productId}/related}
 *   <li><b>ProductsPage.search(String)</b>: {@code GET /products/search?q=term}
 *   <li><b>CartPage.create</b>: {@code POST /carts} → 201, saveAs cartId z {@code $.id}
 *   <li><b>CartPage.addProduct</b>: {@code POST /carts/#{cartId}}, body: {@code
 *       {"product_id":"#{productId}","quantity":1}} → 200
 *   <li><b>CartPage.getCart</b>: {@code GET /carts/#{cartId}} → 200
 *   <li><b>InvoicePage.create</b>: {@code POST /invoices}, header: {@code Authorization: Bearer
 *       #{accessToken}}, body: {@code
 *       {"cart_id":"#{cartId}","payment_method":"cash-on-delivery","payment_details":{},
 *       "billing_street":"123 Main St","billing_city":"New York","billing_state":"NY",
 *       "billing_country":"US","billing_postcode":"10001"}} → 201, saveAs invoiceId z {@code $.id}
 *   <li><b>InvoicePage.list</b>: {@code GET /invoices}, header: Bearer → 200
 *   <li><b>FavoritesPage.add</b>: {@code POST /favorites}, header: Bearer, body: {@code
 *       {"product_id":"#{productId}"}} → 201, saveAs favoriteId z {@code $.id}
 *   <li><b>FavoritesPage.list</b>: {@code GET /favorites}, header: Bearer → 200
 *   <li><b>FavoritesPage.remove</b>: {@code DELETE /favorites/#{favoriteId}}, header: Bearer → 204
 * </ul>
 *
 * <h2>User Journeys</h2>
 *
 * <ol>
 *   <li><b>browseStore</b> (60%): initSession → create cart → list → details → related →
 *       listPage(2), pace(PACE_DURATION), pause(1-3s)
 *   <li><b>abandonCart</b> (30%): initSession → create cart → list → details → addProduct
 *   <li><b>completePurchase</b> (10%): initSession → create cart → list → details → addProduct →
 *       exitBlockOnFail(login → create invoice)
 * </ol>
 *
 * <h2>Scenariusze (TestScenario)</h2>
 *
 * <ul>
 *   <li><b>defaultLoadTest</b>: during(TEST_DURATION).on(randomSwitch(...)) — 60/30/10
 *   <li><b>highPurchaseLoadTest</b>: 30/30/40
 * </ul>
 *
 * <h2>Populacje (TestPopulation)</h2>
 *
 * <ul>
 *   <li>instant: nothingFor(2) + atOnceUsers(USERS)
 *   <li>ramp: rampUsers(USERS).during(RAMP_DURATION)
 *   <li>constantRate: constantUsersPerSec(USERS).during(TEST_DURATION).randomized()
 *   <li>closed: constantConcurrentUsers + rampConcurrentUsers (highPurchaseLoadTest)
 *   <li>throttled: constantUsersPerSec(USERS*2) + throttle(reachRps, holdFor)
 * </ul>
 *
 * <h2>Assertions</h2>
 *
 * <ul>
 *   <li>{@code global().failedRequests().percent().lt(5.0)}
 *   <li>{@code global().responseTime().percentile(95.0).lt(3000)}
 * </ul>
 *
 * <h2>Dodatkowe</h2>
 *
 * <ul>
 *   <li>maxDuration: 2 minuty
 *   <li>User-Agent: Gatling/Etap7
 * </ul>
 */
package pl.training.toolshop.phase7;
