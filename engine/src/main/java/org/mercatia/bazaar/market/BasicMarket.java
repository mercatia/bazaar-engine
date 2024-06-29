package org.mercatia.bazaar.market;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mercatia.bazaar.Economy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;

/**

 */
public class BasicMarket extends Market {

	public static MarketData.Factory factory() {
		return new BasicMarketDefault();
	}

	public static class BasicMarketDefault extends MarketData.Factory {

		@Override
		public List<Market> build(Vertx vertx) {
			var markets = new ArrayList<Market>();

			for (var n : this.names) {
				var m = new BasicMarket(n, this.startingMarketData, this.economy, vertx);
				markets.add(m);
			}

			return markets;
		}

	}

	static Logger logger = LoggerFactory.getLogger(BasicMarket.class);

	private record J(String name, List<String> agentids, List<String> goods, Jsony tradebook) implements Jsony {
	};

	public Jsony jsonify() {
		return new J(name,
				_agents.keySet().stream().map(v -> v.toString()).collect(Collectors.toList()),
				_goodTypes,
				_book.jsonify());
	}

	public BasicMarket(String name, MarketData data, Economy economy, Vertx vertx) {
		super(name, data, economy, vertx);
	}







}
