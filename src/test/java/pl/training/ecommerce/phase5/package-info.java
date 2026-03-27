/**
 * Etap 5 — Profile obciazenia: Open model, Closed model, throttling i parametryzacja z CLI.
 *
 * <p>Cel: Demonstracja wszystkich glownych profili wstrzykiwania uzytkownikow Gatling (Open i
 * Closed), throttling RPS, lifecycle hooks (before/after), parametryzacja testu przez
 * System.getProperty().
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>Base URL: {@code https://api-ecomm.gatling.io}
 *   <li>Produkty w {@code $.products[]}, numeryczne ID
 * </ul>
 *
 * <h2>Endpointy</h2>
 *
 * <ol>
 *   <li>{@code GET /products} → status 200
 *   <li>{@code GET /products/0} → status 200
 * </ol>
 *
 * <h2>Konfiguracja (System.getProperty)</h2>
 *
 * <ul>
 *   <li>{@code TEST_TYPE} (default: "INSTANT") — wybor profilu obciazenia
 *   <li>{@code USERS} (default: "3") — liczba uzytkownikow
 *   <li>{@code DURATION} (default: "30") — czas trwania testu w sekundach
 *   <li>{@code MAX_RPS} (default: "10") — maksymalna liczba requests per second
 * </ul>
 *
 * <h2>Profile obciazenia (switch na TEST_TYPE)</h2>
 *
 * <ul>
 *   <li><b>INSTANT</b>: {@code atOnceUsers(USERS)} — natychmiastowe wstrzykniecie
 *   <li><b>RAMP</b>: {@code rampUsers(USERS).during(DURATION)} — liniowy wzrost
 *   <li><b>CONSTANT_RATE</b>: {@code constantUsersPerSec(USERS).during(DURATION).randomized()}
 *   <li><b>RAMP_RATE</b>: {@code rampUsersPerSec(1).to(USERS).during(DURATION).randomized()}
 *   <li><b>STAIRCASE</b>: {@code incrementUsersPerSec(2.0).times(4).eachLevelLasting(10s)
 *       .separatedByRampsLasting(5s).startingFrom(1.0)}
 *   <li><b>STRESS_PEAK</b>: {@code stressPeakUsers(USERS * 5).during(DURATION)} — krzywa S
 *   <li><b>CLOSED</b>: {@code constantConcurrentUsers(USERS).during(15s)}, {@code
 *       rampConcurrentUsers(USERS).to(USERS*2).during(15s)} — model zamkniety (injectClosed)
 *   <li><b>THROTTLED</b>: {@code constantUsersPerSec(USERS*2).during(DURATION)} + {@code
 *       throttle(reachRps(MAX_RPS).in(10s), holdFor(20s))}
 *   <li><b>COMPLEX</b>: wieloetapowy — {@code nothingFor(5s)}, {@code atOnceUsers(1)}, {@code
 *       rampUsers}, {@code constantUsersPerSec}, {@code rampUsersPerSec}
 * </ul>
 *
 * <h2>Lifecycle hooks</h2>
 *
 * <ul>
 *   <li>{@code before()} — wypisanie parametrow testu przed startem
 *   <li>{@code after()} — komunikat o zakonczeniu
 * </ul>
 *
 * <h2>Gatling API</h2>
 *
 * <ul>
 *   <li>{@code injectOpen()} / {@code injectClosed()} — dwa modele wstrzykiwania
 *   <li>{@code throttle(reachRps(N).in(D), holdFor(D))} — limitowanie RPS
 *   <li>{@code maxDuration(2 minutes)} — bezpiecznik czasowy
 *   <li>{@code nothingFor(Duration)} — opoznienie poczatkowe
 * </ul>
 *
 * <h2>Assertions</h2>
 *
 * <ul>
 *   <li>{@code global().failedRequests().percent().lt(5.0)}
 *   <li>{@code global().responseTime().percentile(95.0).lt(3000)}
 * </ul>
 */
package pl.training.ecommerce.phase5;
