/**
 * VideoGames — Kompletna symulacja produkcyjna CRUD API gier wideo z Page Object Pattern, 4
 * journey, konfigurowalnymi wagami, feederami CSV i Bearer auth.
 *
 * <p>Cel: Samodzielny przyklad produkcyjnej symulacji Gatling testujacy REST API do zarzadzania
 * grami wideo — pelny cykl CRUD (Create, Read, Update, Delete) z autentykacja JWT Bearer, feederem
 * CSV, Page Object Pattern i konfigurowalnymi wagami scenariuszy.
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>Base URL: {@code Config.BASE_URL} (default: {@code https://videogamedb.uk/api})
 *   <li>Lista gier: tablica JSON {@code $[0].id}, numeryczne ID (1-10)
 *   <li>Auth: {@code POST /authenticate} z JSON body, zwraca {@code {token: "JWT"}}
 *   <li>Authorization header: {@code Bearer #{jwtToken}} (z prefixem Bearer)
 *   <li>API jest mockowane — POST/PUT/DELETE nie mutuja danych, gry 1-10 zawsze dostepne
 * </ul>
 *
 * <h2>Struktura pakietow</h2>
 *
 * <pre>
 * videogames/
 *   VideoGameSimulation.java     — glowna Simulation z before/after, assertions, population switch
 *   VideoGameFullTemplateTest.java — template z TODO do cwiczen
 *   SessionHelper.java           — initSession, setAuthenticated, debugSession
 *   config/
 *     Config.java                — System.getProperty() z 4 wagami journey + BASE_URL + pauzy
 *     Keys.java                  — stale kluczy sesji (JWT_TOKEN, GAME_ID, CREATED_GAME_ID, ...)
 *   pages/
 *     AuthPage.java              — POST /authenticate
 *     VideoGamePage.java         — list, details, create, update, delete + CSV feeder
 *   simulations/
 *     UserJourney.java           — 4 journey: browse, createAndView, fullCrud, searchByCategory
 *     TestScenario.java          — defaultLoadTest (4 wagi), highCrudLoadTest
 *     TestPopulation.java        — 7 profili wstrzykiwania
 * </pre>
 *
 * <h2>Konfiguracja (Config.java — System.getProperty)</h2>
 *
 * <ul>
 *   <li>{@code BASE_URL} (default: "https://videogamedb.uk/api")
 *   <li>{@code TEST_TYPE} (default: "INSTANT") —
 *       INSTANT/RAMP/CONSTANT_RATE/STAIRCASE/STRESS_PEAK/CLOSED/THROTTLED
 *   <li>{@code USERS} (default: "5"), {@code RAMP_DURATION} (default: "10"), {@code DURATION}
 *       (default: "30"), {@code MAX_RPS} (default: "10"), {@code PACE} (default: "10")
 *   <li><b>Wagi scenariuszy:</b> {@code BROWSE_WEIGHT} (40.0), {@code CREATE_VIEW_WEIGHT} (25.0),
 *       {@code FULL_CRUD_WEIGHT} (20.0), {@code SEARCH_WEIGHT} (15.0)
 *   <li><b>Pauzy:</b> {@code MIN_PAUSE} (1s), {@code MAX_PAUSE} (3s)
 * </ul>
 *
 * <h2>Klucze sesji (Keys.java)</h2>
 *
 * JWT_TOKEN ("jwtToken"), GAME_ID ("gameId"), GAME_NAME ("name"), CREATED_GAME_ID
 * ("createdGameId"), CURRENT_CATEGORY ("currentCategory"), IS_AUTHENTICATED ("isAuthenticated")
 *
 * <h2>Dane logowania</h2>
 *
 * <ul>
 *   <li>username: {@code admin}, password: {@code admin}
 * </ul>
 *
 * <h2>Endpointy — Pages</h2>
 *
 * <ul>
 *   <li><b>AuthPage.login</b>: {@code POST /authenticate}, body: {@code
 *       {"username":"admin","password":"admin"}} → 200, saveAs(JWT_TOKEN) z {@code $.token},
 *       ustawia IS_AUTHENTICATED=true
 *   <li><b>VideoGamePage.list</b>: {@code GET /videogame} → 200, check: header Content-Type =
 *       application/json, check: {@code jsonPath("$[0].id").ofInt().gt(0)}
 *   <li><b>VideoGamePage.details</b>: {@code GET /videogame/#{gameId}} → 200, feed z CSV ({@code
 *       data/games.csv}, random), check: {@code jsonPath("$.name").isEL("#{name}")}, check: {@code
 *       jmesPath("category").saveAs(CURRENT_CATEGORY)}
 *   <li><b>VideoGamePage.create</b>: {@code POST /videogame}, feed z CSV, header: {@code
 *       Authorization: Bearer #{jwtToken}}, body: {@code ElFileBody("bodies/newGame.json")},
 *       saveAs(CREATED_GAME_ID) z {@code $.id}
 *   <li><b>VideoGamePage.update</b>: {@code PUT /videogame/#{gameId}}, header: {@code
 *       Authorization: Bearer #{jwtToken}}, body (StringBody): {@code
 *       {"id":#{gameId},"category":"#{category}", "name":"#{name} - Updated","rating":"#{rating}",
 *       "releaseDate":"#{releaseDate}","reviewScore":99}}, check: {@code jmesPath("name") ==
 *       "#{name} - Updated"}
 *   <li><b>VideoGamePage.delete</b>: {@code DELETE /videogame/#{gameId}}, header: {@code
 *       Authorization: Bearer #{jwtToken}}, check: {@code bodyString().is("Video game deleted")}
 * </ul>
 *
 * <h2>Dane testowe</h2>
 *
 * <ul>
 *   <li>{@code data/games.csv} (feeder random) — kolumny: gameId, name, releaseDate, reviewScore,
 *       category, rating
 *       <ul>
 *         <li>1, Resident Evil 4, 2005-10-01 23:59:59, 85, Shooter, Universal
 *         <li>2, Gran Turismo 3, 2001-03-10 23:59:59, 91, Driving, Universal
 *         <li>3, Tetris, 1984-06-25 23:59:59, 88, Puzzle, Universal
 *         <li>4, Super Mario 64, 1996-10-20 23:59:59, 90, Platform, Universal
 *       </ul>
 *   <li>{@code bodies/newGame.json} (ElFileBody): {@code
 *       {"id":#{gameId},"category":"#{category}","name":"#{name}",
 *       "rating":"#{rating}","releaseDate":"#{releaseDate}","reviewScore":#{reviewScore}}}
 * </ul>
 *
 * <h2>User Journeys (4)</h2>
 *
 * <ol>
 *   <li><b>browseGames</b> (40%): pace → initSession → list → details → list (read-only, bez auth)
 *   <li><b>createAndView</b> (25%): pace → initSession → exitBlockOnFail(login → create → details →
 *       list)
 *   <li><b>fullCrud</b> (20%): pace → initSession → list → exitBlockOnFail(login → create → details
 *       → update → delete)
 *   <li><b>searchByCategory</b> (15%): pace → initSession → list → details → details (nowy rekord z
 *       feedera) → list
 * </ol>
 *
 * <h2>Scenariusze (TestScenario)</h2>
 *
 * <ul>
 *   <li><b>defaultLoadTest</b>: during(TEST_DURATION).on(randomSwitch(4 wagi z Config))
 *   <li><b>highCrudLoadTest</b>: 20/20/40/20
 * </ul>
 *
 * <h2>Populacje (TestPopulation — 7 profili)</h2>
 *
 * <ul>
 *   <li>instant: nothingFor(2) + atOnceUsers(USERS)
 *   <li>ramp: rampUsers(USERS).during(RAMP_DURATION)
 *   <li>constantRate: constantUsersPerSec(USERS).during(TEST_DURATION).randomized()
 *   <li>staircase: incrementUsersPerSec(2.0).times(4).eachLevelLasting(10s)
 *       .separatedByRampsLasting(5s).startingFrom(1.0)
 *   <li>stressPeak: stressPeakUsers(USERS*5).during(30s)
 *   <li>closed: constantConcurrentUsers + rampConcurrentUsers
 *   <li>throttled: constantUsersPerSec(USERS*2) + throttle(reachRps, holdFor)
 * </ul>
 *
 * <h2>Assertions</h2>
 *
 * <ul>
 *   <li>{@code global().failedRequests().percent().lt(5.0)}
 *   <li>{@code global().responseTime().percentile(95.0).lt(3000)}
 *   <li>{@code global().responseTime().percentile(99.0).lt(5000)}
 *   <li>{@code details("Przegladanie gier", "Lista gier").responseTime().percentile(95.0).lt(2000)}
 *   <li>{@code forAll().failedRequests().percent().lt(20.0)}
 * </ul>
 *
 * <h2>Dodatkowe</h2>
 *
 * <ul>
 *   <li>maxDuration: 5 minut
 *   <li>User-Agent: Gatling/Etap8-Production
 *   <li>before()/after() loguja parametry (Base URL, Test Type, Users, Duration, Scenario Weights)
 *   <li>{@code jmesPath()} uzywany obok {@code jsonPath()} do ekstrakcji danych
 * </ul>
 */
package pl.training.videogames;
