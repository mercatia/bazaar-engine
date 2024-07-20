package org.mercatia.danp.agents.logic;

import java.util.Map;

import org.mercatia.bazaar.goods.Good;
import org.mercatia.bazaar.market.Market;
import org.mercatia.danp.startingdata.AgentData;

import static org.mercatia.danp.DoranParberryEconomy.DPEJobs.WOODCUTTER;

/**

 */
public class LogicWoodcutter extends GenericJob {

	public LogicWoodcutter(AgentData data, Map<String, Good> goods) {
		super(WOODCUTTER, data, goods);
	}

	@Override
	public void simulate(Market market) {
		double food = queryInventory("food");
		double tools = queryInventory("tools");

		boolean has_food = food >= 1;
		boolean has_tools = tools >= 1;

		if (has_food) {
			if (has_tools) {
				//produce 2 wood, consume 1 food, break tools with 10% chance
				produce("wood", 2, 1);
				consume("food", 1, 1);
				consume("tools", 1, 0.1);
			} else {
				//produce 1 wood, consume 1 food
				produce("wood", 1, 1);
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
