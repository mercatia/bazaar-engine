package org.proterra.bazaarbot.agent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.proterra.bazaarbot.Market;
import org.proterra.bazaarbot.agent.Inventory;
// import org.proterra.bazaarbot.utils.EconNoun;
// import org.proterra.bazaarbot.utils.Quick;
// import openfl.Assets;
// import openfl.geom.Point;
import org.proterra.bazaarbot.agent.InventoryData;
import org.proterra.bazaarbot.agent.Logic;

/**
 * The most fundamental agent class, and has as little implementation as possible.
 * In most cases you should start by extending Agent instead of this.
 * @author larsiusprime
 */

public abstract class BasicAgent
{
	public  int id;				//unique integer identifier
	public  String name;	//string identifier, "famer", "woodcutter", etc.
	public  float money;
	public  float moneyLastRound;
	
	public  float inventorySpace;
	public  boolean inventoryFull;
	public  boolean destroyed;

	Inventory inventory;
	Map<String, Float> priceBeliefs;
	Map<String, List<Float>> observedTradingRange;
	float profit =0.0f;
	int lookback = 15;

	public BasicAgent(int id, AgentData data)
	{
		this.id = id;
		this.name = data.className;
		money = data.money;
		inventory = new Inventory();
		inventory.fromData(data.inventory);
		lookback = data.lookBack;
		priceBeliefs = new HashMap();
		observedTradingRange = new HashMap();
	}

	public void init(Market market)
	{
		listGoods = market.getGoods();
		for (Good good: listGoods)
		{
			 trades:Array<Float> = new Array<Float>();

			 price:Float = market.getAverageHistoricalPrice(str, _lookback);
			trades.push(price * 0.5);
			trades.push(price * 1.5);	//push two fake trades to generate a range

			//set initial price belief & observed trading range
			_observedTradingRange.set(str, trades);
			_priceBeliefs.set(str, new Point(price * 0.5, price * 1.5));
		}
	}

	public abstract void simulate(Market market);

	public abstract void generateOffers(Market market, String good);

	public abstract void updatePriceModel(Market market, String act, String good, boolean success, float unitPrice);

	public abstract Offer createBid(Market market, String good, float limit);

	public abstract Offer createAsk(Market market, String commodity, float limit);


	public float queryInventory(String goodid)
	{
		return inventory.query(goodid);
	}

	public void changeInventory(String goodid, float delta)
	{
		inventory.change(good, delta);
	}

	/********PRIVATE************/

	private float getInventorySpace()
	{
		return inventory.getEmptySpace();
	}

	public boolean isInventoryFull()
	{
		return inventory.getEmptySpace() == 0;
	}

	private float getProfit()
	{
		return money - moneyLastRound;
	}

	private float determinePriceOf(String commodity)
	{
		 belief:Point = _priceBeliefs.get(commodity_);
		return Quick.randomRange(belief.x, belief.y);
	}

	private  determineSaleQuantity(bazaar:Market, commodity_:String):Float
	{
		 mean:Float = bazaar.getAverageHistoricalPrice(commodity_,_lookback);
		 trading_range:Point = observeTradingRange(commodity_);
		if (trading_range != null)
		{
			 favorability:Float = Quick.positionInRange(mean, trading_range.x, trading_range.y);
			//position_in_range: high means price is at a high point

			 amount_to_sell:Float = Math.round(favorability * _inventory.surplus(commodity_));
			if (amount_to_sell < 1)
			{
				amount_to_sell = 1;
			}
			return amount_to_sell;
		}
		return 0;
	}

	private  determinePurchaseQuantity(bazaar:Market, commodity_:String):Float
	{
		 mean:Float = bazaar.getAverageHistoricalPrice(commodity_,_lookback);
		 trading_range:Point = observeTradingRange(commodity_);
		if (trading_range != null)
		{
			 favorability:Float = Quick.positionInRange(mean, trading_range.x, trading_range.y);
			favorability = 1 - favorability;
			//do 1 - favorability to see how close we are to the low end

			 amount_to_buy:Float = Math.round(favorability * _inventory.shortage(commodity_));
			if (amount_to_buy < 1)
			{
				amount_to_buy = 1;
			}
			return amount_to_buy;
		}
		return 0;
	}

	private  getPriceBelief(good:String):Point
	{
		return _priceBeliefs.get(good);
	}

	private  observeTradingRange(good:String):Point
	{
		 a:Array<Float> = _observedTradingRange.get(good);
		 pt:Point = new Point(Quick.minArr(a), Quick.maxArr(a));
		return pt;
	}
}


