package org.mercatia;

import java.util.List;

import org.mercatia.bazaar.Economy;

import io.vertx.core.Vertx;

public class EconomyBootstrap {

    public static Economy bootstrap(Class<? extends Economy> economyClass, String name, List<String> marketNames,Vertx vertex) {

        try {
            Economy newEcon = economyClass.getDeclaredConstructor().newInstance(name);
            return newEcon.configure().startEconomy();
        } catch (Exception e) {
            throw new RuntimeException("Unable to start ", e);
        }

    }
}
