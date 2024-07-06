package org.mercatia.bazaar.agent;

import java.util.List;
import java.util.Map;

import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.Offer;
import org.mercatia.bazaar.currency.Currency;
import org.mercatia.bazaar.currency.Money;
import org.mercatia.bazaar.market.BasicMarket;
import org.mercatia.bazaar.market.Market;
import org.mercatia.bazaar.utils.ValueRT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mercatia.bazaar.utils.Range;
import org.mercatia.bazaar.utils.Range.LIMIT;

/**
 * An agent that performs the basic logic from the Doran & Parberry article
 * 
 * @author
 */
public class BasicAgent extends Agent {

	static Logger logger = LoggerFactory.getLogger(BasicAgent.class);

	public static double SIGNIFICANT = 0.25; // 25% more or less is "significant"
	public static Money SIGNIFICANT_MONEY = Money.from(Currency.DEFAULT, 0.25);

	public static double SIG_IMBALANCE = 0.33;
	public static double LOW_INVENTORY = 0.1; // 10% of ideal inventory = "LOW"
	public static double HIGH_INVENTORY = 2.0; // 200% of ideal inventory = "HIGH"

	public BasicAgent(String id, AgentData data, Map<String, Good> goods) {
		super(id, data, goods);
	}

	public Offer createBid(Market bazaar, String good, double limit) {
		Money bidPrice = determinePriceOf(good);
		double ideal = determinePurchaseQuantity(bazaar, good);

		// can't buy more than limit
		// double quantityToBuy = ideal > limit ? limit : ideal;
		double quantityToBuy = ideal > limit ? limit : ideal;
		if (quantityToBuy >= 1.0) {
			return new Offer(id, good, quantityToBuy, bidPrice);
		}
		return null;
	}

	public Offer createAsk(Market bazaar, String good, double limit) {
		Money ask_price = determinePriceOf(good);
		double ideal = determineSaleQuantity(bazaar, good);

		// can't sell less than limit
		// double quantity_to_sell = ideal < limit ? limit : ideal;
		double quantity_to_sell = ideal > limit ? limit : ideal;
		if (quantity_to_sell >= 1.0) {
			return new Offer(id, good, quantity_to_sell, ask_price);
		}
		return null;
	}

	public void generateOffers(Market bazaar, String commodity) {
		Offer offer;
		double surplus = inventory.surplus(commodity);
		if (surplus >= 1) {
			// logger.info("{} surplus {} {} ", this.name, commodity, surplus);
			offer = createAsk(bazaar, commodity, surplus);
			if (offer != null) {
				bazaar.ask(offer);
				logger.debug("{} offer {} ", this.name, offer);
			}
		} else {
			double shortage = inventory.shortage(commodity);
			double space = inventory.getEmptySpace();
			double unit_size = inventory.getCapacityFor(commodity);
			
			if (shortage > 0 && space >= unit_size) {
				// logger.info("{} shortage {} of {} inventorysize={}, unitsize={}" ,this.name,shortage,commodity, space,unit_size);
				double limit = 0;
				if ((shortage * unit_size) <= space) // enough space for ideal order
				{
					limit = shortage;
				} else // not enough space for ideal order
				{
					limit = (double) Math.floor(space / shortage);
				}

				if (limit > 0) {
					offer = createBid(bazaar, commodity, limit);
					if (offer != null) {
						bazaar.bid(offer);
						logger.debug("{} offer {} ", this.name, offer);
					}
				}
			} else if (shortage>0){
				inventoryFull = true;
				// logger.info("{} !!!!! shortage {} of {} inventorysize={}, unitsize={}" ,this.name,shortage,commodity, space,unit_size);
			}
		}
	}

	@Override
	public void updatePriceModel(Market market, Offer.Type act, String goodid, boolean success) {
		updatePriceModel(market, act, goodid, success, Money.from(Currency.DEFAULT, 0.0f));
	}

