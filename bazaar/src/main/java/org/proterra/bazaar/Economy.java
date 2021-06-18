package org.proterra.bazaar;

import java.util.HashMap;
import java.util.Map;

import org.proterra.bazaar.agent.BasicAgent;
import org.proterra.events.EventsListener;

/**
 *
 */

public abstract class Economy implements MarketListener {
	private Map<String, Market> markets;

	public Economy() {
		this.markets = new HashMap<String, Market>();
	}

	public void addMarket(Market m)
	{
		if (!markets.containsKey(m.getName()))
		{
			markets.put(m.getName(),m);
			m.addListener(this);
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
