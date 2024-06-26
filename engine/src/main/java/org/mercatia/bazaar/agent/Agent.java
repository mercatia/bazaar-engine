package org.mercatia.bazaar.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.Market;
import org.mercatia.bazaar.Offer;
import org.mercatia.bazaar.currency.Money;
import org.mercatia.bazaar.utils.Quick;
import org.mercatia.bazaar.utils.Range;

/**
 * The most fundamental agent class, and has as little implementation as
 * possible.
 * In most cases you should start by extending Agent instead of this.
 * 
 * @author larsiusprime
 */
public abstract class Agent implements Jsonable {

    public static class ID {

        private UUID id;

        public ID() {
            this.id = UUID.randomUUID();
        }

        private ID(String textID) {
            this.id = UUID.fromString(textID);
        }

        public boolean equals(ID other) {
            return other.id.equals(this.id);
        }

        public String toString() {
            return id.toString();
        }

        public static ID from(String textId) {
            var id = new ID(textId);
            return id;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ID other = (ID) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            return true;
        }

    }

    public ID id; // unique integer identifier
    public String name; // string identifier, "famer", "woodcutter", etc.
    public Money money;
    public Money moneyLastRound;

    public double inventorySpace;
    public boolean inventoryFull;
    public boolean destroyed;

    Inventory inventory;
    Map<String, Range<Money>> priceBeliefs;
    Map<String, List<Money>> observedTradingRange;
    double profit = 0.0f;
    int lookback = 15;

    protected record AgentJSON(String id, String name, double money, Jsony inventory, Map<String, Jsony> priceBeliefs,
            Map<String, List<Jsony>> observeredTradingRange) implements Jsony {
    };

    public Jsony jsonify() {
        var inv = inventory.jsonify();

        Map<String, Jsony> pv = priceBeliefs.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(),
                v -> v.getValue().jsonify()));

        Map<String, List<Jsony>> otr = observedTradingRange.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(),
                        v -> v.getValue().stream().map(x -> x.jsonify()).collect(Collectors.toList())));

        return new AgentJSON(id.toString(), name, money.as(), inv, pv, otr);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Agent").append("[");
        sb.append(id).append("/").append(this.name).append(" money:").append(money);
        sb.append(" profit:").append(profit);
        sb.append(" inventory:").append(inventory);
        sb.append("]");
        return sb.toString();
    }

    public Agent() {
        this.id = new ID();
    }

    public Agent(String name, AgentData data, Map<String, Good> goods) {
        this();
        this.name = name;
        money = data.getMoney();

        inventory = Inventory.builderFromData(data.inventory, goods);

        lookback = 5;
        priceBeliefs = new HashMap<String, Range<Money>>();
        observedTradingRange = new HashMap<String, List<Money>>();
    }

    public void init(Market market) {
        List<Good> listGoods = market.getGoods();
        for (Good good : listGoods) {
            List<Money> trades = new ArrayList<Money>();

            Money price = market.getAverageHistoricalPrice(good.id, lookback);
            trades.add(price.multiply(0.5f));
            trades.add(price.multiply(1.5f)); // push two fake trades to generate a range

            // set initial price belief & observed trading range
            observedTradingRange.put(good.id, trades);
            priceBeliefs.put(good.id, new Range<Money>(price.multiply(0.5f), price.multiply(1.5f)));
        }
    }

    public ID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public abstract void simulate(Market market);

    public abstract void generateOffers(Market market, String good);

    public abstract void updatePriceModel(Market market, String act, String goodid, boolean success,
            Money clearing_price);

    public abstract void updatePriceModel(Market market, String act, String goodid, boolean success);

    public abstract Offer createBid(Market market, String good, double limit);

    public abstract Offer createAsk(Market market, String commodity, double limit);

    public double queryInventory(String goodid) {
        return inventory.query(goodid);
    }

    public void changeInventory(String goodid, double delta) {
        inventory.change(goodid, delta);
    }

    // private double getInventorySpace()
    // {
    // return inventory.getEmptySpace();
    // }

    public boolean isInventoryFull() {
        return inventory.getEmptySpace() == 0;
    }

    protected Money getProfit() {
        return money.subtract(moneyLastRound);
    }

    protected Money determinePriceOf(String commodity) {
        Range<Money> belief = priceBeliefs.get(commodity);
        return Quick.randomRange(belief.getLower(), belief.getUpper());
    }

    protected long determineSaleQuantity(Market bazaar, String commodity) {
        Money mean = bazaar.getAverageHistoricalPrice(commodity, lookback);
        var trading_range = observeTradingRange(commodity);
        if (trading_range != null) {
            double favorability = trading_range.positionInRange(mean);// Quick.positionInRange(mean, trading_range.x,
                                                                      // trading_range.y); // check defaults
                                                                      // position_in_range: high means price is at a high point

            long amount_to_sell = Math.round(favorability * inventory.surplus(commodity));
            if (amount_to_sell < 1) {
                amount_to_sell = 1;
            }
            return amount_to_sell;
        }
        return 0;
    }

    protected long determinePurchaseQuantity(Market bazaar, String commodity) {
        Money mean = bazaar.getAverageHistoricalPrice(commodity, lookback);
        var trading_range = observeTradingRange(commodity);
        if (trading_range != null) {
            double favorability = trading_range.positionInRange(mean);// Quick.positionInRange(mean,
                                                                      // trading_range.lower, trading_range.upper);
            favorability = 1 - favorability;
            // do 1 - favorability to see how close we are to the low end

            long amount_to_buy = Math.round(favorability * inventory.shortage(commodity));
            if (amount_to_buy < 1) {
                amount_to_buy = 1;
            }
            return amount_to_buy;
        }
        return 0;
    }

    protected Range<Money> getPriceBelief(String good) {
        return priceBeliefs.get(good);
    }

    protected Range<Money> observeTradingRange(String good) {
        List<Money> a = observedTradingRange.get(good);        
        Range<Money> pt = new Range<>(Quick.listMinR(a), Quick.listMaxR(a));
        System.out.println(pt);
        return pt;
    }
}
