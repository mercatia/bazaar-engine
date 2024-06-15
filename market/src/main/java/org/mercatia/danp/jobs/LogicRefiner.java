package org.mercatia.danp.jobs;

import org.mercatia.bazaar.Market;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.AgentData;
/**
/**
 * ...
 * @author larsiusprime
 */
public class LogicRefiner extends GenericJob
{

	public LogicRefiner(String id, AgentData data){
		super(id,data);
	}
	 public void perform(Agent agent,Market market)
	{
		float food = agent.queryInventory("food");
		float tools = agent.queryInventory("tools");
		float ore = agent.queryInventory("ore");

		boolean has_food = food >= 1;
		boolean has_tools = tools >= 1;
		boolean has_ore = ore >= 1;

		// if (has_food && has_ore)
		// {
		// 	if (has_tools)
		// 	{
		// 		//convert all ore into metal, consume 1 food, break tools with 10% chance
		// 		_produce(agent,"metal",ore);
		// 		_consume(agent,"ore",ore);
		// 		_consume(agent,"food",1);
		// 		_consume(agent,"tools",1,0.1);
		// 	}
		// 	else{
		// 		//convert up to 2 ore into metal, consume 1 food
		// 		var max = agent.queryInventory("ore");
		// 		if(max > 2){ max = 2;}
		// 		_produce(agent,"metal", max);
		// 		_consume(agent,"ore", max);
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
