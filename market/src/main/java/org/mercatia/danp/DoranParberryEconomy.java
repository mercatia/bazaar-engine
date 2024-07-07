package org.mercatia.danp;

import static org.mercatia.danp.DoranParberryEconomy.DPEJobs.FARMER;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.AgentData;
import org.mercatia.bazaar.agent.Logic;
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

	public static enum DPEJobs implements Agent.Logic {
		FARMER("farmer"),
		MINER("miner"),
		REFINER("refiner"),
		WOODCUTTER("woodcutter"),
		BLACKSMITH("blacksmith");

		private static final Map<String, DPEJobs> BY_LABEL = new HashMap<>();
		static {
			for (DPEJobs e : values()) {
				BY_LABEL.put(e.label, e);
			}
		}

		public static DPEJobs valueOfLabel(String label) {
			return BY_LABEL.get(label);
		}

		public static Agent.Logic logicOfLabel(String label) {
			return (Agent.Logic) (BY_LABEL.get(label));
		}

		private final String label;

		private DPEJobs(String label) {
			this.label = label;
		}

		public String label() {
			return this.label;
		}
	}

	static Logger logger = LoggerFactory.getLogger(DoranParberryEconomy.class);

	public static class JobFactory extends AgentData.Factory {

		@Override
		public Agent buildAgent() {
			DPEJobs jobs = (DPEJobs) (logic());
			switch (jobs) {
				case FARMER:
					return new LogicFarmer(data, goods);
				case MINER:
					return new LogicMiner(data, goods);
				case REFINER:
					return new LogicRefiner(data, goods);
				case WOODCUTTER:
					return new LogicWoodcutter(data, goods);
				case BLACKSMITH:
					return new LogicBlacksmith(data, goods);
				default:
					throw new RuntimeException(this.logic() + " unknown");
			}
		}

		@Override
		protected Agent.Logic logicFrom(String name) {
			return DPEJobs.logicOfLabel(name);
		}

		@Override
		public List<Agent.Logic> listLogicTypes() {
			return List.of(DPEJobs.values());
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
	public Agent.Logic getAgentClassThatMakesMost(String good) {
		DPEJobs job;
		switch (good.toLowerCase()) {
			case "food":
				job = DPEJobs.FARMER;
				break;
			case "wood":
				job = DPEJobs.WOODCUTTER;
				break;

			case "ore":
				job = DPEJobs.MINER;
				break;

			case "metal":
				job = DPEJobs.REFINER;
				break;

			case "tools":
				job = DPEJobs.BLACKSMITH;
				break;

			default:
				throw new RuntimeException("Unknown good type " + good);

		}
		return (Agent.Logic) job;
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

		Agent.Logic bestClass = market.getMostProfitableAgentClass(10);
		System.out.println("Best class  " + bestClass);
		//Special case to deal with very high demand-to-supply ratios
		//This will make them favor entering an underserved market over
		//Just picking the most profitable class
		String bestGood = market.getHottestGood(1.5, 10);
		System.out.println("Best good   " + bestGood);

		if (bestGood != null) {
			var bestGoodClass = getAgentClassThatMakesMost(bestGood);

			if (bestGoodClass != null) {
				bestClass = bestGoodClass;
			}
		}

		logger.info("Creating new " + bestClass);
		market.addAgent(bestClass);
	}

}
