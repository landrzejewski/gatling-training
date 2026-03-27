/**
 * Etap 3 — Ekstrakcja danych z odpowiedzi (saveAs) i korelacja miedzy requestami.
 *
 * <p>Cel: Zapisywanie wartosci z JSON response do sesji uzytkownika (saveAs), uzycie tych wartosci
 * w kolejnych requestach przez Gatling Expression Language (#{zmienna}), walidacja isEL, debug
 * sesji przez exec(session -&gt; ...) i POST z StringBody.
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>Base URL: {@code https://api.practicesoftwaretesting.com}
 *   <li>Produkty w {@code $.data[]}, ID w formacie ULID (string)
 *   <li>Formularz kontaktowy: POST /messages zwraca {@code {id: ...}}
 * </ul>
 *
 * <h2>Endpointy</h2>
 *
 * <ol>
 *   <li>{@code GET /products} → status 200
 *       <ul>
 *         <li>{@code jsonPath("$.data[0].id").saveAs("productId")} — ULID
 *         <li>{@code jsonPath("$.data[0].name").saveAs("productName")}
 *       </ul>
 *   <li>{@code exec(session -> ...)} — debug: wypisanie productId i productName na stdout
 *   <li>{@code GET /products/#{productId}} → status 200
 *       <ul>
 *         <li>{@code jsonPath("$.name").isEL("#{productName}")} — walidacja korelacji
 *         <li>{@code jsonPath("$.price").saveAs("productPrice")}
 *       </ul>
 *   <li>{@code GET /products/#{productId}/related} → status 200
 *   <li>{@code POST /messages} → status 200, check: {@code jsonPath("$.id").exists()}
 *       <ul>
 *         <li>body (StringBody z EL): {@code
 *             {"first_name":"Test","last_name":"User","email":"test@example.com",
 *             "subject":"customer-service","message":"Pytanie o produkt #{productName}
 *             (#{productId}) w cenie #{productPrice}"}}
 *       </ul>
 * </ol>
 *
 * <h2>HTTP Protocol</h2>
 *
 * <ul>
 *   <li>{@code baseUrl}, {@code acceptHeader("application/json")}, {@code
 *       contentTypeHeader("application/json")}
 * </ul>
 *
 * <h2>Gatling API</h2>
 *
 * <ul>
 *   <li>{@code saveAs("nazwaZmiennej")} — zapis wartosci z JSONPath do sesji
 *   <li>{@code isEL("#{zmienna}")} — walidacja ze wartosc odpowiada zmiennej sesji
 *   <li>{@code StringBody("...")} — inline JSON body z interpolacja EL
 *   <li>{@code exec(session -> { ... return session; })} — debug / manipulacja sesji
 *   <li>{@code session.getString("klucz")} — odczyt stringa z sesji
 *   <li>{@code pause(1, 2)} — think time 1-2s
 * </ul>
 *
 * <h2>Zmienne sesji (korelacja)</h2>
 *
 * <ul>
 *   <li>{@code productId} — ULID produktu z GET /products
 *   <li>{@code productName} — nazwa produktu z GET /products
 *   <li>{@code productPrice} — cena z GET /products/{id}
 * </ul>
 *
 * <h2>Injection</h2>
 *
 * <ul>
 *   <li>{@code atOnceUsers(1)} — tryb debug do weryfikacji korelacji
 * </ul>
 */
package pl.training.toolshop.phase3;
