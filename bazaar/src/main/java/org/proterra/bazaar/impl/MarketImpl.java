package org.proterra.bazaar.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Math;
import org.checkerframework.checker.units.qual.g;
import org.proterra.bazaar.Economy;
import org.proterra.bazaar.Good;
import org.proterra.bazaar.Market;
import org.proterra.bazaar.MarketData;
import org.proterra.bazaar.MarketEvent;
import org.proterra.bazaar.Offer;
import org.proterra.bazaar.agent.AgentData;
import org.proterra.bazaar.agent.BasicAgent;
import org.proterra.bazaar.utils.History;
import org.proterra.bazaar.utils.Quick;

/**
 * ...
 * @author
 */
public class MarketImpl implements Market
{
	public String name;

	/**Logs information about all economic activity in this market**/
	public History history;

	/**Signal fired when an agent's money reaches 0 or below**/
	public MarketEvent signalBankrupt;

	public MarketImpl(String name)
	{
		this.name = name;

		history = new History();
		TradeBook book = new TradeBook();
		List<String> goodTypes = new ArrayList<String>();
		List<BasicAgent> agents = new ArrayList<BasicAgent>();
		Map<String,Good> mapGoods = new HashMap<String, Good>();
		Map<String,AgentData> mapAgents = new HashMap<String, AgentData>();

	}

	public void init(MarketData data)
	{
		fromData(data);
	}

	public int numTypesOfGood()
	{
		return _goodTypes.size();
	}

	public int numAgents()
	{
		return _agents.size();
	}

	public void replaceAgent(BasicAgent oldAgent,BasicAgent newAgent)
	{
		newAgent.id = oldAgent.id;
		_agents.set(oldAgent.id, newAgent);
		newAgent.init(this);
	}

	public void simulate(int rounds)
	{
		for (int r=0; r< rounds; r++)
		{
			for (BasicAgent agent : _agents)
			{
				agent.moneyLastRound = agent.money;
				agent.simulate(this);

				for (String commodity : _goodTypes)
				{
					agent.generateOffers(this, commodity);
				}
			}

			for (String commodity : _goodTypes)
			{
				resolveOffers(commodity);
			}
			for (BasicAgent agent :_agents)
			{
				if (agent.money <= 0)
				{
					signalBankrupt.dispatch(this, agent);
				}
			}
			_roundNum++;
		}
	}

	public void ask(Offer offer)
	{
		_book.ask(offer);
	}

	public void bid(Offer offer)
	{
		_book.bid(offer);
	}

	/**
	 * Returns the historical mean price of the given commodity over the last X rounds
	 * @param	commodity_ string id of commodity
	 * @param	range number of rounds to look back
	 * @return
	 */

	public float getAverageHistoricalPrice(String good,int range)
	{
		return history.prices.average(good, range);
	}

	/**
	 * Get the good with the highest demand/supply ratio over time
	 * @param   minimum the minimum demand/supply ratio to consider an opportunity
	 * @param	range number of rounds to look back
	 * @return
	 */

	public String getHottestGood(float minimum,int range)
	{
		String best_market = "";
		float best_ratio = Float.NEGATIVE_INFINITY;
		for (String good : _goodTypes)
		{
			float asks = history.asks.average(good, range);
			float bids = history.bids.average(good, range);

			float ratio = 0;
			if (asks == 0 && bids > 0)
			{
				//If there are NONE on the market we artificially create a fake supply of 1/2 a unit to avoid the
				//crazy bias that "infinite" demand can cause...

				asks = 0.5f;
			}

			ratio = bids / asks;

			if (ratio > minimum && ratio > best_ratio)
			{
				best_ratio = ratio;
				best_market = good;
			}
		}
		return best_market;
	}

	/**
	 * Returns the good that has the lowest average price over the given range of time
	 * @param	range how many rounds to look back
	 * @param	exclude goods to exclude
	 * @return
	 */

	public String getCheapestGood(int range, List<String> exclude)
	{
		float best_price = Float.POSITIVE_INFINITY;
		String best_good = "";
		for (String g : _goodTypes)
		{
			if (exclude == null || exclude.indexOf(g) == -1)
			{
				float price = history.prices.average(g, range);
				if (price < best_price)
				{
					best_price = price;
					best_good = g;
				}
			}
		}
		return best_good;
	}

	/**
	 * Returns the good that has the highest average price over the given range of time
	 * @param	range how many rounds to look back
	 * @param	exclude goods to exclude
	 * @return
	 */

	public String getDearestGood(int range, List<String> exclude)
	{
		float best_price = 0;
		String best_good = "";
		for (String g : _goodTypes)
		{
			if (exclude == null || exclude.indexOf(g) == -1)
			{
				float price = history.prices.average(g, range);
				if (price > best_price)
				{
					best_price = price;
					best_good = g;
				}
			}
		}
		return best_good;
	}

