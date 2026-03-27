package pl.training.ecommerce.phase7.config;

import java.time.Duration;

public final class Config {

    private Config() {
    }

    public static final String TEST_TYPE = System.getProperty("TEST_TYPE", "INSTANT");
    public static final int USERS = Integer.parseInt(System.getProperty("USERS", "3"));
    public static final Duration RAMP_DURATION = Duration.ofSeconds(Integer.parseInt(System.getProperty("RAMP_DURATION", "10")));
    public static final Duration TEST_DURATION = Duration.ofSeconds(Integer.parseInt(System.getProperty("DURATION", "30")));
    public static final int MAX_RPS = Integer.parseInt(System.getProperty("MAX_RPS", "10"));
    public static final Duration PACE_DURATION = Duration.ofSeconds(Integer.parseInt(System.getProperty("PACE", "10")));
    public static final Duration MIN_PAUSE = Duration.ofSeconds(1);
    public static final Duration MAX_PAUSE = Duration.ofSeconds(3);
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "gatling";

}
