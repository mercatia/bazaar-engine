package org.proterra.bazaarbot.impl;

/**
 * ...
 * @author
 */
public class Market
{
	public String name;

	/**Logs information about all economic activity in this market**/
	public History history

	/**Signal fired when an agent's money reaches 0 or below**/
	public MarketEvent signalBankrupt;

	public  Market(String name)
	{
		this.name = name;

		history = new History();
		book = new TradeBook();
		goodTypes = new ArrayList<String>();
		agents = new ArrayList<BasicAgent>();
		mapGoods = new HashMap<String, Good>();
		mapAgents = new HashMap<String, AgentData>();

	}

	public  init(data:MarketData):Void
	{
		fromData(data);
	}

	public  numTypesOfGood():Int
	{
		return _goodTypes.length;
	}

	public  numAgents():Int
	{
		return _agents.length;
	}

	public  replaceAgent(oldAgent:BasicAgent, newAgent:BasicAgent):Void
	{
		newAgent.id = oldAgent.id;
		_agents[oldAgent.id] = newAgent;
		oldAgent.destroy();
		newAgent.init(this);
	}

	@:access(bazaarbot.agent.BasicAgent)
	public  simulate(rounds:Int):Void
	{
		for (round in 0...rounds)
		{
			for (agent in _agents)
			{
				agent.moneyLastRound = agent.money;
				agent.simulate(this);

				for (commodity in _goodTypes)
				{
					agent.generateOffers(this, commodity);
				}
			}

			for (commodity in _goodTypes)
			{
				resolveOffers(commodity);
			}
			for (agent in _agents)
			{
				if (agent.money <= 0)
				{
					signalBankrupt.dispatch(this, agent);
				}
			}
			_roundNum++;
		}
	}

	public  ask(offer:Offer):Void
	{
		_book.ask(offer);
	}

	public  bid(offer:Offer):Void
	{
		_book.bid(offer);
	}

	/**
	 * Returns the historical mean price of the given commodity over the last X rounds
	 * @param	commodity_ string id of commodity
	 * @param	range number of rounds to look back
	 * @return
	 */

	public  getAverageHistoricalPrice(good:String, range:Int):Float
	{
		return history.prices.average(good, range);
	}

	/**
	 * Get the good with the highest demand/supply ratio over time
	 * @param   minimum the minimum demand/supply ratio to consider an opportunity
	 * @param	range number of rounds to look back
	 * @return
	 */

