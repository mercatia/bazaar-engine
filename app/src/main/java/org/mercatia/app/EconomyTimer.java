package org.mercatia.app;

import java.util.TimerTask;

import org.mercatia.bazaar.Economy;

public class EconomyTimer extends TimerTask {

    private Economy economy;

    public EconomyTimer(Economy e) {
        this.economy = e;
    }

    @Override
    public void run() {
        economy.simulate(1);
    }
}
