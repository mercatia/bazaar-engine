package org.mercatia.bazaar.market;

import org.mercatia.bazaar.AbstractFactory;
import org.mercatia.bazaar.Economy;

public abstract class MarketFactory extends AbstractFactory {

    protected MarketFactory() {

    }

    protected String name;
    protected Economy economy;

    public MarketFactory withName(String name) {
        this.name = name;
        return this;
    }

    public MarketFactory withEconomy(Economy economy) {
        this.economy = economy;
        return this;
    }

    public abstract Market build();

}
