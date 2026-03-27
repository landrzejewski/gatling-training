/**
 * Etap 3 — Ekstrakcja danych z odpowiedzi (saveAs) i korelacja miedzy requestami.
 *
 * <p>Cel: Zapisywanie wartosci z JSON response do sesji uzytkownika (saveAs), uzycie tych wartosci
 * w kolejnych requestach przez Gatling Expression Language (#{zmienna}), walidacja isEL, debug
 * sesji i POST z StringBody z korelacja sessionId + productId.
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>Base URL: {@code https://api-ecomm.gatling.io}
 *   <li>Produkty w {@code $.products[]}, numeryczne ID (int)
 *   <li>Sesja: {@code GET /session} zwraca {@code {sessionId: "uuid"}}
 *   <li>Koszyk: {@code POST /cart} z sessionId i produktami, zwraca {@code {message: "Cart
 *       updated"}}
 * </ul>
 *
 * <h2>Endpointy</h2>
 *
 * <ol>
 *   <li>{@code GET /session} → status 200, {@code jsonPath("$.sessionId").saveAs("sessionId")}
 *   <li>{@code exec(session -> ...)} — debug: wypisanie sessionId na stdout
 *   <li>{@code GET /products} → status 200
 *       <ul>
 *         <li>{@code jsonPath("$.products[0].id").saveAs("productId")}
 *         <li>{@code jsonPath("$.products[0].name").saveAs("productName")}
 *       </ul>
 *   <li>{@code GET /products/#{productId}} → status 200
 *       <ul>
 *         <li>{@code jsonPath("$.name").isEL("#{productName}")} — walidacja korelacji
 *         <li>{@code jsonPath("$.price").saveAs("productPrice")}
 *       </ul>
 *   <li>{@code POST /cart} → status 200, check: {@code jsonPath("$.message").is("Cart updated")}
 *       <ul>
 *         <li>body (StringBody z EL): {@code
 *             {"sessionId":"#{sessionId}","cart":[{"productId":#{productId},"quantity":1}]}}
 *       </ul>
 * </ol>
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
 *   <li>{@code sessionId} — z GET /session
 *   <li>{@code productId} — numeryczne ID z GET /products, uzyte w URL i POST body
 *   <li>{@code productName} — nazwa produktu, uzyta w isEL walidacji
 *   <li>{@code productPrice} — cena z GET /products/{id}
 * </ul>
 *
 * <h2>Injection</h2>
 *
 * <ul>
 *   <li>{@code atOnceUsers(1)} — tryb debug do weryfikacji korelacji
 * </ul>
 */
package pl.training.ecommerce.phase3;
