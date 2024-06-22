package org.mercatia.app;

import java.util.TimerTask;

import org.mercatia.bazaar.Economy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EconomyTimer extends TimerTask {

   static Logger logger = LoggerFactory.getLogger(EconomyTimer.class);


    private Economy economy;

    public EconomyTimer(Economy e) {
        this.economy = e;
    }

    @Override
    public void run() {
        logger.info("Calling simulate");
        economy.simulate(1);       
    }
}
