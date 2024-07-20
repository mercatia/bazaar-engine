package org.mercatia.nothing;

import java.util.Set;

import org.mercatia.bazaar.Bootstrap;
import org.mercatia.bazaar.EconomyFactory;
import org.mercatia.bazaar.agent.AgentFactory;
import org.mercatia.bazaar.goods.GoodFactory;
import org.mercatia.bazaar.market.MarketFactory;

/** Entry point for all the engines */
public class NothingBootstrap extends Bootstrap {

    @Override
    public Class<? extends EconomyFactory> economyFactory() {
        return NothingEconomy.EFactory.class;
    }

    @Override
    public Class<? extends AgentFactory> agentFactory() {
        return NothingAgent.Factory.class;
    }

    @Override
    public Class<? extends GoodFactory> goodFactory() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'goodFactory'");
    }

    @Override
    public Class<? extends MarketFactory> marketFactory() {
        return NothingMarket.Factory.class;
    }

    @Override
    public Set<String> getEconomyNames() {
        return Set.of("NothingEconomy");
    }

    @Override
    public Bootstrap configure() {
        // nothing to read
        return this;
    }

}
