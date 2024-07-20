package org.mercatia.nothing;

import java.util.Set;

import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.EconomyFactory;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.market.Market;


public class NothingEconomy extends Economy {

    public static class EFactory extends EconomyFactory {

        @Override
        public Economy build() {
            return new NothingEconomy();
        }

    }

    public NothingEconomy() {
        super("NothingEconomy");
    }

    @Override
    public Set<String> getMarketNames() {
        return Set.of("NothingMarket");
    }

    @Override
    public void onBankruptcy(Market m, Agent agent) {
    }

}
