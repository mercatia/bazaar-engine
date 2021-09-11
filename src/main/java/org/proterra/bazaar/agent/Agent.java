package org.proterra.bazaar.agent;

import java.util.List;

import org.proterra.bazaar.Market;
import org.proterra.bazaar.Offer;
import org.proterra.bazaar.utils.Point;

/**
 * An agent that performs the basic logic from the Doran & Parberry article
 * 
 * @author
 */
public class Agent extends BasicAgent {
	public static float SIGNIFICANT = 0.25f; // 25% more or less is "significant"
	public static float SIG_IMBALANCE = 0.33f;
	public static float LOW_INVENTORY = 0.1f; // 10% of ideal inventory = "LOW"
	public static float HIGH_INVENTORY = 2.0f; // 200% of ideal inventory = "HIGH"

	public static float MIN_PRICE = 0.01f; // lowest possible price

	public Agent(int id, AgentData data) {
		super(id, data);
	}

	public Offer createBid(Market bazaar, String good, float limit) {
		float bidPrice = super.determinePriceOf(good);
		float ideal = determinePurchaseQuantity(bazaar, good);

		// can't buy more than limit
		float quantityToBuy = ideal > limit ? limit : ideal;
		if (quantityToBuy > 0) {
			return new Offer(id, good, quantityToBuy, bidPrice);
		}
		return null;
	}

	public Offer createAsk(Market bazaar, String commodity, float limit) {
		float ask_price = determinePriceOf(commodity);
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
		updatePriceModel(market,act,goodid,success,0.0f);
	}

	public void updatePriceModel(Market bazaar, String act, String good, boolean success, float unitPrice) {
		List<Float> observed_trades;

		if (success) {
			// Add this to my list of observed trades
			observed_trades = observedTradingRange.get(good);
			observed_trades.add(unitPrice);
		}

		float mean_price = bazaar.getAverageHistoricalPrice(good, 1);

		Point belief = getPriceBelief(good);
		float mean = (belief.x + belief.y) / 2;
		float wobble = 0.05f;

		float delta_to_mean = mean - mean_price;

		if (success) {
			if (act == "buy" && delta_to_mean > SIGNIFICANT) // overpaid
			{
				belief.x -= delta_to_mean / 2; // SHIFT towards mean
				belief.y -= delta_to_mean / 2;
			} else if (act == "sell" && delta_to_mean < -SIGNIFICANT) // undersold
			{
				belief.x -= delta_to_mean / 2; // SHIFT towards mean
				belief.y -= delta_to_mean / 2;
			}

			belief.x += wobble * mean; // increase the belief's certainty
			belief.y -= wobble * mean;
		} else {
			belief.x -= delta_to_mean / 2; // SHIFT towards the mean
			belief.y -= delta_to_mean / 2;

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
				float asks = bazaar.getHistory().asks.average(good, 1);
				float bids = bazaar.getHistory().bids.average(good, 1);

				// supply_vs_demand: 0=balance, 1=all supply, -1=all demand
				float supply_vs_demand = (asks - bids) / (asks + bids);

				// too much supply, or too much demand
				if (supply_vs_demand > SIG_IMBALANCE || supply_vs_demand < -SIG_IMBALANCE) {
					// too much supply: lower price
					// too much demand: raise price

					float new_mean = mean_price * (1 - supply_vs_demand);
					delta_to_mean = mean - new_mean;

					belief.x -= delta_to_mean / 2; // SHIFT towards anticipated new mean
					belief.y -= delta_to_mean / 2;
				}
			}

			belief.x -= wobble * mean; // decrease the belief's certainty
			belief.y += wobble * mean;
		}

		if (belief.x < MIN_PRICE) {
			belief.x = MIN_PRICE;
		} else if (belief.y < MIN_PRICE) {
			belief.y = MIN_PRICE;
		}
	}

	@Override
	public void simulate(Market market) {
		// TODO Auto-generated method stub

	}


}