	public  getHottestGood(minimum:Float = 1.5, range:Int = 10):String
	{
		 best_market:String = "";
		 best_ratio:Float = Math.NEGATIVE_INFINITY;
		for (good in _goodTypes)
		{
			 asks:Float = history.asks.average(good, range);
			 bids:Float = history.bids.average(good, range);

			 ratio:Float = 0;
			if (asks == 0 && bids > 0)
			{
				//If there are NONE on the market we artificially create a fake supply of 1/2 a unit to avoid the
				//crazy bias that "infinite" demand can cause...

				asks = 0.5;
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

	public  getCheapestGood(range:Int, exclude:Array<String> = null):String
	{
		 best_price:Float = Math.POSITIVE_INFINITY;
		 best_good:String = "";
		for (g in _goodTypes)
		{
			if (exclude == null || exclude.indexOf(g) == -1)
			{
				 price:Float = history.prices.average(g, range);
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

	public  getDearestGood(range:Int, exclude:Array<String> = null):String
	{
		 best_price:Float = 0;
		 best_good:String = "";
		for (g in _goodTypes)
		{
			if (exclude == null || exclude.indexOf(g) == -1)
			{
				 price = history.prices.average(g, range);
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
	public  getMostProfitableAgentClass(range:Int = 10):String
	{
		 best:Float = Math.NEGATIVE_INFINITY;
		 bestClass:String="";
		for (className in _mapAgents.keys())
		{
			 val:Float = history.profit.average(className, range);
			if (val > best)
			{
				bestClass = className;
				best = val;
			}
		}
		return bestClass;
	}

	public  getAgentClass(className:String):AgentData
	{
		return _mapAgents.get(className);
	}

	public  getAgentClassNames():Array<String>
	{
		 agentData = [];
		for (key in _mapAgents.keys())
		{
			agentData.push(key);
		}
		return agentData;
	}

	public  getGoods():Array<String>
	{
		return _goodTypes.copy();
	}

	public  getGoods_unsafe():Array<String>
	{
		return _goodTypes;
	}

	public  getGoodEntry(str:String):Good
	{
		if (_mapGoods.exists(str))
		{
			return _mapGoods.get(str).copy();
		}
		return null;
	}

	/********REPORT**********/
	public  get_marketReport(rounds:Int):MarketReport
	{
		 mr:MarketReport = new MarketReport();
		mr.strListGood = "Commodities\n\n";
		mr.strListGoodPrices = "Price\n\n";
		mr.strListGoodTrades = "Trades\n\n";
		mr.strListGoodAsks = "Supply\n\n";
		mr.strListGoodBids = "Demand\n\n";

		mr.strListAgent = "Classes\n\n";
		mr.strListAgentCount = "Count\n\n";
		mr.strListAgentProfit = "Profit\n\n";
		mr.strListAgentMoney = "Money\n\n";

		mr.arrStrListInventory = [];

		for (commodity in _goodTypes)
		{
			mr.strListGood += commodity + "\n";

			 price:Float = history.prices.average(commodity, rounds);
			mr.strListGoodPrices += Quick.numStr(price, 2) + "\n";

			 asks:Float = history.asks.average(commodity, rounds);
			mr.strListGoodAsks += Std.int(asks) + "\n";

			 bids:Float = history.bids.average(commodity, rounds);
			mr.strListGoodBids += Std.int(bids) + "\n";

			 trades:Float = history.trades.average(commodity, rounds);
			mr.strListGoodTrades += Std.int(trades) + "\n";

			mr.arrStrListInventory.push(commodity + "\n\n");
		}
		for (key in _mapAgents.keys())
		{
			 inventory:Array<Float> = [];
			for (str in _goodTypes)
			{
				inventory.push(0);
			}
			mr.strListAgent += key + "\n";
			 profit:Float = history.profit.average(key, rounds);
			mr.strListAgentProfit += Quick.numStr(profit, 2) + "\n";

			 test_profit:Float = 0;
			 list = _agents.filter((a:BasicAgent):Bool { return a.className == key; } );
			 count:Int = list.length;
			 money:Float = 0;

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

	private  _roundNum:Int = 0;

	private  _goodTypes:Array<String>;		//list of string ids for all the legal commodities
	private  _agents:Array<BasicAgent>;
	private  _book:TradeBook;
	private  _mapAgents:Map<String, AgentData>;
	private  _mapGoods:Map<String, Good>;

	private  fromData(data:MarketData)
	{
		//Create commodity index
		for (g in data.goods)
		{
			_goodTypes.push(g.id);
			_mapGoods.set(g.id, new Good(g.id, g.size));

			history.register(g.id);
			history.prices.add(g.id, 1.0);	//start the bidding at $1!
			history.asks.add(g.id, 1.0);	//start history charts with 1 fake buy/sell bid
			history.bids.add(g.id, 1.0);
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

	private  resolveOffers(good:String = ""):Void
	{
		 bids:Array<Offer> = _book.bids.get(good);
		 asks:Array<Offer> = _book.asks.get(good);

		bids = Quick.shuffle(bids);
		asks = Quick.shuffle(asks);

		bids.sort(Quick.sortDecreasingPrice);		//highest buying price first
		asks.sort(Quick.sortIncreasingPrice);		//lowest selling price first

		 successfulTrades:Int = 0;		//# of successful trades this round
		 moneyTraded:Float = 0;			//amount of money traded this round
		 unitsTraded:Float = 0;			//amount of goods traded this round
		 avgPrice:Float = 0;				//avg clearing price this round
		 numAsks:Float = 0;
		 numBids:Float = 0;

		 failsafe:Int = 0;

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
			 seller:Offer = asks[0];
			 seller_a:BasicAgent = _agents[seller.agent_id];
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

		 curr_class:String = "";
		 last_class:String = "";
		 list:Array<Float> = null;
		 avg_profit:Float = 0;

		for (i in 0..._agents.length)
		{
			 a:BasicAgent = _agents[i];		//get current agent
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

	private  transferGood(good:String, units:Float, seller_id:Int, buyer_id:Int):Void
	{
		 seller:BasicAgent = _agents[seller_id];
		  buyer:BasicAgent = _agents[buyer_id];
		seller.changeInventory(good, -units);
		 buyer.changeInventory(good,  units);
	}

	private  transferMoney(amount:Float, seller_id:Int, buyer_id:Int):Void
	{
		 seller:BasicAgent = _agents[seller_id];
		  buyer:BasicAgent = _agents[buyer_id];
		seller.money += amount;
		 buyer.money -= amount;
	}

}
