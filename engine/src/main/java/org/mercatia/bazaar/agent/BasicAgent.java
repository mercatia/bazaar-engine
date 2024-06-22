package org.mercatia.bazaar.agent;

import java.util.List;
import java.util.Map;

import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.Market;
import org.mercatia.bazaar.Offer;
import org.mercatia.bazaar.currency.Currency;
import org.mercatia.bazaar.currency.Money;
import org.mercatia.bazaar.utils.ValueRT;
import org.mercatia.bazaar.utils.Range;
import org.mercatia.bazaar.utils.Range.LIMIT;

/**
 * An agent that performs the basic logic from the Doran & Parberry article
 * 
 * @author
 */
public class BasicAgent extends Agent {
	public static float SIGNIFICANT = 0.25f; // 25% more or less is "significant"
	public static Money SIGNIFICANT_MONEY = Money.from(Currency.DEFAULT, 0.25f);

	public static float SIG_IMBALANCE = 0.33f;
	public static float LOW_INVENTORY = 0.1f; // 10% of ideal inventory = "LOW"
	public static float HIGH_INVENTORY = 2.0f; // 200% of ideal inventory = "HIGH"

	public static Money MIN_PRICE = Money.from(Currency.DEFAULT, 0.01f); // lowest possible price

	public BasicAgent(String id, AgentData data, Map<String, Good> goods) {
		super(id, data, goods);
	}

	public Offer createBid(Market bazaar, String good, float limit) {
		Money bidPrice = super.determinePriceOf(good);
		float ideal = determinePurchaseQuantity(bazaar, good);

		// can't buy more than limit
		float quantityToBuy = ideal > limit ? limit : ideal;
		if (quantityToBuy > 0) {
			return new Offer(id, good, quantityToBuy, bidPrice);
		}
		return null;
	}

	public Offer createAsk(Market bazaar, String commodity, float limit) {
		Money ask_price = determinePriceOf(commodity);
		float ideal = determineSaleQuantity(bazaar, commodity);

		// can't sell less than limit
		float quantity_to_sell = ideal < limit ? limit : ideal;
		if (quantity_to_sell > 0) {
			return new Offer(id, commodity, quantity_to_sell, ask_price);
		}
		return null;
	}

	public void generateOffers(Market bazaar, String commodity) {
		Offer offer;
		float surplus = inventory.surplus(commodity);
		if (surplus >= 1) {
			offer = createAsk(bazaar, commodity, 1);
			if (offer != null) {
				bazaar.ask(offer);
			}
		} else {
			float shortage = inventory.shortage(commodity);
			float space = inventory.getEmptySpace();
			float unit_size = inventory.getCapacityFor(commodity);

			if (shortage > 0 && space >= unit_size) {
				float limit = 0;
				if ((shortage * unit_size) <= space) // enough space for ideal order
				{
					limit = shortage;
				} else // not enough space for ideal order
				{
					limit = (float) Math.floor(space / shortage);
				}

				if (limit > 0) {
					offer = createBid(bazaar, commodity, limit);
					if (offer != null) {
						bazaar.bid(offer);
					}
				}
			}
		}
	}

	@Override
	public void updatePriceModel(Market market, String act, String goodid, boolean success) {
		updatePriceModel(market, act, goodid, success, Money.from(Currency.DEFAULT, 0.0f));
	}

	public void updatePriceModel(Market bazaar, String act, String good, boolean success, Money unitPrice) {
		List<Money> observed_trades;

		if (success) {
			// Add this to my list of observed trades
			observed_trades = observedTradingRange.get(good);
			observed_trades.add(unitPrice);
		}

		Money mean_price = bazaar.getAverageHistoricalPrice(good, 1);

		Range<Money> belief = getPriceBelief(good);
		Money mean = belief.mean();
		float wobble = 0.05f;

		var delta_to_mean = mean.subtract(mean_price);

		if (success) {
			if (act == "buy" && delta_to_mean.greater(SIGNIFICANT_MONEY)) // overpaid
			{
				var drop = delta_to_mean.multiply(0.5f);
				belief.drop(drop);// SHIFT towards mean
				// belief.x -= delta_to_mean / 2; 
				// belief.y -= delta_to_mean / 2;
			} else if (act == "sell" && delta_to_mean.less(SIGNIFICANT_MONEY.multiply(-1.0f))) // undersold
			{
				var drop = delta_to_mean.multiply(0.5f);
				belief.drop(drop);
			}
			belief.raise(mean.multiply(wobble),LIMIT.LOWER);
			belief.drop(mean.multiply(wobble),LIMIT.UPPER);
			// belief.x += wobble * mean; // increase the belief's certainty
			// belief.y -= wobble * mean;
		} else {

			belief.drop(delta_to_mean.multiply(0.5f));
			// belief.x -= delta_to_mean / 2; // SHIFT towards the mean
			// belief.y -= delta_to_mean / 2;

			boolean special_case = false;
			float stocks = queryInventory(good);
			float ideal = inventory.ideal(good);

			if (act == "buy" && stocks < LOW_INVENTORY * ideal) {
				// very low on inventory AND can't buy
				wobble *= 2; // bid more liberally
				special_case = true;
			} else if (act == "sell" && stocks > HIGH_INVENTORY * ideal) {
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

			belief.drop(mean.multiply(wobble),LIMIT.LOWER);
			belief.raise(mean.multiply(wobble),LIMIT.UPPER);
			// belief.x -= wobble * mean; // decrease the belief's certainty
			// belief.y += wobble * mean;
		}

		if (belief.getLower().less(MIN_PRICE)) {
			belief.setLower(MIN_PRICE);
		} else if (belief.getUpper().less(MIN_PRICE)) {
			belief.setUpper(MIN_PRICE);
		}
	}

	@Override
	public void simulate(Market market) {
		// this.
	}

	public void produce(String commodity, float amount, float chance) {
		if (chance >= 1.0 || Math.random() < chance) {
			changeInventory(commodity, amount);
		}
	}

	public void produce(String commodity, float amount) {
		produce(commodity, amount, 1.0f);
	}

	public void consume(String commodity, float amount) {
		consume(commodity, amount, 1.0f);
	}

	public void consume(String commodity, float amount, float chance) {
		if (chance >= 1.0 || Math.random() < chance) {
			if (commodity == "money") {
				money = money.subtract(Money.from(Currency.DEFAULT, amount));
			} else {
				changeInventory(commodity, -amount);
			}
		}
	}
}
