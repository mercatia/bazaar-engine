
package org.mercatia.app;

import static org.mercatia.bazaar.Transport.Actions.TICK;

import java.util.HashMap;

import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.Transport;
import org.mercatia.bazaar.Transport.MSG_TYPE;
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

        var econ = new DoranParberryEconomy("EcoOne");
        world.put("EcoOne", econ);

        var bus = vertx.eventBus();

        for (var e : world.entrySet()) {
            var name = e.getKey();
            var economy = e.getValue();
            economy.configure(vertx);
            economy.start(vertx);

            long timerId = vertx.setPeriodic(800, id -> {
                String addr = String.format("economy/%s", name);
                logger.info("tick to "+ addr);

                var m = Transport.IntraMessage.actionMessage(TICK);
                bus.send(addr, m.msg(), m.options());
            });
        }

        vertx.deployVerticle(new Server(world));

    }
}

/**
 * 
 * Imports which????
 * 
 * 
 */