package org.mercatia.bazaar;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.*;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.AgentData;
import org.mercatia.bazaar.agent.LogicBuilder;
import org.mercatia.bazaar.impl.MarketImpl;

public abstract class Economy {
	private Map<String, Market> markets;
	private LogicBuilder logicBuilder;
	protected MarketData startingMarketData;
	protected AgentData.Factory agentFactory;

	static Logger logger = LoggerFactory.getLogger(Transport.class);



	public Economy() {
		this.markets = new HashMap<String, Market>();
	}

	public abstract void configure(Transport transport);

	public void addMarket(Market m) {
		if (!markets.containsKey(m.getName())) {
			markets.put(m.getName(), m);
			
		}
	}

	public AgentData.Factory getAgentFactory(){
		return this.agentFactory;
	}

	public Market createMarket(String name, MarketData md) {
		return new MarketImpl(name, md, this);
	}

	public Market getMarket(String name) {
		return markets.get(name);
	}

	public Set<String> getMarketNames() {
		return markets.keySet();
	}

	public void simulate(int rounds) {
		logger.info("Simulation of rounds=" + rounds);
		for (Market m : markets.values()) {
			m.simulate(rounds);
		}
		logger.info("Simulation complete");
	}

	public Economy setAgentFactory(AgentData.Factory factory){
		this.agentFactory = factory;
		return this;
	}

	public void setLogicBuilder(LogicBuilder builder) {
		this.logicBuilder = builder;
	}

	public LogicBuilder getLogicBuilder() {
		return this.logicBuilder;
	}

	public abstract void onBankruptcy(Market m, Agent agent);

}
