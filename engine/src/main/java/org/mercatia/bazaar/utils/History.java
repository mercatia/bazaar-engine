package org.mercatia.bazaar.utils;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.agent.Agent.Logic;
import org.mercatia.bazaar.currency.Currency;
import org.mercatia.bazaar.currency.Money;
import org.mercatia.bazaar.goods.Good.GoodType;
import org.mercatia.bazaar.market.Market.RoundStats;

/**
 */
public class History implements Jsonable {
	public HistoryLog<GoodType, Money> prices;
	public HistoryLog<GoodType, ValueRT> asks;
	public HistoryLog<GoodType, ValueRT> bids;
	public HistoryLog<GoodType, ValueRT> trades;
	public HistoryLog<Logic, Money> profit;

	public History() {
		prices = new HistoryLog<>(HistoryClass.Price);
		asks = new HistoryLog<>(HistoryClass.Ask);
		bids = new HistoryLog<>(HistoryClass.Bid);
		trades = new HistoryLog<>(HistoryClass.Trade);
		profit = new HistoryLog<>(HistoryClass.Profit);
	}

	private static record J(Jsony prices, Jsony asks, Jsony bids, Jsony trades, Jsony profit) implements Jsony {
	};

	@Override
	public Jsony jsonify() {
		return new J(prices.jsonify(), asks.jsonify(), bids.jsonify(), trades.jsonify(), profit.jsonify());
	}

    public void updateHistory(GoodType good, RoundStats roundStats) {
                // update history
        asks.add(good, ValueRT.of(roundStats.totalAskQty));
        bids.add(good, ValueRT.of(roundStats.totalBidQty));
        trades.add(good, ValueRT.of(roundStats.succesfulTrades));

        if (roundStats.succesfulTrades > 0) {
            var avgPrice = Money.from(Currency.DEFAULT, roundStats.moneyTraded.as() / roundStats.qtyTraded);
            prices.add(good, avgPrice);
        } else {
            // special case: none were traded this round, use last round's average price
            var avgPrice = prices.average(good, 1);
            prices.add(good, avgPrice);
        }
    }

}
