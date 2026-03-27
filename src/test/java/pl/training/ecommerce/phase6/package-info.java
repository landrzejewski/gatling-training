/**
 * Etap 6 — Zaawansowane sterowanie przeplywem: group, repeat, foreach, doIf, randomSwitch,
 * exitBlockOnFail, tryMax, asLongAs, during, pace.
 *
 * <p>Cel: Demonstracja wszystkich struktur sterujacych Gatling DSL — petle, warunki, losowy wybor
 * sciezki, ochrona przed bledami, petla warunkowa i czasowa.
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>Base URL: {@code https://api-ecomm.gatling.io}
 *   <li>Produkty w {@code $.products[]}, numeryczne ID, paginacja od 0
 *   <li>Sesja: {@code GET /session} → {@code {sessionId: "uuid"}}
 *   <li>Koszyk: {@code POST /cart} z sessionId
 * </ul>
 *
 * <h2>Endpointy</h2>
 *
 * <ul>
 *   <li>{@code GET /session} → {@code jsonPath("$.sessionId").saveAs("sessionId")}
 *   <li>{@code GET /products?page=#{pageIndex}} → 200, {@code
 *       jsonPath("$.products[*].id").findAll().saveAs("productIds")} — lista ID
 *   <li>{@code GET /products/#{currentId}} → 200, {@code saveAs("lastPrice")} z {@code $.price}
 *   <li>{@code GET /products?page=0} → 200 (przegladanie)
 *   <li>{@code GET /products?search=shirt} → 200 (wyszukiwanie)
 *   <li>{@code POST /cart} → 200, body: {@code
 *       {"sessionId":"#{sessionId}","cart":[{"productId":0,"quantity":1}]}}
 *   <li>{@code GET /products/0} → 200 (retry)
 *   <li>{@code GET /products?page=#{currentPage}} → 200 (asLongAs)
 *   <li>{@code GET /products/1} → 200 (during + pace)
 * </ul>
 *
 * <h2>Struktury sterujace (grupy)</h2>
 *
 * <ol>
 *   <li><b>Inicjalizacja</b> — {@code group("Inicjalizacja").on(...)}: GET /session, ustawienie
 *       lastPrice=0.0
 *   <li><b>Przegladanie stron</b> — {@code repeat(3, "pageIndex").on(...)}: iteracja po stronach,
 *       {@code findAll().saveAs("productIds")}
 *   <li><b>Iteracja po produktach</b> — {@code foreach("#{selectedIds}", "currentId").on(...)}:
 *       pobranie szczegolow pierwszych 3 produktow z listy (session.getList(), subList())
 *   <li><b>Warunkowa logika</b> — {@code doIf(session -> lastPrice > 0)}, {@code doIfOrElse(session
 *       -> lastPrice > 50.0).then(...).orElse(...)}: rozne sciezki
 *   <li><b>Losowy wybor</b> — {@code randomSwitch().on(Choice.WithWeight(70.0, ...), ...)}: 70%
 *       przegladanie, 30% wyszukiwanie
 *   <li><b>Ochrona bledow</b> — {@code exitBlockOnFail().on(...)}: POST /cart z przerwaniem przy
 *       bledzie
 *   <li><b>Ponawianie</b> — {@code tryMax(2).on(...).exitHereIfFailed()}: GET /products/0 z retry
 *       do 2 razy
 *   <li><b>Petla warunkowa</b> — {@code asLongAs(session -> currentPage < 3).on(...)}: iteracja z
 *       recznym inkrementowaniem zmiennej sesji (startPage=0)
 *   <li><b>Petla czasowa z pace</b> — {@code during(5s).on(pace(2s).exec(...))}: powtarzanie przez
 *       5s z wymuszonym minimalnym czasem 2s na iteracje
 * </ol>
 *
 * <h2>Gatling API</h2>
 *
 * <ul>
 *   <li>{@code group("nazwa")} — grupowanie requestow w raportach
 *   <li>{@code repeat(N, "counterVar")} — petla z licznikiem
 *   <li>{@code foreach("#{lista}", "element")} — iteracja po liscie sesji
 *   <li>{@code doIf(condition).then(...)}, {@code doIfOrElse(...).then(...).orElse(...)}
 *   <li>{@code randomSwitch().on(new Choice.WithWeight(waga, exec(...)), ...)}
 *   <li>{@code exitBlockOnFail().on(...)} — przerwanie bloku przy bledzie
 *   <li>{@code tryMax(N).on(...).exitHereIfFailed()} — retry z limitem
 *   <li>{@code asLongAs(condition).on(...)} — petla warunkowa
 *   <li>{@code during(Duration).on(pace(Duration).exec(...))} — petla czasowa z pace
 *   <li>{@code jsonPath("$.products[*].id").findAll()} — ekstrakcja listy wartosci
 *   <li>{@code session.getList()}, {@code session.getDouble()}, {@code session.getInt()}
 * </ul>
 *
 * <h2>Injection</h2>
 *
 * <ul>
 *   <li>{@code atOnceUsers(1)} — tryb debug
 * </ul>
 *
 * <h2>Assertions</h2>
 *
 * <ul>
 *   <li>{@code global().failedRequests().percent().lt(5.0)}
 * </ul>
 */
package pl.training.ecommerce.phase6;