	/**
	 *
	 * @param	range
	 * @return
	 */
	public String getMostProfitableAgentClass(int range)
	{
		float best = Float.NEGATIVE_INFINITY;
		String bestClass="";
		for (String className :  _mapAgents.keySet())
		{
			float val = history.profit.average(className, range);
			if (val > best)
			{
				bestClass = className;
				best = val;
			}
		}
		return bestClass;
	}

	public AgentData getAgentClass(String className)
	{
		return _mapAgents.get(className);
	}

	public List<String> getAgentClassNames()
	{
		List<String> agentData = new ArrayList();
		for (String key: _mapAgents.keySet())
		{
			agentData.add(key);
		}
		return agentData;
	}

	public List<String> getGoods()
	{
		return _goodTypes;
	}

	public Good getGoodEntry(String str)
	{
		if (_mapGoods.containsKey(str))
		{
			return _mapGoods.get(str);
		}
		return null;
	}

	/********REPORT**********/
	public MarketReport get_marketReport(int rounds)
	{
		MarketReport mr = new MarketReport();
		mr.strListGood = "Commodities\n\n";
		mr.strListGoodPrices = "Price\n\n";
		mr.strListGoodTrades = "Trades\n\n";
		mr.strListGoodAsks = "Supply\n\n";
		mr.strListGoodBids = "Demand\n\n";

		mr.strListAgent = "Classes\n\n";
		mr.strListAgentCount = "Count\n\n";
		mr.strListAgentProfit = "Profit\n\n";
		mr.strListAgentMoney = "Money\n\n";

		mr.arrStrListInventory = new ArrayList<String>();

		for (String commodity : _goodTypes)
		{
			mr.strListGood += commodity + "\n";

			float price = history.prices.average(commodity, rounds);
			mr.strListGoodPrices += Quick.numStr(price, 2) + "\n";

			float asks = history.asks.average(commodity, rounds);
			mr.strListGoodAsks += asks + "\n";

			float bids = history.bids.average(commodity, rounds);
			mr.strListGoodBids += bids + "\n";

			float trades = history.trades.average(commodity, rounds);
			mr.strListGoodTrades += trades + "\n";

			mr.arrStrListInventory.add(commodity + "\n\n");
		}

		for (String key : _mapAgents.keySet())
		{
			List<Float> inventory = new ArrayList<Float>();
			for (String str : _goodTypes)
			{
				inventory.push(0);
			}
			mr.strListAgent += key + "\n";
			float profit = history.profit.average(key, rounds);
			mr.strListAgentProfit += Quick.numStr(profit, 2) + "\n";

			float test_profit = 0;
			 list = _agents.filter((a:BasicAgent):Bool { return a.className == key; } );
			int count = list.length;
			float money = 0;

			for (a in list)
			{
				money += a.money;
				for (lic in 0..._goodTypes.length)
				{
					inventory[lic] += a.queryInventory(_goodTypes[lic]);
				}
			}

			money /= list.length;
			for (lic in 0..._goodTypes.length)
			{
				inventory[lic] /= list.length;
				mr.arrStrListInventory[lic] += Quick.numStr(inventory[lic],1) + "\n";
			}

			mr.strListAgentCount += Quick.numStr(count, 0) + "\n";
			mr.strListAgentMoney += Quick.numStr(money, 0) + "\n";
		}
		return mr;
	}

	/********PRIVATE*********/

	private int _roundNum = 0;

	private List<String> _goodTypes;		//list of string ids for all the legal commodities
	private List<BasicAgent> _agents;
	private TradeBook _book;
	private Map<String, AgentData> _mapAgents;
	private Map<String, Good> _mapGoods;

	private void fromData(MarketData data)
	{
		//Create commodity index
		for (Good g : data.goods)
		{
			_goodTypes.add(g.id);
			_mapGoods.put(g.id, new Good(g.id, g.size));

			history.register(g.id);
			history.prices.put(g.id, 1.0);	//start the bidding at $1!
			history.asks.put(g.id, 1.0);	//start history charts with 1 fake buy/sell bid
			history.bids.put(g.id, 1.0);
			history.trades.add(g.id, 1.0);

			_book.register(g.id);
		}

		_mapAgents = new Map<String, AgentData>();

		for (aData in data.agentTypes)
		{
			_mapAgents.set(aData.className, aData);
			history.profit.register(aData.className);
		}

		//Make the agent list
		_agents = [];

		 agentIndex = 0;
		for (agent in data.agents)
		{
			agent.id = agentIndex;
			agent.init(this);
			_agents.push(agent);
			agentIndex++;
		}

	}

