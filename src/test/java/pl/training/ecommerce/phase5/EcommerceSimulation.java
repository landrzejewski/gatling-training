package pl.training.ecommerce.phase5;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class EcommerceSimulation extends Simulation {

    private static final String TEST_TYPE = System.getProperty("TEST_TYPE", "INSTANT");
    private static final int USERS = Integer.parseInt(System.getProperty("USERS", "3"));
    private static final Duration DURATION = Duration.ofSeconds(Long.parseLong(System.getProperty("DURATION", "30")));
    private static final int MAX_RPS = Integer.parseInt(System.getProperty("MAX_RPS", "10"));

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://api-ecomm.gatling.io")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Mozilla/5.0");

    private final ScenarioBuilder scenario = scenario("profiles")
            .exec(
                    http("get products")
                            .get("/products")
            )
            .pause(1);

    @Override
    public void before() {
        System.out.println("STARTING ECOMMERCE SIMULATION, Profile: " + TEST_TYPE + ", Users: " + USERS);
    }

    @Override
    public void after() {
        System.out.println("FINISHED ECOMMERCE SIMULATION");
    }

    {
        var population = switch (TEST_TYPE) {
            // Open model: atOnceUsers — natychmiastowe wstrzykniecie N uzytkownikow
            case "INSTANT" -> scenario.injectOpen(atOnceUsers(USERS));
            // Open model: rampUsers — liniowy wzrost w czasie
            case "RAMP" -> scenario.injectOpen(rampUsers(USERS).during(DURATION));
            // Open model: rampUsersPerSec — liniowy wzrost tempa wstrzykiwania
            case "RAMP_RATE" -> scenario.injectOpen(rampUsersPerSec(1).to(USERS).during(DURATION).randomized());
            // Zwieksza obciazenie krokowo, przydatne do znalezienia progu wydajnosci
            case "STAIRCASE" -> scenario.injectOpen(incrementUsersPerSec(2.0)
                    .times(4)
                    .eachLevelLasting(Duration.ofSeconds(10))
                    .separatedByRampsLasting(Duration.ofSeconds(5))
                    .startingFrom(1.0)
            );
            // stressPeakUsers — krzywa S (realistyczny skok ruchu)
            case "STRESS_PEAK" -> scenario.injectOpen(stressPeakUsers(USERS * 5).during(DURATION));

            // Closed model: utrzymuje stala liczbe AKTYWNYCH uzytkownikow
            // (nowy uzytkownik startuje gdy poprzedni konczy)
            case "CLOSED" -> scenario.injectClosed(
                    constantConcurrentUsers(USERS).during(Duration.ofSeconds(15)),
                    rampConcurrentUsers(USERS).to(USERS * 2).during(Duration.ofSeconds(15)));

            // throttle() — ogranicza RPS (requests per second)
            // reachRps + holdFor: ramp do zadanego RPS i utrzymanie
            case "THROTTLED" -> scenario.injectOpen(constantUsersPerSec(USERS * 2).during(DURATION));

            // Zlozony profil — wiele krokow w jednym tescie
            // nothingFor() — opoznienie poczatkowe
            case "COMPLEX" -> scenario.injectOpen(
                    nothingFor(Duration.ofSeconds(5)),
                    atOnceUsers(1),
                    rampUsers(USERS).during(Duration.ofSeconds(10)),
                    constantUsersPerSec(USERS).during(Duration.ofSeconds(10)).randomized(),
                    rampUsersPerSec(1).to(USERS).during(Duration.ofSeconds(10)));

            default -> scenario.injectOpen(atOnceUsers(1));
        };

        var setup = setUp(population)
                .protocols(httpProtocol)
                .maxDuration(Duration.ofMinutes(2))
                .assertions(
                        global().failedRequests().percent().lt(5.0),
                        global().responseTime().percentile(95.0).lt(3000)
                );

        if (TEST_TYPE.equals("THROTTLED")) {
            setup.throttle(reachRps(MAX_RPS).in(Duration.ofSeconds(5)), holdFor(Duration.ofSeconds(20)));
        }
    }

}
