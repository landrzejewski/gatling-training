package pl.training.ecommerce.phase6;

import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class EcommSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http.baseUrl("https://api-ecomm.gatling.io")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private final ScenarioBuilder scn =
            scenario("control flow")

                    // group() — organizuje requesty w raportach pod wspolna nazwa
                    .group("session")
                    .on(
                            exec(http("create session")
                                    .get("/session")
                                    .check(jsonPath("$.sessionId").saveAs("sessionId")))
                                    .exec(session -> session.set("lastPrice", 0.0))
                    )
                    // repeat(N) — powtarza blok N razy; licznik dostepny jako zmienna
                    .group("paging")
                    .on(
                            repeat(3, "pageIndex")
                                    .on(
                                            exec(http("Strona #{pageIndex}")
                                                    .get("/products?page=#{pageIndex}")
                                                    .check(status().is(200))
                                                    // jsonPath(...).findAll() — ekstrakcja wszystkich trafien jako lista
                                                    .check(jsonPath("$.products[*].id").findAll().saveAs("productIds"))
                                            )
                                                    .pause(1))
                    )

                    // foreach() — iteruje po liscie z sesji
                    .group("iteration")
                    .on(
                            // Weź pierwsze 3 ID z listy
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
                                                    .pause(1)
                                    )
                    )

                    // doIf() — warunkowe wykonanie bloku
                    .group("conditionals")
                    .on(
                            // doIf sprawdza warunek sesji — tu: czy cena > 0
                            doIf(session -> session.getDouble("lastPrice") > 0)
                                    .then(
                                            exec(
                                                    session -> {
                                                        System.out.println("Ostatnia cena: " + session.getDouble("lastPrice"));
                                                        return session;
                                                    }))
                                    // doIfOrElse — dwie sciezki: then/orElse
                                    .doIfOrElse(session -> session.getDouble("lastPrice") > 50.0)
                                    .then(
                                            exec(
                                                    session -> {
                                                        System.out.println("Produkt drogi — pomijam koszyk");
                                                        return session.set("action", "skip");
                                                    }))
                                    .orElse(
                                            exec(
                                                    session -> {
                                                        System.out.println("Produkt tani — dodaje do koszyka");
                                                        return session.set("action", "buy");
                                                    })))

                    // randomSwitch() z Choice.WithWeight — losowy wybor sciezki
                    .group("random")
                    .on(
                            randomSwitch()
                                    .on(
                                            new Choice.WithWeight(
                                                    70.0,
                                                    exec(
                                                            http("Przegladanie produktow")
                                                                    .get("/products?page=0")
                                                                    .check(status().is(200)))),
                                            new Choice.WithWeight(
                                                    30.0,
                                                    exec(
                                                            http("Wyszukiwanie")
                                                                    .get("/products?search=shirt")
                                                                    .check(status().is(200))))))

                    // exitBlockOnFail() — przerywa blok przy pierwszym bledzie
                    .group("errors")
                    .on(
                            exitBlockOnFail()
                                    .on(
                                            exec(
                                                    http("Dodaj do koszyka")
                                                            .post("/cart")
                                                            .body(
                                                                    StringBody(
                                                                            """
                                                                                    {"sessionId":"#{sessionId}","cart":[{"productId":0,"quantity":1}]}
                                                                                    """))
                                                            .check(status().is(200)))))

                    // tryMax(N) — ponawia blok do N razy w razie niepowodzenia
                    .group("retry")
                    .on(
                            tryMax(2)
                                    .on(exec(http("Produkt z retry").get("/products/0").check(status().is(200))))
                                    .exitHereIfFailed())

                    // asLongAs() — petla warunkowa (powtarza dopoki warunek true)
                    .group("loop")
                    .on(
                            exec(session -> session.set("currentPage", 0))
                                    .asLongAs(session -> session.getInt("currentPage") < 3)
                                    .on(
                                            exec(http("Strona asLongAs #{currentPage}")
                                                    .get("/products?page=#{currentPage}")
                                                    .check(status().is(200)))
                                                    .exec(
                                                            session -> {
                                                                int next = session.getInt("currentPage") + 1;
                                                                return session.set("currentPage", next);
                                                            })
                                                    .pause(1)))

                    // during() — petla czasowa (powtarza przez zadany czas)
                    // pace() — wymusza minimalny czas iteracji
                    .group("loop")
                    .on(
                            during(Duration.ofSeconds(5))
                                    .on(
                                            pace(Duration.ofSeconds(2))
                                                    .exec(
                                                            http("Produkt w petli czasowej")
                                                                    .get("/products/1")
                                                                    .check(status().is(200))))
                    );

    {
        setUp(scn.injectOpen(atOnceUsers(1)))
                .protocols(httpProtocol)
                .assertions(global().failedRequests().percent().lt(5.0));
    }

}
