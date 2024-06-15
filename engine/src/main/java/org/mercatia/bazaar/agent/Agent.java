package org.mercatia.bazaar.agent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.Market;
import org.mercatia.bazaar.Offer;
import org.mercatia.bazaar.utils.Point;
import org.mercatia.bazaar.utils.Quick;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The most fundamental agent class, and has as little implementation as possible.
 * In most cases you should start by extending Agent instead of this.
 * @author larsiusprime
 */
public abstract class Agent
{


    public  String id;				//unique integer identifier
    public  String name;	//string identifier, "famer", "woodcutter", etc.
    public  float money;
    public  float moneyLastRound;
    
    public  float inventorySpace;
    public  boolean inventoryFull;
    public  boolean destroyed;

    Inventory inventory;
    Map<String, Point> priceBeliefs;
    Map<String, List<Float>> observedTradingRange;
    float profit =0.0f;
    int lookback = 15;

    public String toString(){
        StringBuilder sb = new StringBuilder("Agent").append("[");
        sb.append(id).append(":").append(money);
        sb.append("]");
        return sb.toString();
    }

    public Agent(){

    }

    public Agent(String id, AgentData data)
    {
        this.id = id;
        this.name = id;
        money = data.money;
        inventory = new Inventory();
        // inventory.fromData(data.inventory);
        lookback = 5;
        priceBeliefs = new HashMap<String,Point>();
        observedTradingRange = new HashMap<String,List<Float>>();
    }

    public void init(Market market)
    {
        List<Good> listGoods = market.getGoods();
        for (Good good: listGoods)
        {
             List<Float> trades = new ArrayList<Float>();

            float price = market.getAverageHistoricalPrice(good.id, lookback);
            trades.add(price * 0.5f);
            trades.add(price * 1.5f);	//push two fake trades to generate a range

            //set initial price belief & observed trading range
            observedTradingRange.put(good.id, trades);
            priceBeliefs.put(good.id, new Point(price * 0.5f, price * 1.5f));
        }
    }

    public abstract void simulate(Market market);

    public abstract void generateOffers(Market market, String good);

    public abstract void updatePriceModel(Market market, String act, String goodid, boolean success, float clearing_price);

    public abstract void updatePriceModel(Market market, String act, String goodid, boolean success);
    public abstract Offer createBid(Market market, String good, float limit);

    public abstract Offer createAsk(Market market, String commodity, float limit);


    public float queryInventory(String goodid)
    {
        return inventory.query(goodid);
    }

    public void changeInventory(String goodid, float delta)
    {
        inventory.change(goodid, delta);
    }

    // private float getInventorySpace()
    // {
    //     return inventory.getEmptySpace();
    // }

    public boolean isInventoryFull()
    {
        return inventory.getEmptySpace() == 0;
    }

    protected float getProfit()
    {
        return money - moneyLastRound;
    }

    protected float determinePriceOf(String commodity)
    {
        Point belief = priceBeliefs.get(commodity);
        return Quick.randomRange(belief.x, belief.y);
    }

    protected float determineSaleQuantity(Market bazaar, String commodity)
    {
        Float mean = bazaar.getAverageHistoricalPrice(commodity,lookback);
        Point trading_range = observeTradingRange(commodity);
        if (trading_range != null)
        {
            float favorability = Quick.positionInRange(mean, trading_range.x, trading_range.y); //check defaults
            //position_in_range: high means price is at a high point

            float amount_to_sell= Math.round(favorability * inventory.surplus(commodity));
            if (amount_to_sell < 1)
            {
                amount_to_sell = 1;
            }
            return amount_to_sell;
        }
        return 0;
    }

    protected float determinePurchaseQuantity(Market bazaar,String commodity)
    {
        float mean = bazaar.getAverageHistoricalPrice(commodity,lookback);
        Point trading_range = observeTradingRange(commodity);
        if (trading_range != null)
        {
            float favorability = Quick.positionInRange(mean, trading_range.x, trading_range.y);
            favorability = 1 - favorability;
            //do 1 - favorability to see how close we are to the low end

            float amount_to_buy = Math.round(favorability * inventory.shortage(commodity));
            if (amount_to_buy < 1)
            {
                amount_to_buy = 1;
            }
            return amount_to_buy;
        }
        return 0;
    }

    protected Point getPriceBelief(String good)
    {
        return priceBeliefs.get(good);
    }

    protected Point observeTradingRange(String good)
    {
         List<Float> a = observedTradingRange.get(good);
         Point pt = new Point(Quick.minArr(a), Quick.maxArr(a));
        return pt;
    }
}


