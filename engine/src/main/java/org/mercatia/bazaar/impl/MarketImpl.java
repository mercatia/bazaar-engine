package org.mercatia.bazaar.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mercatia.bazaar.AgentBankruptEvent;
import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.Market;
import org.mercatia.bazaar.MarketData;
import org.mercatia.bazaar.Offer;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.AgentData;
import org.mercatia.bazaar.utils.History;
import org.mercatia.bazaar.utils.Quick;

/**
 * ...
 * 
 * @author
 */
public class MarketImpl implements Market {
	public String name;

	/** Logs information about all economic activity in this market **/
	public History history;

	/** Signal fired when an agent's money reaches 0 or below **/
	// public MarketEvent signalBankrupt;
	private List<String> _goodTypes; // list of string ids for all the legal commodities
	private Map<String, Agent> _agents;
	private TradeBook _book;
	private Map<String, AgentData> _mapAgents;
	private Map<String, Good> _mapGoods;

	public MarketImpl(String name, MarketData data) {
		this.name = name;

		history = new History();
		_book = new TradeBook();

		_goodTypes = new ArrayList<String>();
		_agents = new HashMap<String, Agent>();
		_mapGoods = new HashMap<String, Good>();
		_mapAgents = new HashMap<String, AgentData>();

		for (Good good : data.goods) {
			_goodTypes.add(good.id);
			_mapGoods.put(good.id, good);

			history.register(good.id);
			history.prices.add(good.id, 1.0f);
			history.asks.add(good.id, 1.0f);
			history.bids.add(good.id, 1.0f);
			history.trades.add(good.id, 1.0f);

			_book.register(good.id);
		}

		for (AgentData agent : data.agents) {
			// _agents.put(agent.id, agent);
		}

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
		newAgent.init(this);
	}

	/** Run the main simulation */
	public void simulate(int rounds) {
		// for each agent
		for (Agent agent : _agents.values()) {
			agent.moneyLastRound = agent.money;
			agent.simulate(this);

			for (String id : _goodTypes) {
				agent.generateOffers(this, id);
			}
		}

		for (String id : _goodTypes) {
			resolveOffers(id);
		}

		for (Agent agent : _agents.values()) {
			if (agent.money <= 0) {
				this.fireEvent(new AgentBankruptEvent(this, agent));
			}
		}

	}

	public void ask(Offer offer) {
		_book.ask(offer);
	}

	public void bid(Offer offer) {
		_book.bid(offer);
	}

	/**
	 * Returns the historical mean price of the given commodity over the last X
	 * rounds
	 * 
	 * @param commodity_ string id of commodity
	 * @param range      number of rounds to look back
	 * @return
	 */

	public float getAverageHistoricalPrice(String goodid, int range) {
		return history.prices.average(goodid, range);
	}

	/**
	 * Get the good with the highest demand/supply ratio over time
	 * 
	 * @param minimum the minimum demand/supply ratio to consider an opportunity
	 * @param range   number of rounds to look back
	 * @return
	 */

