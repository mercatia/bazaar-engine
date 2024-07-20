package org.mercatia.danp.agents.logic;

import java.util.Map;

import org.mercatia.bazaar.goods.Good;
import org.mercatia.bazaar.market.Market;
import org.mercatia.danp.startingdata.AgentData;

import static org.mercatia.danp.DoranParberryEconomy.DPEJobs.BLACKSMITH;

/**

 */
public class LogicBlacksmith extends GenericJob {

	public LogicBlacksmith(AgentData data, Map<String, Good> goods) {
		super(BLACKSMITH, data, goods);
	}

	@Override
	public void simulate(Market market) {
		double food = queryInventory("food");
		double metal = queryInventory("metal");

		boolean has_food = food >= 1.0;
		boolean has_metal = metal >= 1.0;

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
