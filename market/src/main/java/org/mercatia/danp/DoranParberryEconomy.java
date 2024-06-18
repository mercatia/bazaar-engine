package org.mercatia.danp;

import java.io.InputStream;

import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.Market;
import org.mercatia.bazaar.MarketData;
import org.mercatia.bazaar.Transport;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.AgentData;
import org.mercatia.bazaar.impl.MarketImpl;
import org.mercatia.danp.jobs.LogicBlacksmith;
import org.mercatia.danp.jobs.LogicFarmer;
import org.mercatia.danp.jobs.LogicMiner;
import org.mercatia.danp.jobs.LogicRefiner;
import org.mercatia.danp.jobs.LogicWoodcutter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 */
public class DoranParberryEconomy extends Economy {

	static Logger logger = LoggerFactory.getLogger(DoranParberryEconomy.class);

	public static class JobFactory extends AgentData.Factory {

		@Override
		public Agent build() {
			switch (logic()) {
				case "farmer":
					return new LogicFarmer(id, data, goods);
				case "miner":
					return new LogicMiner(id, data, goods);
				case "refiner":
					return new LogicRefiner(id, data, goods);
				case "woodcutter":
					return new LogicWoodcutter(id, data, goods);
				case "blacksmith":
					return new LogicBlacksmith(id, data, goods);
				default:
					throw new RuntimeException(this.logic() + " unknown");
			}
		}

	}

	private final static String[] marketNames = new String[] { "WibbleCity" };

	public DoranParberryEconomy() {
		super();
	}

	private Transport transport;

	@Override
	public void configure(Transport transport) {

		try {
			logger.info("Reading the configuration");
			ObjectMapper mapper = new ObjectMapper();
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("settings.json");

			this.startingMarketData = mapper.readValue(is, MarketData.class);
			logger.info(startingMarketData.toString());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		this.transport = transport;
		transport.greet(this.getClass().getSimpleName() + " hello");

		setAgentFactory(new JobFactory());

		for (var n : marketNames) {
			var market = new MarketImpl(n, this.startingMarketData, this);
			market.addListener(transport);
			addMarket(market);
		}

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

	// private Logic getLogic(String str)
	// {
	// switch(str)
	// {
	// case "blacksmith": return new LogicBlacksmith(null);
	// case "farmer": return new LogicFarmer(null);
	// case "miner": return new LogicMiner(null);
	// case "refiner": return new LogicRefiner(null);
	// case "woodcutter": return new LogicWoodcutter(null);
	// }
	// return null;
	// }

	public void onBankruptcy(Market arg0, Agent arg1) {
		// TODO Auto-generated method stub

	}

}
