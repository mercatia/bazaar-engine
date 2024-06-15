package org.mercatia.bazaar;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.LogicBuilder;
import org.mercatia.bazaar.impl.MarketImpl;

/**
 *
 */

public abstract class Economy implements MarketListener {
	private Map<String, Market> markets;
	private LogicBuilder logicBuilder;
	protected MarketData startingMarketData;


    private static final Logger LOGGER = Logger.getLogger(Economy.class.getName());

	public Economy() {
		this.markets = new HashMap<String, Market>();


	}

	public abstract void configure();

	public void addMarket(Market m)
	{
		if (!markets.containsKey(m.getName()))
		{
			markets.put(m.getName(),m);
			m.addListener(this);
		}
	}


	public Market createMarket(String name, MarketData md){
		 return new MarketImpl(name,md);
	}

	public Market getMarket(String name) {
		return markets.get(name);
	}

	public Set<String> getMarketNames(){
		return markets.keySet();
	}

	public void simulate(int rounds) {
		LOGGER.info("Simulation of rounds="+rounds);
		for (Market m : markets.values()) {
			m.simulate(rounds);
		}
		LOGGER.info("Simulation complete");
	}

	public void setLogicBuilder(LogicBuilder builder){
		this.logicBuilder = builder;
	}

	public LogicBuilder getLogicBuilder(){
		return this.logicBuilder;
	}

	public abstract void onBankruptcy(Market m, Agent agent);

}
