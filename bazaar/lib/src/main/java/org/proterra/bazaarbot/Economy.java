package org.proterra.bazaarbot;

import java.util.HashMap;
import java.util.Map;

import org.proterra.bazaarbot.agent.BasicAgent;

/**
 * ...
 * 
 * @author larsiusprime
 */

public abstract class Economy {
	private Map<String, Market> markets;

	public Economy() {
		this.markets = new HashMap<String, Market>();
	}

	public void addMarket(Market m)
	{
		if (!markets.containsKey(m.getName()))
		{
			markets.put(m.getName(),m);
			m.onBankruptcy(this);
		}
	}

	public Market getMarket(String name) {
		return markets.get(name);
	}

	public void simulate(int rounds) {
		for (Market m : markets.values()) {
			m.simulate(rounds);
		}
	}

	public abstract void onBankruptcy(Market m, BasicAgent agent);

}
