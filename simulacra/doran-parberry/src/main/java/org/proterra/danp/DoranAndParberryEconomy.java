package org.proterra.danp;

import org.proterra.bazaar.*;
import org.proterra.danp.jobs.LogicBlacksmith;

/**
 * ...
 * @author larsiusprime
 */
class DoranAndParberryEconomy extends Economy
{

	public DoranAndParberryEconomy()
	{
		super();
		var market = new Market("default");
		// market.init(MarketData.fromJSON(Json.parse(Assets.getText("assets/settings.json")), getAgent));
		// addMarket(market);
	}

	// override  onBankruptcy(m:Market, a:BasicAgent):Void
	// {
	// 	replaceAgent(m, a);
	// }

	private void replaceAgent(Market market, BasicAgent  agent)
	{
		String bestClass = market.getMostProfitableAgentClass();

		//Special case to deal with very high demand-to-supply ratios
		//This will make them favor entering an underserved market over
		//Just picking the most profitable class
		String bestGood = market.getHottestGood();

		if (bestGood != "")
		{
			String bestGoodClass = getAgentClassThatMakesMost(bestGood);
			if (bestGoodClass != "")
			{
				bestClass = bestGoodClass;
			}
		}

		String newAgent = getAgent(market.getAgentClass(bestClass));
		market.replaceAgent(agent, newAgent);
	}


	/**
	 * Get the average amount of a given good that a given agent class has
	 * @param	className
	 * @param	good
	 * @return
	 */
	/*
	public  getAgentClassAverageInventory(className:String, good:String):Float
	{
		var list = _agents.filter((a:BasicAgent):Bool { return a.className == className; } );
		var amount:Float = 0;
		for (agent in list)
		{
			amount += agent.queryInventory(good);
		}
		amount /= list.length;
		return amount;
	}
	*/

	/**
	 * Find the agent class that produces the most of a given good
	 * @param	good
	 * @return
	 */
	public String getAgentClassThatMakesMost(String good)	{
		switch (good) {
			case "food": return "farmer";
			case "wood": return "woodcutter";
			case "ore":return "miner";
			case "metal":return "refiner";
			case "tools":return "blacksmith";
			default: return "";
		}
	}

	/**
	 * Find the agent class that has the most of a given good
	 * @param	good
	 * @return
	 */
	/*
	public  getAgentClassWithMost(good:String):String
	{
		var amount:Float = 0;
		var bestAmount:Float = 0;
		var bestClass:String = "";
		for (key in _mapAgents.keys())
		{
			amount = getAverageInventory(key, good);
			if (amount > bestAmount)
			{
				bestAmount = amount;
				bestClass = key;
			}
		}
		return bestClass;
	}
	*/


	private Logic getLogic(String str)
	{
		switch(str)
		{
			case "blacksmith": return new LogicBlacksmith(null);
			case "farmer": return new LogicFarmer(null);
			case "miner": return new LogicMiner(null);
			case "refiner": return new LogicRefiner(null);
			case "woodcutter": return new LogicWoodcutter(null);
		}
		return null;
	}
}
