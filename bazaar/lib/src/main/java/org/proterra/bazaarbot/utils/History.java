package org.proterra.bazaarbot.utils;

/**
 */
public class History
{
	public HistoryLog prices;
	public HistoryLog asks;
	public HistoryLog bids;
	public HistoryLog trades;
	public HistoryLog profit;

	public History()
	{
		prices = new HistoryLog(EconNoun.Price);
		asks   = new HistoryLog(EconNoun.Ask);
		bids   = new HistoryLog(EconNoun.Bid);
		trades = new HistoryLog(EconNoun.Trade);
		profit = new HistoryLog(EconNoun.Profit);
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
