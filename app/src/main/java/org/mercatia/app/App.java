
package org.mercatia.app;

import static org.mercatia.bazaar.Transport.Actions.TICK;

import java.util.HashMap;

import org.mercatia.bazaar.Bootstrap;
import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.Transport;
import org.mercatia.nothing.NothingBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;

public class App {

    static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        var world = new HashMap<String, Economy>();
        var bus = vertx.eventBus();

        
        var bootstrap = Bootstrap.get(NothingBootstrap.class);
        var factory = bootstrap.getEconomyFactory();
        
        for (var name : bootstrap.getEconomyNames()) {
            var economy = factory.withName(name).build();
        
            world.put(name, economy.configure().startEconomy());

            vertx.setPeriodic(200, id -> {
                String addr = String.format("economy/%s", name);
                logger.info("tick to " + addr);

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