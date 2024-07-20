package org.mercatia.danp.markets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.Offer;
import org.mercatia.bazaar.goods.Good.GoodType;
import org.mercatia.bazaar.market.Market;
import org.mercatia.danp.startingdata.MarketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;

/**

 */
public class DPMarket extends Market {

	public DPMarket(String name, Economy economy) {
		super(name, economy);
		//TODO Auto-generated constructor stub
	}

	static Logger logger = LoggerFactory.getLogger(DPMarket.class);

	private record J(String name, List<String> agentids, List<String> goods, List<Jsony> tradebook) implements Jsony {
	};

	public Jsony jsonify() {
		// try {
		// mutex.lock();
		// var resolvedOffers = new ArrayList<Jsony>();
		// if (lastResolvedTradeBook != null) {
		// //
		// lastResolvedTradeBook.values().forEach(offerList->offerList.forEach(offer->resolvedOffers.add(offer.jsonify())));;
		// }

		// return new J(name,
		// _agents.keySet().stream().map(v ->
		// v.toString()).collect(Collectors.toList()),
		// _goodTypes, resolvedOffers);
		// } finally {
		// mutex.unlock();
		// }
		return null;
	}

	@Override
	public List<GoodType> getGoodsTraded() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getGoodsTraded'");
	}
}
