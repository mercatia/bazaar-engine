package org.mercatia.danp;

import java.io.InputStream;

import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.Market;
import org.mercatia.bazaar.MarketData;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.AgentData;
import org.mercatia.bazaar.impl.MarketImpl;
import org.mercatia.danp.jobs.LogicBlacksmith;
import org.mercatia.danp.jobs.LogicFarmer;
import org.mercatia.danp.jobs.LogicMiner;
import org.mercatia.danp.jobs.LogicRefiner;
import org.mercatia.danp.jobs.LogicWoodcutter;
import org.mercatia.events.AgentBankruptEvent;
import org.mercatia.events.MarketEventListener;
import org.mercatia.events.MarketReportEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

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
					return new LogicWoodcutter( data, goods);
				case "blacksmith":
					return new LogicBlacksmith( data, goods);
				default:
					throw new RuntimeException(this.logic() + " unknown");
			}
		}

	}

	private final static String[] marketNames = new String[] { "WibbleCity" };

	private EventBus eventBus;

	public DoranParberryEconomy(String name){
		super(name);
	}

	private String outboundTopic;

	@Override
	public Economy configure(Vertx vertx) {

		try {
			logger.info("Reading the configuration");
			ObjectMapper mapper = new ObjectMapper();
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("settings.json");

			this.startingMarketData = mapper.readValue(is, MarketData.class);
			logger.info(startingMarketData.toString());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		this.eventBus = vertx.eventBus();

		logger.info("economy/"+this.getName()+"/incoming");
		this.eventBus.consumer("economy/"+this.getName()+"/incoming",(event)->{
			logger.info("Got message" + event);
			if (event.body().toString().equals("tick")){
				logger.info("Calling simulate");
				simulate(1);
			}
		});
		
		this.outboundTopic = "economy/"+this.getName()+"/outbound";
		this.eventBus.send(outboundTopic, "Hello from "+this.getName());

		setAgentFactory(new JobFactory());

		for (var n : marketNames) {
			var market = new MarketImpl(n, this.startingMarketData, this);
			market.addListener(new EventAdapter(this.eventBus,this.outboundTopic));
			addMarket(market);
		}
		return this;
	}

	class EventAdapter implements MarketEventListener{

		private EventBus bus;
		private String topic;
		
		public EventAdapter(EventBus eventBus,String outboundTopic) {
			this.bus = eventBus;
			this.topic = outboundTopic;
		}

		@Override
		public void marketReport(MarketReportEvent event) {
			this.bus.send(topic,event);
		}

		@Override
		public void agentBankrupt(AgentBankruptEvent event) {
			this.bus.send(topic,event);
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
