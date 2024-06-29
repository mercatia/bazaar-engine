package org.mercatia.danp.jobs;

import java.util.Map;

import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.AgentData;
import org.mercatia.bazaar.market.Market;

/**

 */
public class LogicBlacksmith extends GenericJob {

	public LogicBlacksmith(AgentData data, Map<String, Good> goods) {
		super("Blacksmith", data, goods);
	}

	@Override
	public void simulate(Market market) {
		double food = queryInventory("food");
		double metal = queryInventory("metal");

		boolean has_food = food >= 1;
		boolean has_metal = metal >= 1;

		if (has_food && has_metal) {
			//convert all metal into tools
			produce("tools", metal, 1);
			consume("metal", metal, 1);
		} else {
			//fined $2 for being idle
			consume("money", 2, 1);
			if (!has_food && inventoryFull) {
				makeRoomFor(market, this, "food", 2);
			}
		}
	}

}
