package org.mercatia.danp;

import java.io.InputStream;
import java.util.Arrays;

import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.AgentData;
import org.mercatia.bazaar.market.BasicMarket;
import org.mercatia.bazaar.market.Market;
import org.mercatia.bazaar.market.MarketData;
import org.mercatia.danp.jobs.LogicBlacksmith;
import org.mercatia.danp.jobs.LogicFarmer;
import org.mercatia.danp.jobs.LogicMiner;
import org.mercatia.danp.jobs.LogicRefiner;
import org.mercatia.danp.jobs.LogicWoodcutter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.Vertx;

/**
 */
public class DoranParberryEconomy extends Economy {

	static Logger logger = LoggerFactory.getLogger(DoranParberryEconomy.class);

	public static class JobFactory extends AgentData.Factory {

		@Override
		public Agent build() {
			switch (logic()) {
				case "farmer":
					return new LogicFarmer(data, goods);
				case "miner":
					return new LogicMiner(data, goods);
				case "refiner":
					return new LogicRefiner(data, goods);
				case "woodcutter":
					return new LogicWoodcutter(data, goods);
				case "blacksmith":
					return new LogicBlacksmith(data, goods);
				default:
					throw new RuntimeException(this.logic() + " unknown");
			}
		}

	}

	private final static String[] marketNames = new String[] { "WibbleCity" };

	public DoranParberryEconomy(String name) {
		super(name);
	}

	@Override
	public Economy configure(Vertx vertx) {
		super.configure(vertx);

		try {
			logger.info("Reading the configuration");
			ObjectMapper mapper = new ObjectMapper();
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("settings.json");

			this.startingMarketData = mapper.readValue(is, MarketData.class);
			logger.info(startingMarketData.toString());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		setAgentFactory(new JobFactory());

		setMarketFactory(BasicMarket.factory().name(Arrays.asList(marketNames)).startingData(startingMarketData));

		return this;
	}

	/**
	 * Get the average amount of a given good that a given agent class has
	 * 
	 * @param className
	 * @param good
	 * @return
	 */
	/*
	 * public getAgentClassAverageInventory(className:String, good:String):Float {
	 * var list = _agents.filter((a:BasicAgent):Bool { return a.className ==
	 * className; } ); var amount:Float = 0; for (agent in list) { amount +=
	 * agent.queryInventory(good); } amount /= list.length; return amount; }
	 */

	/**
	 * Find the agent class that produces the most of a given good
	 * 
	 * @param good
	 * @return
	 */
	public String getAgentClassThatMakesMost(String good) {
		switch (good) {
			case "food":
				return "farmer";
			case "wood":
				return "woodcutter";
			case "ore":
				return "miner";
			case "metal":
				return "refiner";
			case "tools":
				return "blacksmith";
			default:
				return "";
		}
	}

	/**
	 * Find the agent class that has the most of a given good
	 * 
	 * @param good
	 * @return
	 */
	/*
	 * public getAgentClassWithMost(good:String):String { var amount:Float = 0; var
	 * bestAmount:Float = 0; var bestClass:String = ""; for (key in
	 * _mapAgents.keys()) { amount = getAverageInventory(key, good); if (amount >
	 * bestAmount) { bestAmount = amount; bestClass = key; } } return bestClass; }
	 */

	public void onBankruptcy(Market market, Agent oldAgent) {
	
		var bestClass= market.getMostProfitableAgentClass(10);

		//Special case to deal with very high demand-to-supply ratios
		//This will make them favor entering an underserved market over
		//Just picking the most profitable class
		var bestGood = market.getHottestGood(1.5,10);

		if (bestGood != "")
		{
			var bestGoodClass = getAgentClassThatMakesMost(bestGood);
			if (bestGoodClass != "")
			{
				bestClass = bestGoodClass;
			}
		}
		logger.info("Creating new "+bestClass);
		market.addAgent(bestClass);
	}

}
