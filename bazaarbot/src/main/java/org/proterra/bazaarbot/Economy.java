package org.proterra.bazaarbot;
import java.util.HashMap;
import java.util.Map;

/**
 * ...
 * @author larsiusprime
 */

class Economy
{
	public Economy()
	{
		this.markets = new HashMap<String,Market>();
	}

	public void addMarket(Market m)
	{
		if (_markets.indexOf(m) == -1)
		{
			_markets.push(m);
			m.signalBankrupt.add(onBankruptcy);
		}
	}

	public getMarket(name:String):Market
	{
		for (m in _markets)
		{
			if (m.name == name) return m;
		}
		return null;
	}

	public simulate(rounds:Int)
	{
		for (m in _markets)
		{
			m.simulate(rounds);
		}
	}

	/***PRIVATE***/

	private onBankruptcy(m:Market, a:BasicAgent):Void
	{
		//no implemenation -- provide your own in a subclass
	}

	private Map<String,Market> markets;
}
