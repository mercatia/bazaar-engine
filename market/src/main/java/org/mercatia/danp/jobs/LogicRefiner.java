package org.mercatia.danp.jobs;

import java.util.Map;

import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.agent.AgentData;
import org.mercatia.bazaar.market.Market;

/**
/**
 * ...
 * @author larsiusprime
 */
public class LogicRefiner extends GenericJob {

	public LogicRefiner(AgentData data, Map<String, Good> goods) {
		super("Refiner", data, goods);
	}

	@Override
	public void simulate(Market market) {
		double food = queryInventory("food");
		double tools = queryInventory("tools");
		double ore = queryInventory("ore");

		boolean has_food = food >= 1;
		boolean has_tools = tools >= 1;
		boolean has_ore = ore >= 1;

		if (has_food && has_ore) {
			if (has_tools) {
				//convert all ore into metal, consume 1 food, break tools with 10% chance
				produce("metal", ore);
				consume("ore", ore);
				consume("food", 1);
				consume("tools", 1, 0.1);
			} else {
				//convert up to 2 ore into metal, consume 1 food
				var max = queryInventory("ore");
				if (max > 2) {
					max = 2;
				}
				produce("metal", max);
				consume("ore", max);
				consume( "food", 1);
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
