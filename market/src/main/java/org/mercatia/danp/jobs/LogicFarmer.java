package org.mercatia.danp.jobs;
import java.util.Map;

import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.Market;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.AgentData;
/**
 * ...
 * @author larsiusprime
 */
public class LogicFarmer extends GenericJob
{

	public LogicFarmer(AgentData data,Map<String,Good> goods)
	{
		super("Farmer",data,goods);
	}

	@Override
	public void simulate(Market market)
	{
		float wood = queryInventory("wood");
		float tools = queryInventory("tools");
		
		boolean has_wood = wood >= 1;
		boolean has_tools = tools >= 1;
		
		if (has_wood)
		{
		 	if (has_tools)
			{
				//produce 4 food, consume 1 wood, break tools with 10% chance
				produce("food",4,1);
				consume("wood",1,1);
				consume("tools",1,0.1f);
			}
			else{
				//produce 2 food, consume 1 wood
				produce("food",2,1);
				consume("wood",1,1);
			}	
		}
		else
		{
			//fined $2 for being idle
			consume("money",2);
		}
	}
}