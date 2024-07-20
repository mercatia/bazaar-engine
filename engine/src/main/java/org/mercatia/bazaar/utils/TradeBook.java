package org.mercatia.bazaar.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mercatia.bazaar.Offer;
import org.mercatia.bazaar.goods.Good.GoodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class TradeBook {

	static Logger logger = LoggerFactory.getLogger(TradeBook.class);
	protected Map<GoodType, ArrayList<Offer>> bids;
	protected Map<GoodType, ArrayList<Offer>> asks;

	public TradeBook() {
		bids = new HashMap<>();
		asks = new HashMap<>();
	}

	public ArrayList<Offer> getBids(GoodType good) {
		return this.bids.get(good);
	}

	public ArrayList<Offer> getAsks(GoodType good) {
		return this.asks.get(good);
	}

	public void clear() {
		this.asks.clear();
		this.bids.clear();
	}

	public TradeBook addOffer(Offer offer) {
		var offerList = offer.isBuy() ? bids : asks;
		var goodType = offer.getGoodType();

		if (!offerList.containsKey(goodType))
			offerList.put(goodType, new ArrayList<Offer>());

		offerList.get(goodType).add(offer);

		return this;
	}

	public void logBook() {
		for (var b : this.bids.values()) {
			logger.info(b.toString());
		}

		for (var b : this.asks.values()) {
			logger.info(b.toString());
		}
	}
}
