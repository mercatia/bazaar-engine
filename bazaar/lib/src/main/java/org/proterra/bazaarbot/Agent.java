package org.proterra.bazaarbot;

import java.util.List;

import org.proterra.bazaarbot.agent.AgentData;
import org.proterra.bazaarbot.agent.BasicAgent;


/**
 * An agent that performs the basic logic from the Doran & Parberry article
 * @author
 */
public class Agent extends BasicAgent
{
	public static  float SIGNIFICANT = 0.25f;		//25% more or less is "significant"
	public static float  SIG_IMBALANCE = 0.33f;
	public static float  LOW_INVENTORY = 0.1f;		//10% of ideal inventory = "LOW"
	public static float  HIGH_INVENTORY = 2.0f;	//200% of ideal inventory = "HIGH"

	public static float  MIN_PRICE = 0.01f;		//lowest possible price

	public Agent(int id, AgentData data)
	{
		super(id, data);
	}

	 public Offer createBid(Market bazaar, String good, float limit)
	{
		float bidPrice = determinePriceOf(good);
		float ideal = determinePurchaseQuantity(bazaar, good);

		//can't buy more than limit
		float quantityToBuy = ideal > limit ? limit : ideal;
		if (quantityToBuy > 0)
		{
			return new Offer(id, good, quantityToBuy, bidPrice);
		}
		return null;
	}

	 public Offer createAsk(Market bazaar, String commodity, float limit)
	{
		float ask_price = determinePriceOf(commodity);
		float ideal = determineSaleQuantity(bazaar, commodity);

		//can't sell less than limit
		float quantity_to_sell = ideal < limit_ ? limit_ : ideal;
		if (quantity_to_sell > 0)
		{
			return new Offer(id, commodity_, quantity_to_sell, ask_price);
		}
		return null;
	}

	 public void generateOffers(bazaar:Market, commodity:String):Void
	{
		 offer:Offer;
		 surplus = _inventory.surplus(commodity);
		if (surplus >= 1)
		{
			 offer = createAsk(bazaar, commodity, 1);
			 if (offer != null)
			 {
				bazaar.ask(offer);
			 }
		}
		else
		{
			 shortage = _inventory.shortage(commodity);
			 space = _inventory.getEmptySpace();
			 unit_size = _inventory.getCapacityFor(commodity);

			if (shortage > 0 && space >= unit_size)
			{
				 limit = 0;
				if ((shortage * unit_size) <= space)	//enough space for ideal order
				{
					limit = shortage;
				}
				else									//not enough space for ideal order
				{
					limit = Math.floor(space / shortage);
				}

				if (limit > 0)
				{
					offer = createBid(bazaar, commodity, limit);
					if (offer != null)
					{
						bazaar.bid(offer);
					}
				}
			}
		}
	}

	 public void updatePriceModel(Market bazaar,String act,String good,boolean success,float unitPrice)
	{
		List<Float> observed_trades;

		if (success)
		{
			//Add this to my list of observed trades
			observed_trades = observedTradingRange.get(good);
			observed_trades.push(unitPrice);
		}

		 public_mean_price = bazaar.getAverageHistoricalPrice(good, 1);

		 belief:Point = getPriceBelief(good);
		 mean = (belief.x + belief.y) / 2;
		 wobble = 0.05;

		 delta_to_mean = mean - public_mean_price;

		if (success)
		{
			if (act == "buy" && delta_to_mean > SIGNIFICANT)			//overpaid
			{
				belief.x -= delta_to_mean / 2;							//SHIFT towards mean
				belief.y -= delta_to_mean / 2;
			}
			else if (act == "sell" && delta_to_mean < -SIGNIFICANT)		//undersold
			{
				belief.x -= delta_to_mean / 2;							//SHIFT towards mean
				belief.y -= delta_to_mean / 2;
			}

			belief.x += wobble * mean;	//increase the belief's certainty
			belief.y -= wobble * mean;
		}
		else
		{
			belief.x -= delta_to_mean / 2;	//SHIFT towards the mean
			belief.y -= delta_to_mean / 2;

			 special_case:Bool = false;
			 stocks = queryInventory(good);
			 ideal = _inventory.ideal(good);

			if (act == "buy" && stocks < LOW_INVENTORY * ideal)
			{
				//very low on inventory AND can't buy
				wobble *= 2;			//bid more liberally
				special_case = true;
			}
			else if (act == "sell" && stocks > HIGH_INVENTORY * ideal)
			{
				//very high on inventory AND can't sell
				wobble *= 2;			//ask more liberally
				special_case = true;
			}

			if (!special_case)
			{
				//Don't know what else to do? Check supply vs. demand
				 asks = bazaar.history.asks.average(good,1);
				 bids = bazaar.history.bids.average(good,1);

				//supply_vs_demand: 0=balance, 1=all supply, -1=all demand
				 supply_vs_demand = (asks - bids) / (asks + bids);

				//too much supply, or too much demand
				if (supply_vs_demand > SIG_IMBALANCE || supply_vs_demand < -SIG_IMBALANCE)
				{
					//too much supply: lower price
					//too much demand: raise price

					 new_mean = public_mean_price * (1 - supply_vs_demand);
					delta_to_mean = mean - new_mean;

					belief.x -= delta_to_mean / 2;	//SHIFT towards anticipated new mean
					belief.y -= delta_to_mean / 2;
				}
			}

			belief.x -= wobble * mean;	//decrease the belief's certainty
			belief.y += wobble * mean;
		}

		if (belief.x < MIN_PRICE)
		{
			belief.x = MIN_PRICE;
		}
		else if (belief.y < MIN_PRICE)
		{
			belief.y = MIN_PRICE;
		}
	}
}