	public String getHottestGood(float minimum, int range) {
		String best_market = "";
		float best_ratio = Float.NEGATIVE_INFINITY;
		for (String goodid : _goodTypes) {
			float asks = history.asks.average(goodid, range);
			float bids = history.bids.average(goodid, range);

			float ratio = 0;
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
		float best_price = Float.POSITIVE_INFINITY;
		String best_good = "";
		for (String goodid : _goodTypes) {
			if (exclude == null || exclude.indexOf(goodid) == -1) {
				float price = history.prices.average(goodid, range);
				if (price < best_price) {
					best_price = price;
					best_good = goodid;
				}
			}
		}
		return best_good;
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
		float best_price = 0;
		String best_good = "";
		for (String goodid : _goodTypes) {
			if (exclude == null || exclude.indexOf(goodid) == -1) {
				float price = history.prices.average(goodid, range);
				if (price > best_price) {
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
	public String getMostProfitableAgentClass(int range) {
		float best = Float.NEGATIVE_INFINITY;
		String bestClass = "";
		for (String className : _mapAgents.keySet()) {
			float val = history.profit.average(className, range);
			if (val > best) {
				bestClass = className;
				best = val;
			}
		}
		return bestClass;
	}

	public AgentData getAgentClass(String className) {
		return _mapAgents.get(className);
	}

	public List<String> getAgentClassNames() {
		List<String> agentData = new ArrayList();
		for (String key : _mapAgents.keySet()) {
			agentData.add(key);
		}
		return agentData;
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

	private void resolveOffers(String good) {
		List<Offer> bids = _book.getBids(good);
		List<Offer> asks = _book.getAsks(good);

		// not sure why shuffle here..
		Collections.shuffle(bids);
		Collections.shuffle(asks);

		bids.sort((Offer a, Offer b) -> {
			if (a.getUnitPrice() < b.getUnitPrice()) {
				return -1;
			} else if (a.getUnitPrice() > b.getUnitPrice()) {
				return 1;
			} else {
				return 0;
			}

		}); 

		// highest buying price first
		asks.sort((Offer a, Offer b) -> {
			if (a.getUnitPrice() < b.getUnitPrice()) {
				return 1;
			} else if (a.getUnitPrice() > b.getUnitPrice()) {
				return -1;
			} else {
				return 0;
			}

		}); // lowest selling price first

		int successfulTrades = 0; // # of successful trades this round
		float moneyTraded = 0; // amount of money traded this round
		float unitsTraded = 0; // amount of goods traded this round
		float avgPrice = 0; // avg clearing price this round
		float numAsks = 0;
		float numBids = 0;

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
			Offer buyer = bids.get(0);
			Offer seller = asks.get(0);

			float quantity_traded = Math.min(seller.units, buyer.units);
			float clearing_price = Quick.avgf(seller.getUnitPrice(), buyer.getUnitPrice());

			if (quantity_traded > 0) {
				// transfer the goods for the agreed price
				seller.units -= quantity_traded;
				buyer.units -= quantity_traded;

				transferGood(good, quantity_traded, seller.agent_id, buyer.agent_id);
				transferMoney(quantity_traded * clearing_price, seller.agent_id, buyer.agent_id);

				// update agent price beliefs based on successful transaction
				Agent buyer_a = _agents.get(buyer.agent_id);
				Agent seller_a = _agents.get(seller.agent_id);
				buyer_a.updatePriceModel(this, "buy", good, true, clearing_price);
				seller_a.updatePriceModel(this, "sell", good, true, clearing_price);

				// log the stats
				moneyTraded += (quantity_traded * clearing_price);
				unitsTraded += quantity_traded;
				successfulTrades++;
			}

			if (seller.units == 0) // seller is out of offered good
			{
				asks.remove(0);
				failsafe = 0;
			}
			if (buyer.units == 0) // buyer is out of offered good
			{
				bids.remove(0); // remove bid
				failsafe = 0;
			}

			failsafe++;

			if (failsafe > 1000) // not good
			{
				throw new RuntimeException("BOINK!");
			}
		}

		// reject all remaining offers,
		// update price belief models based on unsuccessful transaction
		for (Offer b : bids) {
			Agent buyer_a = _agents.get(b.agent_id);
			;
			buyer_a.updatePriceModel(this, "buy", good, false);
		}
		for (Offer s : asks) {
			Agent seller_a = _agents.get(s.agent_id);
			seller_a.updatePriceModel(this, "sell", good, false);
		}

		// update history

		history.asks.add(good, numAsks);
		history.bids.add(good, numBids);
		history.trades.add(good, unitsTraded);

		if (unitsTraded > 0) {
			avgPrice = moneyTraded / unitsTraded;
			history.prices.add(good, avgPrice);
		} else {
			// special case: none were traded this round, use last round's average price
			history.prices.add(good, history.prices.average(good, 1));
			avgPrice = history.prices.average(good, 1);
		}

		String curr_class = "";
		String last_class = "";
		List<Float> list = null;
		float avg_profit = 0;

		// for (Agent a: _agents.values())
		// {
		// //get current agent
		// curr_class = a.className; //check its class
		// if (curr_class != last_class) //new class?
		// {
		// if (list != null) //do we have a list built up?
		// {
		// //log last class' profit
		// history.profit.add(last_class, Quick.listAvgf(list));
		// }
		// list = []; //make a new list
		// last_class = curr_class;
		// }
		// list.push(a.profit); //push profit onto list
		// }

		// add the last class too
		// history.profit.add(last_class, Quick.listAvgf(list));

		// sort by id so everything works again
		// _agents.sort(Quick.sortAgentId);

	}

	private void transferGood(String good, float units, String seller_id, String buyer_id) {
		Agent seller = _agents.get(seller_id);
		Agent buyer = _agents.get(buyer_id);
		seller.changeInventory(good, -units);
		buyer.changeInventory(good, units);
	}

	private void transferMoney(float amount, String seller_id, String buyer_id) {
		Agent seller = _agents.get(seller_id);
		Agent buyer = _agents.get(buyer_id);
		seller.money += amount;
		buyer.money -= amount;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public History getHistory() {
		return this.history;
	}

	@Override
	public MarketReport getMarketReport() {
		MarketReport mr = new MarketReport();
		mr.name = this.name;
		return mr;
	}

}