	public void updatePriceModel(Market bazaar, Offer.Type act, String good, boolean success, Money unitPrice) {
		List<Money> observed_trades;
		
		if (success) {
			// Add this to my list of observed trades
			observed_trades = observedTradingRange.get(good);
			observed_trades.add(unitPrice);
		}

		Money mean_price = bazaar.getAverageHistoricalPrice(good, 5);

		Range<Money> belief = getPriceBelief(good);
		Money mean = belief.mean();
		double wobble = 0.05;

		var delta_to_mean = mean.subtract(mean_price);
		// if (name.toLowerCase().equals("blacksmith"))
		// 	logger.info("{} {} market mean {}  my mean {}",this.name,good,mean_price ,mean);
		if (success) {
			if (act == Offer.Type.BUY && delta_to_mean.greater(SIGNIFICANT_MONEY)) // overpaid
			{
				var drop = delta_to_mean.multiply(0.5);
				belief.drop(drop);// SHIFT towards mean
				// belief.x -= delta_to_mean / 2; 
				// belief.y -= delta_to_mean / 2;
			} else if (act == Offer.Type.SELL && delta_to_mean.less(SIGNIFICANT_MONEY.multiply(-1.0))) // undersold
			{
				var drop = delta_to_mean.multiply(0.5);
				belief.drop(drop);
			}
			belief.raise(mean.multiply(wobble), LIMIT.LOWER);
			belief.drop(mean.multiply(wobble), LIMIT.UPPER);
			// belief.x += wobble * mean; // increase the belief's certainty
			// belief.y -= wobble * mean;
		} else {

			belief.drop(delta_to_mean.multiply(0.5f));
			// belief.x -= delta_to_mean / 2; // SHIFT towards the mean
			// belief.y -= delta_to_mean / 2;

			boolean special_case = false;
			double stocks = queryInventory(good);
			double ideal = inventory.ideal(good);

			if (act == Offer.Type.BUY && stocks < LOW_INVENTORY * ideal) {
				// very low on inventory AND can't buy
				wobble *= 2; // bid more liberally
				special_case = true;
			} else if (act == Offer.Type.SELL && stocks > HIGH_INVENTORY * ideal) {
				// very high on inventory AND can't sell
				wobble *= 2; // ask more liberally
				special_case = true;
			}

			if (!special_case) {
				// Don't know what else to do? Check supply vs. demand
				ValueRT asks = bazaar.getHistory().asks.average(good, 1);
				ValueRT bids = bazaar.getHistory().bids.average(good, 1);

				// supply_vs_demand: 0=balance, 1=all supply, -1=all demand
				double supply_vs_demand = (asks.as() - bids.as()) / (asks.as() + bids.as());

				// too much supply, or too much demand
				if (supply_vs_demand > SIG_IMBALANCE || supply_vs_demand < -SIG_IMBALANCE) {
					// too much supply: lower price
					// too much demand: raise price

					var new_mean = mean_price.multiply(1 - supply_vs_demand);
					delta_to_mean = mean.subtract(new_mean);
					belief.drop(delta_to_mean.multiply(0.5f));
					// belief.x -= delta_to_mean / 2; // SHIFT towards anticipated new mean
					// belief.y -= delta_to_mean / 2;
				}
			}

			belief.drop(mean.multiply(wobble), LIMIT.LOWER);
			belief.raise(mean.multiply(wobble), LIMIT.UPPER);
		}

		if (belief.getLower().less(MIN_PRICE)) {
			belief.setLower(MIN_PRICE);
		}

		if (belief.getUpper().less(MIN_PRICE)) {
			belief.setUpper(MIN_PRICE);
		}
	}

	@Override
	public void simulate(Market market) {
		// this.
	}

	public void produce(String commodity, double amount, double chance) {
		if (chance >= 1.0 || Math.random() < chance) {
			changeInventory(commodity, amount);
		}
	}

	public void produce(String commodity, double amount) {
		produce(commodity, amount, 1.0);
	}

	public void consume(String commodity, double amount) {
		consume(commodity, amount, 1.0);
	}

	public void consume(String commodity, double amount, double chance) {
		if (chance >= 1.0 || Math.random() < chance) {
			if (commodity == "money") {
				money = money.subtract(Money.from(Currency.DEFAULT, amount));
			} else {
				changeInventory(commodity, -amount);
			}
		}
	}
}
