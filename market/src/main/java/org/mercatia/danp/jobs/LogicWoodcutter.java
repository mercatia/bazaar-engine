package org.mercatia.danp.jobs;

import java.util.Map;

import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.Market;
import org.mercatia.bazaar.agent.AgentData;

/**
/**
 * ...
 * @author larsiusprime
 */
public class LogicWoodcutter extends GenericJob {

	public LogicWoodcutter( AgentData data, Map<String, Good> goods) {
		super("Woodcutter", data, goods);
	}

	@Override
	public void simulate(Market market) {
		float food = queryInventory("food");
		float tools = queryInventory("tools");

		boolean has_food = food >= 1;
		boolean has_tools = tools >= 1;

		if (has_food) {
			if (has_tools) {
				//produce 2 wood, consume 1 food, break tools with 10% chance
				produce("wood", 2,1);
				consume("food", 1,1);
				consume("tools", 1, 0.1f);
			} else {
				//produce 1 wood, consume 1 food
				produce("wood", 1,1);
				consume("food", 1, 1);
			}
		} else {
			//fined $2 for being idle
			consume("money", 2);
			if (!has_food && inventoryFull) {
				makeRoomFor(market, this, "food", 2);
			}
		}
	}
}
