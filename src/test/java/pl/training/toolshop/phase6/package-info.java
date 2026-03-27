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
 *   <li>Base URL: {@code https://api.practicesoftwaretesting.com}
 *   <li>Produkty w {@code $.data[]}, ULID IDs, paginacja od strony 1
 *   <li>Koszyk 2-step: {@code POST /carts} (201) → {@code POST /carts/{cartId}} (200)
 * </ul>
 *
 * <h2>Endpointy</h2>
 *
 * <ul>
 *   <li>{@code POST /carts} → 201, {@code saveAs("cartId")} z {@code $.id}
 *   <li>{@code GET /products?page=#{currentPageNum}} → 200, {@code
 *       jsonPath("$.data[*].id").findAll().saveAs("productIds")} — lista ULID
 *   <li>{@code GET /products/#{currentId}} → 200, {@code saveAs("lastPrice")} z {@code $.price}
 *   <li>{@code POST /carts/#{cartId}} → 200, body: {@code
 *       {"product_id":"#{currentId}","quantity":1}}, check: {@code $.result == "item added or
 *       updated"}
 *   <li>{@code GET /categories} → 200
 *   <li>{@code GET /products/search?q=pliers} → 200
 *   <li>{@code GET /categories/tree} → 200
 *   <li>{@code POST /messages} → 200, body: {@code {"first_name":"Test","last_name":"User",
 *       "email":"test@example.com","subject":"customer-service","message":"Pytanie testowe"}},
 *       check: {@code jsonPath("$.id").exists()}
 * </ul>
 *
 * <h2>Struktury sterujace (grupy)</h2>
 *
 * <ol>
 *   <li><b>Inicjalizacja</b> — {@code group("Inicjalizacja").on(...)}: POST /carts, ustawienie
 *       lastPrice=0.0
 *   <li><b>Przegladanie stron</b> — {@code repeat(3, "pageIndex").on(...)}: iteracja po stronach
 *       (pageIndex+1), {@code findAll().saveAs("productIds")}
 *   <li><b>Iteracja po produktach</b> — {@code foreach("#{selectedIds}", "currentId").on(...)}:
 *       pobranie szczegolow pierwszych 3 produktow z listy (session.getList(), subList())
 *   <li><b>Warunkowa logika</b> — {@code doIf(session -> lastPrice > 0)}, {@code doIfOrElse(session
 *       -> lastPrice > 50.0).then(...).orElse(...)}: warunkowe dodanie do koszyka
 *   <li><b>Losowy wybor</b> — {@code randomSwitch().on(Choice.WithWeight(40.0, ...), ...)}: 40%
 *       kategorie, 30% wyszukiwanie, 30% drzewo kategorii
 *   <li><b>Ochrona bledow</b> — {@code exitBlockOnFail().on(...)}: POST /messages z przerwaniem
 *       przy bledzie
 *   <li><b>Ponawianie</b> — {@code tryMax(2).on(...).exitHereIfFailed()}: GET /products z retry do
 *       2 razy
 *   <li><b>Petla warunkowa</b> — {@code asLongAs(session -> currentPage <= 3).on(...)}: iteracja po
 *       stronach z recznym inkrementowaniem zmiennej sesji
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
 *   <li>{@code jsonPath("$.data[*].id").findAll()} — ekstrakcja listy wartosci
 *   <li>{@code session.getList()}, {@code session.getDouble()}, {@code session.getInt()}
 *   <li>{@code session.set("key", value)} — ustawianie zmiennych sesji
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
package pl.training.toolshop.phase6;
