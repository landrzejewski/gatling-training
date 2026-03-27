package pl.training.ecommerce.phase7.simulations;

import io.gatling.javaapi.core.ChainBuilder;
import pl.training.ecommerce.phase7.SessionHelper;
import pl.training.ecommerce.phase7.config.Config;
import pl.training.ecommerce.phase7.pages.Authentication;
import pl.training.ecommerce.phase7.pages.Cart;
import pl.training.ecommerce.phase7.pages.CheckoutProcess;
import pl.training.ecommerce.phase7.pages.Products;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.pace;
import static pl.training.ecommerce.phase7.config.Config.*;

public class UserJourney {

    public static final ChainBuilder browseProducts = pace(Config.PACE_DURATION)
            .group("browse products")
            .on(
                    exec(SessionHelper.initSession)
                            .exec(Products.list)
                            .pause(MIN_PAUSE, MAX_PAUSE)
                            .exec(Products.details)
                            .pause(MIN_PAUSE, MAX_PAUSE)
                            .exec(Products.listPage(1))
                            .pause(MIN_PAUSE, MAX_PAUSE)
                            .exec(Products.search("shirt"))
            );

    public static final ChainBuilder abandonCart = pace(Config.PACE_DURATION)
            .group("abort cart")
            .on(
                    exec(SessionHelper.initSession)
                            .exec(SessionHelper.initSession)
                            .pause(MIN_PAUSE, MAX_PAUSE)
                            .exec(Products.list)
                            .pause(MIN_PAUSE, MAX_PAUSE)
                            .exec(Products.details)
                            .pause(MIN_PAUSE, MAX_PAUSE)
                            .exec(Cart.addProduct)
            );

    public static final ChainBuilder completePurchase = pace(Config.PACE_DURATION)
            .group("complete purchase")
            .on(
                    exec(SessionHelper.initSession)
                            .exec(SessionHelper.initSession)
                            .pause(MIN_PAUSE, MAX_PAUSE)
                            .exec(Products.list)
                            .pause(MIN_PAUSE, MAX_PAUSE)
                            .exec(Products.details)
                            .pause(MIN_PAUSE, MAX_PAUSE)
                            .exec(Cart.addProduct)
                            .pause(MIN_PAUSE, MAX_PAUSE)
                            .exec(Authentication.login(USERNAME, PASSWORD))
                            .pause(MIN_PAUSE, MAX_PAUSE)
                            .exec(CheckoutProcess.checkout)
            );

}
