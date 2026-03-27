package pl.training.videogames.pages;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import pl.training.videogames.config.Keys;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

// Page Object: /videogame — CRUD operacje na grach
// feed() tylko w details i create (potrzebuja danych z CSV)
// update i delete polegaja na sesji ustawionej przez details/create
public final class VideoGamePage {

  private VideoGamePage() {}

  private static final FeederBuilder<String> csvFeeder = csv("data/games.csv").random();

  // Lista wszystkich gier — walidacja Content-Type i istnienia danych
  public static final ChainBuilder list =
      exec(
          http("Lista gier")
              .get("/videogame")
              .check(status().is(200))
              .check(header("Content-Type").is("application/json"))
              .check(jsonPath("$[0].id").ofInt().gt(0)));

  // Szczegoly gry — feed z CSV, walidacja nazwy, zapis kategorii
  public static final ChainBuilder details =
      feed(csvFeeder)
          .exec(
              http("Szczegoly gry - #{name}")
                  .get("/videogame/#{gameId}")
                  .check(status().is(200))
                  .check(jsonPath("$.name").isEL("#{name}"))
                  .check(jmesPath("category").saveAs(Keys.CURRENT_CATEGORY)));

  // Tworzenie gry — feed z CSV, ElFileBody, zapis ID
  public static final ChainBuilder create =
      feed(csvFeeder)
          .exec(
              http("Utworz gre - #{name}")
                  .post("/videogame")
                  .header("authorization", "Bearer #{jwtToken}")
                  .body(ElFileBody("bodies/newGame.json"))
                  .asJson()
                  .check(jsonPath("$.id").ofInt().saveAs(Keys.CREATED_GAME_ID)));

  // Aktualizacja gry — uzywa danych z sesji (bez feed)
  public static final ChainBuilder update =
      exec(
          http("Aktualizuj gre - #{name}")
              .put("/videogame/#{gameId}")
              .header("authorization", "Bearer #{jwtToken}")
              .body(
                  StringBody(
                      """
                      {
                        "id": #{gameId},
                        "category": "#{category}",
                        "name": "#{name} - Updated",
                        "rating": "#{rating}",
                        "releaseDate": "#{releaseDate}",
                        "reviewScore": 99
                      }
                      """))
              .asJson()
              .check(jmesPath("name").isEL("#{name} - Updated")));

  // Usuniecie gry — uzywa danych z sesji (bez feed)
  public static final ChainBuilder delete =
      exec(
          http("Usun gre - #{name}")
              .delete("/videogame/#{gameId}")
              .header("authorization", "Bearer #{jwtToken}")
              .check(bodyString().is("Video game deleted")));
}
