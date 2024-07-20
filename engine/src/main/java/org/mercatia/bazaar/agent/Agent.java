package org.mercatia.bazaar.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.BootstrappedEntity;
import org.mercatia.bazaar.Offer;
import org.mercatia.bazaar.Transport;
import org.mercatia.bazaar.currency.Currency;
import org.mercatia.bazaar.currency.Money;
import org.mercatia.bazaar.goods.Good;
import org.mercatia.bazaar.goods.Good.GoodType;
import org.mercatia.bazaar.market.Market;
import org.mercatia.bazaar.utils.Quick;
import org.mercatia.bazaar.utils.Range;
import org.mercatia.bazaar.utils.Range.LIMIT;
import org.mercatia.bazaar.utils.Typed;
import org.mercatia.bazaar.utils.ValueRT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

/**
 * 
 */
public abstract class Agent extends BootstrappedEntity implements Jsonable {

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

    public static interface Logic extends Typed {
        public String label();
    };

    static Logger logger = LoggerFactory.getLogger(Agent.class);

    public static double SIGNIFICANT = 0.25; // 25% more or less is "significant"
    public static Money SIGNIFICANT_MONEY = Money.from(Currency.DEFAULT, 0.25);

    public static double SIG_IMBALANCE = 0.33;
    public static double LOW_INVENTORY = 0.1; // 10% of ideal inventory = "LOW"
    public static double HIGH_INVENTORY = 2.0; // 200% of ideal inventory = "HIGH"

    protected EventBus eventBus;
    protected String addr;
    public ID id; // unique integer identifier
    // public String name; // string identifier, "famer", "woodcutter", etc.
    protected Logic logic;

    public Money money;
    public Money moneyLastRound;

    public double inventorySpace;
    public boolean inventoryFull;
    public boolean destroyed;

    protected Inventory inventory;
    Map<GoodType, Range<Money>> priceBeliefs;
    Map<GoodType, List<Money>> observedTradingRange;

    int lookback = 15;

    public static Money MIN_PRICE = Money.from(Currency.DEFAULT, 0.01); // lowest possible price
    public static Money MAX_PRICE = null;

    protected record AgentJSON(String id, String name, double money, Jsony inventory, Map<String, Jsony> priceBeliefs,
            Map<String, List<Jsony>> observeredTradingRange) implements Jsony {
    };

    public Jsony jsonify() {
        var inv = inventory.jsonify();

        // Map<String, Jsony> pv =
        // priceBeliefs.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(),
        // v -> v.getValue().jsonify()));

        // Map<String, List<Jsony>> otr = observedTradingRange.entrySet().stream()
        // .collect(Collectors.toMap(e -> e.getKey(),
        // v -> v.getValue().stream().map(x ->
        // x.jsonify()).collect(Collectors.toList())));

        return null;// new AgentJSON(id.toString(), logic.label(), money.as(), inv, pv, otr);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Agent").append("[");
        sb.append(id).append("] ").append(String.format("%-15s", this.logic.label())).append(" money: ").append(money);
        sb.append(" previousMoney: ").append(this.moneyLastRound);
        sb.append(" inventory: ").append(inventory);

        return sb.toString();
    }

    public Agent() {
        this.id = new ID();
    }

    public Agent(Agent.Logic logic, Money startingMoney, Map<String, Good> goods) {
        this();
        this.logic = logic;
        money = startingMoney;

        lookback = 5;
        priceBeliefs = new HashMap<GoodType, Range<Money>>();
        observedTradingRange = new HashMap<GoodType, List<Money>>();
    }

    public void init(Market market, EventBus eventBus) {
        List<Good.GoodType> listGoods = market.getGoodsTraded();
        for (Good.GoodType good : listGoods) {
            List<Money> trades = new ArrayList<Money>();

            Money price = market.getAverageHistoricalPrice(good, lookback);
            trades.add(price.multiply(0.5));
            trades.add(price.multiply(1.5)); // push two fake trades to generate a range

            // set initial price belief & observed trading range
            observedTradingRange.put(good, trades);
            priceBeliefs.put(good,
                    new Range<Money>(price.multiply(0.5), price.multiply(1.5), MIN_PRICE, MAX_PRICE));
        }

        this.eventBus = eventBus;
        this.addr = String.format("economy/%s/market/%s/agent/%s", market.getEconomy().getName(), market.getName(),
                this.id.toString());

        MessageConsumer<JsonObject> consumer = eventBus.consumer(this.addr);
        consumer.handler(message -> {
            var busMsg = Transport.IntraMessage.busmsg(message);
            if (busMsg.isAction()) {
                var reply = new JsonObject();
                switch (busMsg.getAction()) {
                    case GET_AGENT:
                        reply = JsonObject.mapFrom(this.jsonify());
                        break;
                    default:
                        // logger.error("Unknown action");
                        message.fail(500, "Unknown action ");
                }

                message.reply(reply);
            }

        });

    }

    public ID getId() {
        return this.id;
    }

