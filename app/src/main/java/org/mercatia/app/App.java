
package org.mercatia.app;

import java.util.*;

import org.mercatia.bazaar.Economy;
import org.mercatia.danp.DoranParberryEconomy;

public class App {
    public static void main(String[] args) {

        var world = new HashMap<String,Economy>();
        world.put("EcoOne",new DoranParberryEconomy());

        for (var e: world.values()){
            e.configure();

            var timerTask = new EconomyTimer(e);
            Timer timer = new Timer(false);
            timer.scheduleAtFixedRate(timerTask, 0, 10*1000);
        }

    }
}
