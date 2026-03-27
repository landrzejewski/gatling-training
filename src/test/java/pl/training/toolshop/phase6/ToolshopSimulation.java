package pl.training.toolshop.phase6;

import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ToolshopSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol =
        http.baseUrl("https://api.practicesoftwaretesting.com")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private final ScenarioBuilder scn =
        scenario("Etap 6 - Sterowanie przeplywem")

            // group() — organizuje requesty w raportach pod wspolna nazwa
            // Toolshop: koszyk wymaga POST /carts (201) do utworzenia
            .group("Inicjalizacja")
            .on(
                exec(http("Utworz koszyk")
                    .post("/carts")
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("cartId")))
                .exec(session -> session.set("lastPrice", 0.0)))

            // repeat(N) — powtarza blok N razy; licznik dostepny jako zmienna
            // Toolshop: strony od 1, wiec uzywamy pageIndex+1
            .group("Przegladanie stron")
            .on(
                repeat(3, "pageIndex")
                .on(
                    exec(session -> {
                        int page = session.getInt("pageIndex") + 1;
                        return session.set("currentPageNum", page);
                    })
                    .exec(http("Strona #{currentPageNum}")
                        .get("/products?page=#{currentPageNum}")
                        .check(status().is(200))
                        // jsonPath(...).findAll() — ekstrakcja wszystkich ULID jako lista
                        .check(jsonPath("$.data[*].id").findAll().saveAs("productIds")))
                    .pause(1)))

            // foreach() — iteruje po liscie z sesji
            .group("Iteracja po produktach")
            .on(
                // Wez pierwsze 3 ULID z listy
                exec(session -> {
                    List<Object> allIds = session.getList("productIds");
                    List<Object> firstThree = allIds.subList(0, Math.min(3, allIds.size()));
                    return session.set("selectedIds", firstThree);
                })
                .foreach("#{selectedIds}", "currentId")
                .on(
                    exec(http("Produkt #{currentId}")
                        .get("/products/#{currentId}")
                        .check(status().is(200))
                        .check(jsonPath("$.price").saveAs("lastPrice")))
                    .pause(1)))

            // doIf() / doIfOrElse() — warunkowe wykonanie bloku
            .group("Warunkowa logika")
            .on(
                doIf(session -> session.getDouble("lastPrice") > 0)
                .then(
                    exec(session -> {
                        System.out.println("Ostatnia cena: " + session.getDouble("lastPrice"));
                        return session;
                    }))
                .doIfOrElse(session -> session.getDouble("lastPrice") > 50.0)
                .then(
                    exec(session -> {
                        System.out.println("Produkt drogi — pomijam koszyk");
                        return session.set("action", "skip");
                    }))
                .orElse(
                    // Toolshop: dodanie do koszyka przez POST /carts/{cartId}
                    exec(session -> {
                        System.out.println("Produkt tani — dodaje do koszyka");
                        return session.set("action", "buy");
                    })
                    .exec(http("Dodaj do koszyka")
                        .post("/carts/#{cartId}")
                        .body(StringBody(
                            """
                            {"product_id":"#{currentId}","quantity":1}
                            """))
                        .check(status().is(200))
                        .check(jsonPath("$.result").is("item added or updated")))))

            // randomSwitch() z Choice.WithWeight — losowy wybor sciezki
            .group("Losowy wybor")
            .on(
                randomSwitch()
                .on(
                    new Choice.WithWeight(40.0,
                        exec(http("Kategorie")
                            .get("/categories")
                            .check(status().is(200)))),
                    new Choice.WithWeight(30.0,
                        exec(http("Wyszukiwanie pliers")
                            .get("/products/search?q=pliers")
                            .check(status().is(200)))),
                    new Choice.WithWeight(30.0,
                        exec(http("Drzewo kategorii")
                            .get("/categories/tree")
                            .check(status().is(200))))))

            // exitBlockOnFail() — przerywa blok przy pierwszym bledzie
            .group("Kontakt z ochrona bledow")
            .on(
                exitBlockOnFail()
                .on(
                    exec(http("Formularz kontaktowy")
                        .post("/messages")
                        .body(StringBody(
                            """
                            {"first_name":"Test","last_name":"User","email":"test@example.com","subject":"customer-service","message":"Pytanie testowe"}
                            """))
                        .check(status().is(200))
                        .check(jsonPath("$.id").exists()))))

            // tryMax(N) — ponawia blok do N razy w razie niepowodzenia
            .group("Ponawianie")
            .on(
                tryMax(2)
                .on(
                    exec(http("Produkt z retry")
                        .get("/products/#{currentId}")
                        .check(status().is(200))))
                .exitHereIfFailed())

            // asLongAs() — petla warunkowa (powtarza dopoki warunek true)
            // Toolshop: strony od 1
            .group("Petla warunkowa")
            .on(
                exec(session -> session.set("currentPage", 1))
                .asLongAs(session -> session.getInt("currentPage") <= 3)
                .on(
                    exec(http("Strona asLongAs #{currentPage}")
                        .get("/products?page=#{currentPage}")
                        .check(status().is(200)))
                    .exec(session -> {
                        int next = session.getInt("currentPage") + 1;
                        return session.set("currentPage", next);
                    })
                    .pause(1)))

            // during() — petla czasowa (powtarza przez zadany czas)
            // pace() — wymusza minimalny czas iteracji
            .group("Petla czasowa z pace")
            .on(
                during(Duration.ofSeconds(5))
                .on(
                    pace(Duration.ofSeconds(2))
                    .exec(http("Produkt w petli czasowej")
                        .get("/products")
                        .check(status().is(200)))));

    {
        setUp(scn.injectOpen(atOnceUsers(1)))
            .protocols(httpProtocol)
            .assertions(global().failedRequests().percent().lt(5.0));
    }
}
