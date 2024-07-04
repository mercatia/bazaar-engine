package org.mercatia.bazaar;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mercatia.bazaar.Transport.Actions;
import org.mercatia.bazaar.Transport.MSG_TYPE;
import org.mercatia.bazaar.Transport.MSG_KEYS;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.AgentData;
import org.mercatia.bazaar.agent.LogicBuilder;
import org.mercatia.bazaar.market.BasicMarket;
import org.mercatia.bazaar.market.Market;
import org.mercatia.bazaar.market.MarketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public abstract class Economy {
	private Map<String, Market> markets;
	private LogicBuilder logicBuilder;
	protected MarketData startingMarketData;
	protected AgentData.Factory agentFactory;
	protected MarketData.Factory marketFactory;

	protected EventBus eventBus;
	protected String name;
	protected String addr;


	protected BigDecimal tick = new BigDecimal(0);

	static Logger logger = LoggerFactory.getLogger(Transport.class);

	public Economy(String name) {
		this.markets = new HashMap<String, Market>();
		this.name = name;
		this.addr = String.format("economy/%s", this.name);
	}

	public Economy configure(Vertx vertx) {
		this.eventBus = vertx.eventBus();

		// add the event consumer here
		MessageConsumer<JsonObject> consumer = eventBus.consumer(this.addr);
		consumer.handler(message -> {
			var busMsg = Transport.IntraMessage.busmsg(message);
			if (busMsg.isAction()) {
				var reply = new JsonObject();
				switch (busMsg.getAction()) {
					case LIST_MARKETS:
						JsonArray arr = new JsonArray();
						getMarketNames().forEach((v) -> arr.add(v));
						reply.put("markets", arr);

						break;
					case TICK:
						simulate(1);

						reply.put("tick", this.tick.longValue());
						break;

					default:
						logger.error("Unknown action");
						message.fail(500, "Unknown action ");
				}

				message.reply(reply);
			}

		});

		return this;
	}

	public void start(Vertx vertx){
		var markets = this.marketFactory.economy(this).build(vertx);
		markets.forEach(m->addMarket(m));
		publishStarted();
	}

	protected void publishStarted() {
		var msg = new JsonObject();
		msg.put("name", this.name);
		msg.put("event", "started");

		this.eventBus.publish(addr, msg, new DeliveryOptions().addHeader("type", "event"));
	}

	public String getName() {
		return this.name;
	}

	public void addMarket(Market m) {
		if (!markets.containsKey(m.getName())) {
			markets.put(m.getName(), m);

		}
	}

	public AgentData.Factory getAgentFactory() {
		return this.agentFactory;
	}

	
	public MarketData.Factory getMarketFactory() {
		return this.marketFactory;
	}


	public Market getMarket(String name) {
		return markets.get(name);
	}

	public Set<String> getMarketNames() {
		return markets.keySet();
	}



	public void simulate(int rounds) {
		this.tick.add(new BigDecimal(rounds));
		logger.info("Simulation of rounds=" + rounds);
		for (Market m : markets.values()) {
			m.simulate(rounds);
		}
		logger.info("Simulation complete");

	}

	public Economy setAgentFactory(AgentData.Factory factory) {
		this.agentFactory = factory;
		return this;
	}

	public Economy setMarketFactory(MarketData.Factory factory) {
		this.marketFactory = factory;
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
