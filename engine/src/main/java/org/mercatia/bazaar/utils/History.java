package org.mercatia.bazaar.utils;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.currency.Money;


/**
 */
public class History implements Jsonable {
	public HistoryLog<Money> prices;
	public HistoryLog<ValueRT> asks;
	public HistoryLog<ValueRT> bids;
	public HistoryLog<ValueRT> trades;
	public HistoryLog<ValueRT> profit;

	public History() {
		prices = new HistoryLog<>(EconNoun.Price);
		asks = new HistoryLog<>(EconNoun.Ask);
		bids = new HistoryLog<>(EconNoun.Bid);
		trades = new HistoryLog<>(EconNoun.Trade);
		profit = new HistoryLog<>(EconNoun.Profit);
	}

	public void register(String good) {
		prices.register(good);
		asks.register(good);
		bids.register(good);
		trades.register(good);
		profit.register(good);
	}

	private static record J(Jsony prices, Jsony asks, Jsony bids, Jsony trades, Jsony profit) implements Jsony {
	};

	@Override
	public Jsony jsonify() {
		return new J(prices.jsonify(), asks.jsonify(), bids.jsonify(), trades.jsonify(), profit.jsonify());
	}
}
