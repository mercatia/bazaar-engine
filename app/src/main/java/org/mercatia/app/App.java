
package org.mercatia.app;

import java.util.HashMap;
import java.util.Timer;

import org.mercatia.bazaar.Economy;
import org.mercatia.danp.DoranParberryEconomy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;

public class App {

    static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        var world = new HashMap<String, Economy>();

        // var tx = Transport.configure();

        var econ = new DoranParberryEconomy("EcoOne").configure(vertx);
        world.put("EcoOne", econ);

        var bus = vertx.eventBus();

        for (var e : world.entrySet()) {
            var name = e.getKey();
            var economy = e.getValue();
            economy.configure(vertx);
            bus.consumer("economy/" + name + "/outgoing", message -> {
                System.out.println("I have received a message: " + message.body());
            });

            long timerId = vertx.setPeriodic(5000, id -> {
                logger.info("tick "+"economy/" + name + "/incoming");
                bus.send("economy/" + name + "/incoming", "tick");
            });
        }

        vertx.deployVerticle(new Server(world));

    }
}

/**
 * 
 Imports which????

 * 
 */