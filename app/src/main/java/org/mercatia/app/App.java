
package org.mercatia.app;

import java.util.*;

import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.Transport;
import org.mercatia.danp.DoranParberryEconomy;

import org.slf4j.*;

public class App {
    
    static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        logger.info("Hello");

        var world = new HashMap<String,Economy>();

        var tx = Transport.configure();

        world.put("EcoOne",new DoranParberryEconomy());

        for (var e: world.values()){
            e.configure(tx);
          
            var timerTask = new EconomyTimer(e);
            Timer timer = new Timer(false);
            timer.scheduleAtFixedRate(timerTask, 0, 4*1000);
        }

    }
}
