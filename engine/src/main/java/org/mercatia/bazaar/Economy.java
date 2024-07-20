package org.mercatia.bazaar;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.AgentFactory;
import org.mercatia.bazaar.goods.GoodFactory;
import org.mercatia.bazaar.market.Market;
import org.mercatia.bazaar.utils.Tick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public abstract class Economy extends BootstrappedEntity {
	private Map<String, Market> markets;

	protected AgentFactory agentFactory;
	
	protected GoodFactory goodFactory;

	protected EventBus eventBus;
	protected String name;
	protected String addr;

	static Logger logger = LoggerFactory.getLogger(Transport.class);

	public Economy(String name) {
		super();
		this.markets = new HashMap<String, Market>();
		this.name = name;
		this.addr = String.format("economy/%s", this.name);
	}

	public Economy configure() {
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
						var t = Tick.getTick().increment();
						simulate();

						reply.put("tick", t.toString());
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

	/**
	 * Called to start the economy working.
	 * 
	 * @return
	 */
	public Economy startEconomy() {
		// all the factories should be configure by now.
		var marketNames = this.getMarketNames();
		
		marketNames.forEach(name -> {
			var marketFactory = getBootstrap().getMarketFactory(this);
			var market = marketFactory.withName(name).withEconomy(this).build();
			addMarket(market);
		});

		publishStarted();
		return this;
	}

	public Economy stopEconomy() {
		// TODO:
		return this;
	}

	public String getName() {
		return this.name;
	}

	public void addMarket(Market m) {
		if (!markets.containsKey(m.getName())) {
			markets.put(m.getName(), m);
		}
	}

	public Market getMarket(String name) {
		return markets.get(name);
	}

	public void simulate() {
		var pointInTime = Tick.getTick().pointInTime();

		for (Market m : markets.values()) {
			m.simulate(pointInTime);
		}
	}

	protected void publishStarted() {
		var msg = new JsonObject();
		msg.put("name", this.name);
		msg.put("event", "started");

		this.eventBus.publish(addr, msg, new DeliveryOptions().addHeader("type", "event"));
	}

	public abstract Set<String> getMarketNames();

	public abstract void onBankruptcy(Market m, Agent agent);

}
