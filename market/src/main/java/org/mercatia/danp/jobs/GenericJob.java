package org.mercatia.danp.jobs;

import java.util.Arrays;
import java.util.Map;

import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.agent.AgentData;
import org.mercatia.bazaar.agent.BasicAgent;
import org.mercatia.bazaar.market.Market;
import org.mercatia.danp.DoranParberryEconomy.DPEJobs;

/**
 * ...
 * @author larsiusprime
 */
public abstract class GenericJob extends BasicAgent {

	public GenericJob(DPEJobs id, AgentData data, Map<String, Good> goods) {
		super((Logic)id, data, goods);
	}

	public void makeRoomFor(Market market, BasicAgent agent, String good, double amt) {

		String toDrop = market.getCheapestGood(10, Arrays.asList(good));
		if (toDrop != "") {
			if (inventory.query(toDrop) > amt) {
				consume(toDrop, amt);
			} else {
				consume(inventory.getMostHeld(), amt);
			}

			inventoryFull = false;
			System.err.println("Dropping " + toDrop + " " + amt);
		} else {
			System.err.println("!!! nothing to drop");
		}
	}

}
