package org.mercatia.bazaar.market;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.Offer;
import org.mercatia.bazaar.Offer.Type;
import org.mercatia.bazaar.Transport;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.Agent.ID;
import org.mercatia.bazaar.agent.AgentData;
import org.mercatia.bazaar.currency.Currency;
import org.mercatia.bazaar.currency.Money;
import org.mercatia.bazaar.impl.TradeBook;
import org.mercatia.bazaar.utils.History;
import org.mercatia.bazaar.utils.ValueRT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public abstract class Market implements Jsonable {

    static Logger logger = LoggerFactory.getLogger(Market.class);

    protected String name;
    /** Logs information about all economic activity in this market **/
    public History history;

    /** Signal fired when an agent's money reaches 0 or below **/
    // public MarketEvent signalBankrupt;
    protected List<String> _goodTypes; // list of string ids for all the legal commodities
    protected Map<ID, Agent> _agents;
    protected TradeBook _book;

    
    protected ReentrantLock mutex = new ReentrantLock();
    protected Map<String,List<Offer>> lastResolvedTradeBook;
    protected Map<String, AgentData> _mapAgents;
    protected Map<String, Good> _mapGoods;
    protected Economy economy;
    protected EventBus eventBus;
    protected String addr;
    protected MarketData startingData;

    public Market(String name, MarketData data, Economy economy, Vertx vertx) {
        this.name = name;
        this.eventBus = vertx.eventBus();
        this.addr = String.format("economy/%s/market/%s", economy.getName(), this.name);
        this.economy = economy;
        this.startingData = data;

        history = new History();
        _book = new TradeBook();

        _goodTypes = new ArrayList<String>();
        _agents = new HashMap<ID, Agent>();
        _mapGoods = new HashMap<String, Good>();
        _mapAgents = new HashMap<String, AgentData>();

        for (Good good : data.goods.values()) {
            _goodTypes.add(good.id);
            _mapGoods.put(good.id, good);

            history.register(good.id);
            history.prices.add(good.id, Money.from(Currency.DEFAULT, 1.0));
            history.asks.add(good.id, ValueRT.of(1.0));
            history.bids.add(good.id, ValueRT.of(1.0));
            history.trades.add(good.id, ValueRT.of(1.0));

            _book.register(good.id);
        }

        // Create the default set of agents
        var af = economy.getAgentFactory();
        var agentData = data.agents;

        for (var agentStart : data.startConditions.getAgents().entrySet()) {
            int count = agentStart.getValue();
            String type = agentStart.getKey();
            for (int i = 0; i < count; i++) {
                var agent = af.agentData(agentData.get(type)).goods(this._mapGoods).build();
                agent.init(this, this.eventBus);
                this._agents.put(agent.getId(), agent);
            }
        }

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

    public void addAgent(String type){
        var agentData = startingData.agents;
        var af = economy.getAgentFactory();
        var agent = af.agentData(agentData.get(type)).goods(this._mapGoods).build();
        agent.init(this, this.eventBus);
        this._agents.put(agent.getId(), agent);
    }

    /** Run the main simulation */
    public void simulate(int rounds) {
        // for each agent
        for (Agent agent : _agents.values()) {
            logger.info(agent.toString());
            agent.moneyLastRound = agent.money;
            agent.simulate(this);

            for (String id : _goodTypes) {
                agent.generateOffers(this, id);
            }
        }

        try {
            mutex.lock();
            this.lastResolvedTradeBook = new HashMap<>();

            for (String id : _goodTypes) {
                resolveOffers(id);
            }
    
        } finally{
            mutex.unlock();
        }

        // determine if any new agents are required
        var toRemove = new ArrayList<ID>();
        for (Agent agent : _agents.values()) {
            logger.info(agent.toString());
            if (agent.money.zeroOrLess()) {
                toRemove.add(agent.id);
            }
        }

        writeLogFile();

        logger.info("Agents: {} before removing BANKRUPT {} ",_agents.size(),toRemove);
        for (var id : toRemove){
           
            this.economy.onBankruptcy(this, _agents.remove(id));
        }

        if (_agents.size()<=2){
            System.exit(-1);
        }
    }

    private void writeLogFile(){

        try (FileWriter writer = new FileWriter("log.json")){
            writer.write(JsonObject.mapFrom(this.history.jsonify()).toString());
        } catch (IOException e){
            logger.error("Can't write file",e);
        }
    }

    public Agent getAgent(ID id) {
        return this._agents.get(id);
    }

    public Set<ID> getAgents() {
        return this._agents.keySet();
    }

    public int numTypesOfGood() {
        return _goodTypes.size();
    }

    public int numAgents() {
        return _agents.size();
    }

    public void replaceAgent(Agent oldAgent, Agent newAgent) {
        newAgent.id = oldAgent.id;
        _agents.put(oldAgent.id, newAgent);
        newAgent.init(this, this.eventBus);
    }

    public void ask(Offer offer) {
        _book.ask(offer.setType(Type.SELL));
    }

    public void bid(Offer offer) {
        _book.bid(offer.setType(Type.BUY));
    }

    /**
     * Returns the historical mean price of the given commodity over the last X
     * rounds
     * 
     * @param commodity_ string id of commodity
     * @param range      number of rounds to look back
     * @return
     */

    public Money getAverageHistoricalPrice(String goodid, int range) {
        return history.prices.average(goodid, range);
    }

    /**
     * Get the good with the highest demand/supply ratio over time
     * 
     * @param minimum the minimum demand/supply ratio to consider an opportunity
     * @param range   number of rounds to look back
     * @return
     */

    public String getHottestGood(double minimum, int range) {
        String best_market = "";
        double best_ratio = Double.NEGATIVE_INFINITY;
        for (String goodid : _goodTypes) {
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

    public String getCheapestGood(int range, List<String> exclude) {
        double best_price = Double.POSITIVE_INFINITY;
        String best_good = "";
        for (String goodid : _goodTypes) {
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

    public List<Good> getGoods() {
        return new ArrayList<Good>(_mapGoods.values());
    }

    public Good getGoodEntry(String str) {
        if (_mapGoods.containsKey(str)) {
            return _mapGoods.get(str);
        }
        return null;
    }

    /**
     *
     * @param range
     * @return
     */
    public String getMostProfitableAgentClass(int range) {
        double best = Double.NEGATIVE_INFINITY;
        String bestClass = "";
        for (String className : _mapAgents.keySet()) {
            double val = history.profit.average(className, range).as();
            if (val > best) {
                bestClass = className;
                best = val;
            }
        }
        return bestClass;
    }

    /**
     * Returns the good that has the highest average price over the given range of
     * time
     * 
     * @param range   how many rounds to look back
     * @param exclude goods to exclude
     * @return
     */
    public String getDearestGood(int range, List<String> exclude) {
        double best_price = 0;
        String best_good = "";
        for (String goodid : _goodTypes) {
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

    private void resolveOffers(String good) {

        var resolvedTradeBook = new ArrayList<Offer>();

        List<Offer> bids = _book.getBids(good);
        List<Offer> asks = _book.getAsks(good);

        // highest buying price first
        bids.sort((Offer a, Offer b) -> {
            if (a.getUnitPrice().less(b.getUnitPrice())) {
                return 1;
            } else if (a.getUnitPrice().greater(b.getUnitPrice())) {
                return -1;
            } else {
                return 0;
            }

        });

        asks.sort((Offer a, Offer b) -> {
            if (a.getUnitPrice().less(b.getUnitPrice())) {
                return -1;
            } else if (a.getUnitPrice().greater(b.getUnitPrice())) {
                return 1;
            } else {
                return 0;
            }

        }); // lowest selling price first

        int successfulTrades = 0; // # of successful trades this round
        Money moneyTraded = Money.NONE(); // amount of money traded this round
        double unitsTraded = 0; // amount of goods traded this round
        Money avgPrice = Money.NONE(); // avg clearing price this round
        double numAsks = 0;
        double numBids = 0;

        int failsafe = 0;

        for (Offer o : bids) {
            numBids += o.units;
        }

        for (Offer o : asks) {
            numAsks += o.units;
        }

        // march through and try to clear orders
        while (bids.size() > 0 && asks.size() > 0) // while both books are non-empty
        {
            Offer buyerOffer = bids.get(0);
            Offer sellerOffer = asks.get(0);

            double quantity_traded = Math.min(sellerOffer.units, buyerOffer.units);
            Money clearing_price = sellerOffer.getUnitPrice().average(buyerOffer.getUnitPrice());
            // logger.info("Sell $ {} Buy ${} Clearing ${}",sellerOffer.getUnitPrice(),buyerOffer.getUnitPrice(),clearing_price);
            if (quantity_traded > 0) {
                // // transfer the goods for the agreed price
                // sellerOffer.units -= quantity_traded;
                // buyerOffer.units -= quantity_traded;

                transferGood(good, quantity_traded, sellerOffer.agent_id, buyerOffer.agent_id);
                transferMoney(clearing_price.multiply(quantity_traded), sellerOffer.agent_id, buyerOffer.agent_id);

                // update agent price beliefs based on successful transaction
                Agent buyer_a = _agents.get(buyerOffer.agent_id);
                Agent seller_a = _agents.get(sellerOffer.agent_id);
                buyer_a.updatePriceModel(this, Offer.Type.BUY, good, true, clearing_price);
                seller_a.updatePriceModel(this, Offer.Type.SELL, good, true, clearing_price);

                sellerOffer.addResoultion(new Offer.Resoultion(quantity_traded, clearing_price));
                buyerOffer.addResoultion(new Offer.Resoultion(quantity_traded, clearing_price));
                // log the stats
                moneyTraded = moneyTraded.add(clearing_price.multiply(quantity_traded));
                unitsTraded += quantity_traded;
                successfulTrades++;

                resolvedTradeBook.add(asks.remove(0));
                resolvedTradeBook.add(bids.remove(0)); // remove bid
            }    
            
        }

        // reject all remaining offers,
        // update price belief models based on unsuccessful transaction
        for (Offer b : bids) {
            
            Agent buyer_a = _agents.get(b.agent_id);
            logger.info("Rejected Bids {} {}",buyer_a.getName(),b);
            buyer_a.updatePriceModel(this,Offer.Type.BUY, good, false);
            resolvedTradeBook.add(b);
        }
        for (Offer s : asks) {
            
            Agent seller_a = _agents.get(s.agent_id);
            logger.info("Rejected Asks {} {}",seller_a.getName(),s);
            seller_a.updatePriceModel(this,Offer.Type.SELL, good, false);
            resolvedTradeBook.add(s);
        }

        asks.clear();
        bids.clear();

        // update history
        history.asks.add(good, ValueRT.of(numAsks));
        history.bids.add(good, ValueRT.of(numBids));
        history.trades.add(good, ValueRT.of(unitsTraded));

        

        if (unitsTraded > 0) {
            avgPrice = Money.from(Currency.DEFAULT, moneyTraded.as() / unitsTraded);
            history.prices.add(good, avgPrice);
        } else {
            // special case: none were traded this round, use last round's average price
            avgPrice = history.prices.average(good, 1);
            history.prices.add(good, avgPrice);          
        }

        // logger.info(history.prices.get(good).toString());
        logger.info("{} traded units {}, total money {}, avg price {}",good,unitsTraded, moneyTraded,avgPrice);

       
       this.lastResolvedTradeBook.put(good,resolvedTradeBook);
        
    }

    protected void transferGood(String good, double units, ID seller_id, ID buyer_id) {
        Agent seller = _agents.get(seller_id);
        Agent buyer = _agents.get(buyer_id);
        seller.changeInventory(good, -units);
        buyer.changeInventory(good, units);
    }

    protected void transferMoney(Money amount, ID seller_id, ID buyer_id) {
        Agent seller = _agents.get(seller_id);
        Agent buyer = _agents.get(buyer_id);
        logger.info(" {} from {} to {}",amount, buyer.name, seller.name);
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

    public AgentData getAgentClass(String className) {
        return _mapAgents.get(className);
    }

}