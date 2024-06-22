package org.mercatia.bazaar.utils;

import org.mercatia.bazaar.currency.Money;

/**
 */
public class History
{
	public HistoryLog<Money> prices;
	public HistoryLog<ValueRT> asks;
	public HistoryLog<ValueRT> bids;
	public HistoryLog<ValueRT> trades;
	public HistoryLog<ValueRT> profit;

	public History()
	{
		prices = new HistoryLog<>(EconNoun.Price);
		asks   = new HistoryLog<>(EconNoun.Ask);
		bids   = new HistoryLog<>(EconNoun.Bid);
		trades = new HistoryLog<>(EconNoun.Trade);
		profit = new HistoryLog<>(EconNoun.Profit);
	}

	public void register(String good)
	{
		prices.register(good);
		asks.register(good);
		bids.register(good);
		trades.register(good);
		profit.register(good);
	}
}
