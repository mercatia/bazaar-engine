package org.mercatia.danp;

import java.io.InputStream;
import java.util.Set;

import org.mercatia.bazaar.Bootstrap;
import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.EconomyFactory;
import org.mercatia.bazaar.agent.AgentFactory;
import org.mercatia.bazaar.goods.GoodFactory;
import org.mercatia.bazaar.market.MarketFactory;
import org.mercatia.danp.agents.DPAgentFactory;
import org.mercatia.danp.goods.DPGoodFactory;
import org.mercatia.danp.markets.DPMarketFactory;
import org.mercatia.danp.startingdata.MarketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DPBootstrap extends Bootstrap {
    static Logger logger = LoggerFactory.getLogger(DPBootstrap.class);

    public static class EFactory extends EconomyFactory {

        @Override
        public Economy build() {
            return new DoranParberryEconomy("EconOne");
        }

    }

    @Override
    public Class<? extends EconomyFactory> economyFactory() {
        return DPBootstrap.EFactory.class;
    }

    @Override
    public Class<? extends AgentFactory> agentFactory() {
        return DPAgentFactory.class;
    }

    @Override
    public Class<? extends GoodFactory> goodFactory() {
        return DPGoodFactory.class;
    }

    @Override
    public Class<? extends MarketFactory> marketFactory() {
        return DPMarketFactory.class;
    }

    @Override
    public Set<String> getEconomyNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEconomyNames'");
    }

    MarketData startingMarketData;

    public MarketData getStartingData(){
        return startingMarketData;
    }

    @Override
    public Bootstrap configure() {
        try {
            logger.info("Reading the configuration");
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("settings.json");

            this.startingMarketData = mapper.readValue(is, MarketData.class);
            logger.info(startingMarketData.toString());
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
