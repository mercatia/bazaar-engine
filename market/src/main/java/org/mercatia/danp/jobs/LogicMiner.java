package org.mercatia.danp.jobs;

import org.mercatia.bazaar.Market;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.AgentData;
/**
/**
 * ...
 * @author larsiusprime
 */
public class LogicMiner extends GenericJob
{

	public  LogicMiner(String id, AgentData data)
	{
		super(id,data);
	}

	 public void perform(Agent agent,Market market)
	{
		float food = agent.queryInventory("food");
		float tools = agent.queryInventory("tools");

		boolean has_food = food >= 1;
		boolean has_tools = tools >= 1;

		// if (has_food)
		// {
		// 	if (has_tools)
		// 	{
		// 		//produce 4 ore, consume 1 food, break tools with 10% chance
		// 		_produce(agent,"ore",4);
		// 		_consume(agent,"food",1);
		// 		_consume(agent,"tools",1,0.1);
		// 	}
		// 	else
		// 	{
		// 		//produce 2 ore, consume 1 food
		// 		_produce(agent,"ore",2);
		// 		_consume(agent,"food",1);
		// 	}
		// }
		// else
		// {
		// 	//fined $2 for being idle
		// 	_consume(agent,"money",2);
		// 	if (!has_food && agent.inventoryFull)
		// 	{
		// 		makeRoomFor(market, agent,"food",2);
		// 	}
		// }
	}
}