    public Agent.Logic getLogic() {
        return this.logic;
    }

    public Agent storeLastRound() {
        this.moneyLastRound = this.money;
        return this;
    }



    public Offer createBid(Market bazaar, GoodType good, double limit) {
        Money bidPrice = determinePriceOf(good);
        double ideal = determinePurchaseQuantity(bazaar, good);

        // can't buy more than limit
        // double quantityToBuy = ideal > limit ? limit : ideal;
        double quantityToBuy = ideal > limit ? limit : ideal;

        return new Offer(id, good, quantityToBuy, bidPrice);

    }

    public Offer createAsk(Market bazaar, GoodType good, double limit) {
        Money ask_price = determinePriceOf(good);
        double ideal = determineSaleQuantity(bazaar, good);

        // can't sell less than limit
        double quantity_to_sell = ideal < limit ? limit : ideal;
        logger.info("Selling {} surplues was {}", quantity_to_sell, limit);
        if (quantity_to_sell >= 1.0) {
            return new Offer(id, good, quantity_to_sell, ask_price);
        }
        return null;
    }

    public double queryInventory(GoodType goodid) {
        return inventory.query(goodid);
    }

    public void changeInventory(GoodType goodid, double delta) {
        inventory.change(goodid, delta);
    }

    public boolean isInventoryFull() {
        return inventory.getEmptySpace() == 0;
    }

    public Money getProfit() {
        return money.subtract(moneyLastRound);
    }

    protected Money determinePriceOf(GoodType commodity) {
        Range<Money> belief = priceBeliefs.get(commodity);
        return Quick.randomRange(belief.getLower(), belief.getUpper());
    }

    protected long determineSaleQuantity(Market bazaar, GoodType commodity) {
        Money mean = bazaar.getAverageHistoricalPrice(commodity, lookback);
        var trading_range = observeTradingRange(commodity);
        logger.info("Trading range is {}");
        if (trading_range != null) {
            double favorability = trading_range.positionInRange(mean);// Quick.positionInRange(mean, trading_range.x,
                                                                      // trading_range.y); // check defaults
                                                                      // position_in_range: high means price is at a
                                                                      // high point

            long amount_to_sell = Math.round(favorability * inventory.surplus(commodity));
            if (amount_to_sell < 1) {
                amount_to_sell = 1;
            }
            return amount_to_sell;
        }
        return 0;
    }

    protected long determinePurchaseQuantity(Market bazaar, GoodType commodity) {
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

    protected Range<Money> getPriceBelief(GoodType good) {
        return priceBeliefs.get(good);
    }

    protected Range<Money> observeTradingRange(GoodType good) {
        List<Money> a = observedTradingRange.get(good);
        Range<Money> pt = new Range<>(Quick.listMinR(a), Quick.listMaxR(a));
        return pt;
    }

    public void generateOffers(Market bazaar, GoodType commodity) {
        Offer offer;
        double surplus = inventory.surplus(commodity);
        if (surplus >= 1) {
            logger.info("{} surplus {} {} ", this.logic.label(), commodity, surplus);
            offer = createAsk(bazaar, commodity, surplus);
            if (offer != null) {
                bazaar.addOffer(offer);
                logger.debug("{} offer {} ", this.logic.label(), offer);
            }
        } else {
            double shortage = inventory.shortage(commodity);
            double space = inventory.getEmptySpace();
            double unit_size = inventory.getCapacityFor(commodity);

            if (shortage > 0 && space >= unit_size) {
                logger.info("{} shortage {} of {} inventorysize={}, unitsize={}", this.logic.label(), shortage,
                        commodity, space, unit_size);
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
                        bazaar.addOffer(offer);
                        logger.debug("{} offer {} ", this.logic.label(), offer);
                    }
                }
            } else if (shortage > 0) {
                inventoryFull = true;
                logger.info("{} !!!!! shortage {} of {} inventorysize={}, unitsize={}", this.logic.label(), shortage,
                        commodity, space, unit_size);
            }
        }

    }

    public void updatePriceModel(Market bazaar, Offer.Type act, Good.GoodType good, boolean success, Money unitPrice) {
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
        // logger.info("{} {} market mean {} my mean {}",this.name,good,mean_price
        // ,mean);
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

    public void produce(GoodType commodity, double amount, double chance) {
        if (chance >= 1.0 || Math.random() < chance) {
            changeInventory(commodity, amount);
        }
    }

    public void produce(GoodType commodity, double amount) {
        produce(commodity, amount, 1.0);
    }

    public void consume(GoodType commodity, double amount) {
        consume(commodity, amount, 1.0);
    }

    public void consume(GoodType commodity, double amount, double chance) {
        if (chance >= 1.0 || Math.random() < chance) {
            if (commodity == null) {
                money = money.subtract(Money.from(Currency.DEFAULT, amount));
            } else {
                changeInventory(commodity, -amount);
            }
        }
    }


    public abstract void simulate(Market market);

}
