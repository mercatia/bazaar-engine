package org.mercatia.bazaar.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.BootstrappedEntity;
import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.Offer;
import org.mercatia.bazaar.Transport;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.Agent.ID;
import org.mercatia.bazaar.currency.Currency;
import org.mercatia.bazaar.currency.Money;
import org.mercatia.bazaar.goods.Good;
import org.mercatia.bazaar.goods.Good.GoodType;
import org.mercatia.bazaar.utils.History;
import org.mercatia.bazaar.utils.Tick;
import org.mercatia.bazaar.utils.TradeBook;
import org.mercatia.bazaar.utils.ValueRT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public abstract class Market extends BootstrappedEntity implements Jsonable {

    static Logger logger = LoggerFactory.getLogger(Market.class);

    protected String name;

    /** Logs information about all economic activity in this market **/
    public History history;
   
    /* All the good types for trade in this market */    
    protected List<Good.GoodType> goodTypes;

    protected Map<ID, Agent> _agents;
    protected TradeBook _book;

    protected ReentrantLock mutex = new ReentrantLock();
    protected TradeBook lastResolvedTradeBook;

    protected Map<String, Good> _mapGoods;
    protected Economy economy;
    protected EventBus eventBus;
    protected String addr;
    // protected MarketData startingData;

    public Market(String name, Economy economy) {
        this.name = name;
        this.eventBus = vertx.eventBus();
        this.addr = String.format("economy/%s/market/%s", economy.getName(), this.name);
        this.economy = economy;
        // this.startingData = data;

        history = new History();
        _book = new TradeBook();

        goodTypes = getBootstrap().getGoodFactory(this).listTypes();
        _agents = new HashMap<ID, Agent>();
        _mapGoods = new HashMap<String, Good>();

        // Create starting points for all good types.
        for (var g : goodTypes) {
            history.prices.add(g, Money.from(Currency.DEFAULT, 1.0));
            history.asks.add(g, ValueRT.of(1.0));
            history.bids.add(g, ValueRT.of(1.0));
            history.trades.add(g, ValueRT.of(1.0));
        }

        // Create the default set of agents
        var af = getBootstrap().getAgentFactory(this);
        af.getStartingAgents().forEach(agent -> _agents.put(agent.getId(),agent));

        MessageConsumer<JsonObject> consumer = eventBus.consumer(this.addr);
        consumer.handler(message -> {
            var busMsg = Transport.IntraMessage.busmsg(message);
            if (busMsg.isAction()) {
                var reply = new JsonObject();
                switch (busMsg.getAction()) {
                    case GET_MARKET:
                        reply = JsonObject.mapFrom(this.jsonify());
                        break;
                    case GET_ALL_AGENTS:
                        var arr = new JsonArray();
                        this.getAgents().forEach(v -> {
                            var agent = this.getAgent(v);
                            var json = JsonObject.mapFrom(agent.jsonify());
                            arr.add(json);
                        });
                        reply.put("agents", arr);
                        break;
                    default:
                        logger.error("Unknown action");
                        message.fail(500, "Unknown action ");
                }

                message.reply(reply);
            }

        });

    }

    public void addAgent(Agent.Logic type) {
        var agent = getBootstrap().getAgentFactory(this).withLogic(type).build();
        this._agents.put(agent.getId(), agent);
    }

    /** Run the main simulation */
    public void simulate(Tick pointInTime) {
        _book.clear();
        try {

            mutex.lock();

            for (Agent agent : _agents.values()) {
                agent.storeLastRound().simulate(this);
                for (var good : goodTypes) {
                    agent.generateOffers(this, good);
                }
                logger.info("Generated offers");
            }

            for (var good : goodTypes) {
                resolveOffers(good);
            }

            recordAgentProfit();

            // determine if any new agents are required
            var toRemove = new ArrayList<ID>();
            for (Agent agent : _agents.values()) {
                logger.info(agent.toString());
                if (agent.money.zeroOrLess()) {
                    toRemove.add(agent.id);
                }
            }

            logger.info("Agents: {} before removing BANKRUPT {} ", _agents.size(), toRemove);
            for (var id : toRemove) {
                this.economy.onBankruptcy(this, _agents.remove(id));
            }
            logger.info("---");
            if (_agents.size() <= 2) {
                System.exit(-1);
            }
        } finally {
            mutex.unlock();
        }
    }

    public Agent getAgent(ID id) {
        return this._agents.get(id);
    }

    public Set<ID> getAgents() {
        return this._agents.keySet();
    }

    public int numAgents() {
        return _agents.size();
    }

    public void addOffer(Offer offer) {
        _book.addOffer(offer);
    }

    /**
     * Returns the historical mean price of the given commodity over the last X
     * rounds
     * 
     * @param commodity_ string id of commodity
     * @param range      number of rounds to look back
     * @return
     */
    public Money getAverageHistoricalPrice(GoodType good, int range) {
        return history.prices.average(good, range);
    }

    /**
     * Get the good with the highest demand/supply ratio over time
     * 
     * @param minimum the minimum demand/supply ratio to consider an opportunity
     * @param range   number of rounds to look back
     * @return
     */
    public GoodType getHottestGood(double minimum, int range) {
        GoodType best_market = null;
        double best_ratio = Double.NEGATIVE_INFINITY;

        for (var goodid : goodTypes) {
            double asks = history.asks.average(goodid, range).as();
            double bids = history.bids.average(goodid, range).as();

            double ratio = 0;
            if (asks == 0 && bids > 0) {
                // If there are NONE on the market we artificially create a fake supply of 1/2 a
                // unit to avoid the
                // crazy bias that "infinite" demand can cause...

                asks = 0.5f;
            }

            ratio = bids / asks;

            if (ratio > minimum && ratio > best_ratio) {
                best_ratio = ratio;
                best_market = goodid;
            }
        }
        return best_market;
    }

    /**
     * Returns the good that has the lowest average price over the given range of
     * time
     * 
     * @param range   how many rounds to look back
     * @param exclude goods to exclude
     * @return
     */
    public GoodType getCheapestGood(int range, List<String> exclude) {
        double best_price = Double.POSITIVE_INFINITY;
        GoodType best_good = null;
        for (GoodType goodid : goodTypes) {
            if (exclude == null || exclude.indexOf(goodid) == -1) {
                double price = history.prices.average(goodid, range).as();
                if (price < best_price) {
                    best_price = price;
                    best_good = goodid;
                }
            }
        }
        return best_good;
    }

    /**
     *
     * @param range
     * @return
     */
    public Agent.Logic getMostProfitableAgentClass(int range) {
        double best = Double.NEGATIVE_INFINITY;
        Agent.Logic bestLogic = null;
        var logicTypes = getBootstrap().getAgentFactory(this).listLogicTypes();

        for (var logic : logicTypes) {
            double val = history.profit.average(logic, range).as();
            logger.info("{} {}", logic.label(), val);
            if (val > best) {
                bestLogic = logic;
                best = val;
            }
        }
        return bestLogic;
    }

    /**
     * For all agents record the average profit
     */
    protected void recordAgentProfit() {
        Map<Agent.Logic, List<Money>> calc = new HashMap<>();

        for (Agent a : this._agents.values()) {
            var agentType = a.getLogic();
            if (!calc.containsKey(agentType)) {
                calc.put(agentType, new ArrayList<Money>());
            }
            calc.get(agentType).add(a.getProfit());
        }

        for (var x : calc.entrySet()) {
            var agentType = x.getKey();
            var profitList = x.getValue();
            history.profit.add(agentType, Money.average(profitList));
        }
    }

    /**
     * Returns the good that has the highest average price over the given range of
     * time
     * 
     * @param range   how many rounds to look back
     * @param exclude goods to exclude
     * @return
     */
    public GoodType getDearestGood(int range, List<Good.GoodType> exclude) {
        double best_price = 0;
        GoodType best_good = null;
        for (GoodType goodid : goodTypes) {
            if (exclude == null || exclude.indexOf(goodid) == -1) {
                double price = history.prices.average(goodid, range).as();
                if (price > best_price) {
                    best_price = price;
                    best_good = goodid;
                }
            }
        }
        return best_good;
    }

    public static class RoundStats {
        public int succesfulTrades=0;
        public Money moneyTraded=Money.NONE();
        public double qtyTraded=0.0;
        public Money avgClearingPrice=Money.NONE();
        public double totalAskQty=0.0;
        public double totalBidQty=0.0;
        

        RoundStats() {
        }

        

    }

    private void resolveOffers(Good.GoodType good) {

        var roundStats = new RoundStats();

        var resolvedTradeBook = new ArrayList<Offer.OfferLine>();

        var bids = _book.getBids(good);
        var asks = _book.getAsks(good);

        // highest buying price first
        bids.sort(Offer.offerSortDsc);
        asks.sort(Offer.offerSortAsc); // lowest selling price first

        for (Offer o : bids) {
            roundStats.totalAskQty += o.getTotalUnits();
        }

        for (Offer o : asks) {
            roundStats.totalAskQty += o.getTotalUnits();
        }

        // march through and try to clear orders
        while (bids.size() > 0 && asks.size() > 0) // while both books are non-empty
        {
            Offer buyerOffer = bids.get(0);
            Offer sellerOffer = asks.get(0);

            double quantity_traded = Math.min(sellerOffer.getTotalUnits(), buyerOffer.getTotalUnits());
            Money clearing_price = sellerOffer.getLowestUnitPrice().average(buyerOffer.getHighestUnitPrice());
            if (quantity_traded > 0.0) {

                transferGood(good, quantity_traded, sellerOffer.getOfferingAgent(), buyerOffer.getOfferingAgent());
                transferMoney(clearing_price, quantity_traded, sellerOffer.getOfferingAgent(),
                        buyerOffer.getOfferingAgent());

                // update agent price beliefs based on successful transaction
                Agent buyer_a = _agents.get(buyerOffer.getOfferingAgent());
                Agent seller_a = _agents.get(sellerOffer.getOfferingAgent());

                buyer_a.updatePriceModel(this, Offer.Type.BUY, good, true, clearing_price);
                seller_a.updatePriceModel(this, Offer.Type.SELL, good, true, clearing_price);

                buyerOffer.completeLines(quantity_traded, clearing_price);
                sellerOffer.completeLines(quantity_traded, clearing_price);

                roundStats.moneyTraded = roundStats.moneyTraded.add(clearing_price.multiply(quantity_traded));
                roundStats.qtyTraded += quantity_traded;
                roundStats.succesfulTrades++;

            }

            if (buyerOffer.isFullyResolved()) {
                bids.remove(0);
            }

            if (sellerOffer.isFullyResolved()) {
                asks.remove(0);
            }

        }

        // reject all remaining offers,
        // update price belief models based on unsuccessful transaction
        for (Offer b : bids) {
            Agent buyer_a = _agents.get(b.getOfferingAgent());
            // logger.info("Rejected Bids {} {}", buyer_a.getLogic().label(), b);
            buyer_a.updatePriceModel(this, Offer.Type.BUY, good, false, Money.NONE());
            b.setOverallResolution((Offer.STATE.REJECTED));
            // resolvedTradeBook.add(b);
        }
        for (Offer s : asks) {
            Agent seller_a = _agents.get(s.getOfferingAgent());
            // logger.info("Rejected Asks {} {}", seller_a.getLogic().label(), s);
            seller_a.updatePriceModel(this, Offer.Type.SELL, good, false, Money.NONE());
            s.setOverallResolution(Offer.STATE.REJECTED);
            // resolvedTradeBook.add(s);
        }

        history.updateHistory(good, roundStats);

        // logger.info(history.prices.get(good).toString());
        // logger.info("{} traded units {}, total money {}, avg price {}", good,
        // unitsTraded, moneyTraded, avgPrice);

    }

    protected void transferGood(GoodType good, double units, ID seller_id, ID buyer_id) {
        Agent seller = _agents.get(seller_id);
        Agent buyer = _agents.get(buyer_id);
        seller.changeInventory(good, -units);
        buyer.changeInventory(good, units);
    }

    protected void transferMoney(Money amount, double quantity_traded, ID seller_id, ID buyer_id) {
        Agent seller = _agents.get(seller_id);
        Agent buyer = _agents.get(buyer_id);

        seller.money = seller.money.add(amount);
        buyer.money = buyer.money.subtract(amount);

    }

    public String getName() {
        return this.name;
    }

    public History getHistory() {
        return this.history;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public abstract List<Good.GoodType> getGoodsTraded();



}