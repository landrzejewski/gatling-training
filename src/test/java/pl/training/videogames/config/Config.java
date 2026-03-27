package pl.training.videogames.config;

import java.time.Duration;

// Konfiguracja produkcyjna — 4 wagi Journey, pauzy, profil testu
// Uzycie: mvn gatling:test -DUSERS=10 -DBROWSE_WEIGHT=40 -DCREATE_VIEW_WEIGHT=30
public final class Config {

  private Config() {}

  public static final String BASE_URL =
      System.getProperty("BASE_URL", "https://videogamedb.uk/api");
  public static final String TEST_TYPE = System.getProperty("TEST_TYPE", "INSTANT");
  public static final int USERS = Integer.parseInt(System.getProperty("USERS", "5"));
  public static final Duration RAMP_DURATION =
      Duration.ofSeconds(Integer.parseInt(System.getProperty("RAMP_DURATION", "10")));
  public static final Duration TEST_DURATION =
      Duration.ofSeconds(Integer.parseInt(System.getProperty("DURATION", "30")));
  public static final int MAX_RPS = Integer.parseInt(System.getProperty("MAX_RPS", "10"));
  public static final Duration PACE_DURATION =
      Duration.ofSeconds(Integer.parseInt(System.getProperty("PACE", "10")));

  // 4 wagi scenariuszy
  public static final double BROWSE_WEIGHT =
      Double.parseDouble(System.getProperty("BROWSE_WEIGHT", "40.0"));
  public static final double CREATE_VIEW_WEIGHT =
      Double.parseDouble(System.getProperty("CREATE_VIEW_WEIGHT", "25.0"));
  public static final double FULL_CRUD_WEIGHT =
      Double.parseDouble(System.getProperty("FULL_CRUD_WEIGHT", "20.0"));
  public static final double SEARCH_WEIGHT =
      Double.parseDouble(System.getProperty("SEARCH_WEIGHT", "15.0"));

  // Konfigurowalne pauzy
  public static final Duration MIN_PAUSE =
      Duration.ofSeconds(Integer.parseInt(System.getProperty("MIN_PAUSE", "1")));
  public static final Duration MAX_PAUSE =
      Duration.ofSeconds(Integer.parseInt(System.getProperty("MAX_PAUSE", "3")));
}
