package org.mercatia.danp.agents.logic;

import static org.mercatia.danp.DoranParberryEconomy.DPEJobs.MINER;

import java.util.Map;

import org.mercatia.bazaar.goods.Good;
import org.mercatia.bazaar.market.Market;
import org.mercatia.danp.startingdata.AgentData;;


/**

 */
public class LogicMiner extends GenericJob {

	public LogicMiner( AgentData data, Map<String, Good> goods) {
		super(MINER, data, goods);
	}

	@Override
	public void simulate(Market market) {
		double food = queryInventory("food");
		double tools = queryInventory("tools");

		boolean has_food = food >= 1;
		boolean has_tools = tools >= 1;

		if (has_food) {
			if (has_tools) {
				//produce 4 ore, consume 1 food, break tools with 10% chance
				produce("ore", 4);
				consume("food", 1);
				consume("tools", 1, 0.1);
			} else {
				//produce 2 ore, consume 1 food
				produce("ore", 2);
				consume("food", 1);
			}
		} else {
			//fined $2 for being idle
			consume("money", 2);
			if (inventoryFull) {
				makeRoomFor(market, this, "food", 2);
			}
		}
	}
}