	private void resolveOffers(String good)
	{
		List<Offer> bids = _book.bids.get(good);
		List<Offer> asks = _book.asks.get(good);

		bids = Quick.shuffle(bids);
		asks = Quick.shuffle(asks);

		bids.sort(Quick.sortDecreasingPrice);		//highest buying price first
		asks.sort(Quick.sortIncreasingPrice);		//lowest selling price first

		int successfulTrades = 0;		//# of successful trades this round
		float moneyTraded = 0;			//amount of money traded this round
		float unitsTraded = 0;			//amount of goods traded this round
		float avgPrice = 0;				//avg clearing price this round
		float numAsks = 0;
		float numBids = 0;

		int failsafe = 0;

		for (i in 0...bids.length)
		{
			numBids += bids[i].units;
		}

		for (i in 0...asks.length)
		{
			numAsks += asks[i].units;
		}

		//march through and try to clear orders
		while (bids.length > 0 && asks.length > 0)		//while both books are non-empty
		{
			 buyer:Offer = bids[0];
			 seller:Offer = asks[0];

			 quantity_traded = Math.min(seller.units, buyer.units);
			 clearing_price  = Quick.avgf(seller.unit_price, buyer.unit_price);

			if (quantity_traded > 0)
			{
				//transfer the goods for the agreed price
				seller.units -= quantity_traded;
				buyer.units -= quantity_traded;

				transferGood(good, quantity_traded, seller.agent_id, buyer.agent_id);
				transferMoney(quantity_traded * clearing_price, seller.agent_id, buyer.agent_id);

				//update agent price beliefs based on successful transaction
				 buyer_a:BasicAgent = _agents[buyer.agent_id];
				 seller_a:BasicAgent = _agents[seller.agent_id];
				buyer_a.updatePriceModel(this, "buy", good, true, clearing_price);
				seller_a.updatePriceModel(this, "sell", good, true, clearing_price);

				//log the stats
				moneyTraded += (quantity_traded * clearing_price);
				unitsTraded += quantity_traded;
				successfulTrades++;
			}

			if (seller.units == 0)		//seller is out of offered good
			{
				asks.splice(0, 1);		//remove ask
				failsafe = 0;
			}
			if (buyer.units == 0)		//buyer is out of offered good
			{
				bids.splice(0, 1);		//remove bid
				failsafe = 0;
			}

			failsafe++;

			if (failsafe > 1000)
			{
				trace("BOINK!");
			}
		}

		//reject all remaining offers,
		//update price belief models based on unsuccessful transaction
		while (bids.length > 0)
		{
			 buyer:Offer = bids[0];
			 buyer_a:BasicAgent = _agents[buyer.agent_id];
			buyer_a.updatePriceModel(this,"buy",good, false);
			bids.splice(0, 1);
		}
		while (asks.length > 0)
		{
			Offer seller:Offer = asks[0];
			BasicAgent seller_a:BasicAgent = _agents[seller.agent_id];
			seller_a.updatePriceModel(this,"sell",good, false);
			asks.splice(0, 1);
		}

		//update history

		history.asks.add(good, numAsks);
		history.bids.add(good, numBids);
		history.trades.add(good, unitsTraded);

		if (unitsTraded > 0)
		{
			avgPrice = moneyTraded / cast(unitsTraded, Float);
			history.prices.add(good, avgPrice);
		}
		else
		{
			//special case: none were traded this round, use last round's average price
			history.prices.add(good, history.prices.average(good, 1));
			avgPrice = history.prices.average(good,1);
		}

		_agents.sort(Quick.sortAgentAlpha);

		String curr_class = "";
		String last_class = "";
		List<Float> list = null;
		float avg_profit = 0;

		for (i in 0..._agents.length)
		{
			BasicAgent  a = _agents[i];		//get current agent
			curr_class = a.className;			//check its class
			if (curr_class != last_class)		//new class?
			{
				if (list != null)				//do we have a list built up?
				{
					//log last class' profit
					history.profit.add(last_class, Quick.listAvgf(list));
				}
				list = [];		//make a new list
				last_class = curr_class;
			}
			list.push(a.profit);			//push profit onto list
		}

		//add the last class too
		history.profit.add(last_class, Quick.listAvgf(list));

		//sort by id so everything works again
		_agents.sort(Quick.sortAgentId);

	}

	private  void transferGood(String good, float units,int seller_id,int buyer_id)
	{
		BasicAgent seller = _agents[seller_id];
		BasicAgent  buyer = _agents[buyer_id];
		seller.changeInventory(good, -units);
		 buyer.changeInventory(good,  units);
	}

	private void transferMoney(float amount,int seller_id,int buyer_id)
	{
		BasicAgent seller = _agents[seller_id];
		BasicAgent  buyer = _agents[buyer_id];
		seller.money += amount;
		 buyer.money -= amount;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onBankruptcy(Economy economy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public History getHistory() {
		// TODO Auto-generated method stub
		return null;
	}

}
