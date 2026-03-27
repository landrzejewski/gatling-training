/**
 * Etap 7 — Page Object Pattern: modularna architektura testu Gatling.
 *
 * <p>Cel: Organizacja kodu w warstwy — Config → Pages → Journeys → Scenarios → Populations →
 * Simulation. Kazdy poziom ma jasna odpowiedzialnosc, komponenty sa reuzywalne.
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>Base URL: {@code https://api-ecomm.gatling.io}
 *   <li>Produkty w {@code $.products[]}, numeryczne ID (int)
 *   <li>Sesja: {@code GET /session} → {@code {sessionId: "uuid"}}
 *   <li>Auth: {@code POST /login} z formParam (username/password), zwraca {@code {accessToken:
 *       "token"}}
 *   <li>Koszyk: {@code POST /cart} z sessionId i tablica cart
 *   <li>Checkout: {@code POST /checkout} z header {@code Authorization: #{accessToken}} (bez
 *       prefixu Bearer)
 * </ul>
 *
 * <h2>Struktura pakietow</h2>
 *
 * <pre>
 * etap7/
 *   EcommSimulation.java   — glowna klasa Simulation, wybor population, setUp
 *   SessionHelper.java     — initSession, setAuthenticated, debugSession
 *   config/
 *     Config.java          — System.getProperty() parametry
 *     Keys.java            — stale kluczy sesji
 *   pages/                 — Page Objects (ChainBuilder): pojedyncze akcje HTTP
 *     SessionPage.java     — GET /session
 *     AuthPage.java        — POST /login (formParam!)
 *     ProductsPage.java    — GET /products (list, listPage, details, search, searchFromSession)
 *     CartPage.java        — POST /cart (addProduct + overload z parametrami)
 *     CheckoutPage.java    — POST /checkout (z Authorization header)
 *   simulations/
 *     UserJourney.java     — kompletne sciezki uzytkownika
 *     TestScenario.java    — scenariusze z wagami (randomSwitch + Choice.WithWeight)
 *     TestPopulation.java  — profile wstrzykiwania (PopulationBuilder)
 * </pre>
 *
 * <h2>Konfiguracja (Config.java — System.getProperty)</h2>
 *
 * <ul>
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
 * SESSION_ID, ACCESS_TOKEN, PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_IDS, IS_AUTHENTICATED
 *
 * <h2>Pages — endpointy</h2>
 *
 * <ul>
 *   <li><b>SessionPage.create</b>: {@code GET /session} → 200, saveAs(SESSION_ID) z {@code
 *       $.sessionId}
 *   <li><b>AuthPage.login</b>: {@code POST /login}, {@code formParam("username", "admin")}, {@code
 *       formParam("password", "gatling")} → 200, saveAs(ACCESS_TOKEN) z {@code $.accessToken}
 *   <li><b>ProductsPage.list</b>: {@code GET /products}, saveAs productId/productName z {@code
 *       $.products[0]}
 *   <li><b>ProductsPage.listPage(int)</b>: {@code GET /products?page=N}
 *   <li><b>ProductsPage.details</b>: {@code GET /products/#{productId}}, saveAs productPrice z
 *       {@code $.price}
 *   <li><b>ProductsPage.search(String)</b>: {@code GET /products?search=term}
 *   <li><b>ProductsPage.searchFromSession</b>: {@code GET /products?search=#{searchTerm}}
 *   <li><b>CartPage.addProduct</b>: {@code POST /cart}, body: {@code
 *       {"sessionId":"#{sessionId}","cart":[{"productId":#{productId},"quantity":1}]}} → 200,
 *       check: {@code $.message == "Cart updated"}
 *   <li><b>CartPage.addProduct(int, int)</b>: wariant z parametrami productId, quantity
 *   <li><b>CheckoutPage.complete</b>: {@code POST /checkout}, header: {@code Authorization:
 *       #{accessToken}} (BEZ "Bearer"), body: {@code
 *       {"sessionId":"#{sessionId}","cart":[{"productId":#{productId},"quantity":1}]}} → 200,
 *       check: {@code $.message == "Checkout completed"}
 * </ul>
 *
 * <h2>User Journeys</h2>
 *
 * <ol>
 *   <li><b>browseStore</b> (60%): initSession → create session → list → details → listPage(1) →
 *       search("shirt"), pace(PACE_DURATION), pause(1-3s)
 *   <li><b>abandonCart</b> (30%): initSession → create session → list → details → addProduct
 *   <li><b>completePurchase</b> (10%): initSession → create session → list → details → addProduct →
 *       login → checkout
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
package pl.training.ecommerce.phase7;
