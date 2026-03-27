package pl.training.toolshop.phase3;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ToolshopSimulation extends Simulation {

  private final HttpProtocolBuilder httpProtocol =
      http.baseUrl("https://api.practicesoftwaretesting.com")
          .acceptHeader("application/json")
          .contentTypeHeader("application/json");

  private final ScenarioBuilder scenario = scenario("session management")
          .exec(
              http("get/set session value")
                  .get("/products")
                  .check(status().is(200))
                  .check(jsonPath("$.data[0].id").saveAs("productId"))
                  .check(jsonPath("$.data[0].name").saveAs("productName")))
          .exec(
              session -> {
                System.out.println(
                    "Wybrany produkt: "
                        + session.getString("productName")
                        + " (ULID: "
                        + session.getString("productId")
                        + ")");
                return session;
              })
          .pause(1, 2)
          .exec(
              http("get product with id #{productId}")
                  .get("/products/#{productId}")
                  .check(status().is(200))
                  .check(jsonPath("$.name").isEL("#{productName}"))
                  .check(jsonPath("$.price").saveAs("productPrice")))
          .exec(
              session -> {
                System.out.println("Cena produktu: " + session.getString("productPrice"));
                return session;
              })
          .pause(1, 2)
          .exec(
              http("products related to product with id: #{productId}")
                  .get("/products/#{productId}/related")
                  .check(status().is(200)))
          .pause(1, 2)
           .exec(
              http("send contact form")
                  .post("/messages")
                  .body(
                      StringBody(
                          """
                          {
                            "first_name": "Test",
                            "last_name": "User",
                            "email": "test@example.com",
                            "subject": "customer-service",
                            "message": "Pytanie o produkt #{productName} (#{productId}) w cenie #{productPrice}"
                          }
                          """))
                  .check(status().is(200))
                  .check(jsonPath("$.id").exists()));

  {
    setUp(scenario.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  }
}
