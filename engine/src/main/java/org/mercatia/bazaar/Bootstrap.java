package org.mercatia.bazaar;

import java.util.Set;

import org.mercatia.bazaar.agent.AgentFactory;
import org.mercatia.bazaar.goods.GoodFactory;
import org.mercatia.bazaar.market.Market;
import org.mercatia.bazaar.market.MarketFactory;

public abstract class Bootstrap {

    public static Bootstrap get(Class<? extends Bootstrap> bootstrapClass) {
        try {
            Bootstrap bootstrap = bootstrapClass.getDeclaredConstructor().newInstance();

            return bootstrap.configure();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public EconomyFactory getEconomyFactory() {
        try {
            var factory = economyFactory().getDeclaredConstructor().newInstance().withBootstrap(this);
            return (EconomyFactory) factory;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MarketFactory getMarketFactory(Economy economy) {
        try {
            var factory = marketFactory().getDeclaredConstructor().newInstance().withEconomy(economy)
                    .withBootstrap(this);
            return (MarketFactory) factory;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GoodFactory getGoodFactory(Market market) {
        try {
            var factory = goodFactory().getDeclaredConstructor().newInstance();
            return factory;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public AgentFactory getAgentFactory(Market market) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getGoodFactory'");
    }

    public abstract Bootstrap configure();

    public abstract Class<? extends EconomyFactory> economyFactory();

    public abstract Class<? extends AgentFactory> agentFactory();

    public abstract Class<? extends GoodFactory> goodFactory();

    public abstract Class<? extends MarketFactory> marketFactory();

    public abstract Set<String> getEconomyNames();

}
