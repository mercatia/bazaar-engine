package org.mercatia.danp.agents.logic;

import org.mercatia.bazaar.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ...
 * 
 * @author larsiusprime
 */
public abstract class GenericJob extends Agent {

	static Logger logger = LoggerFactory.getLogger(GenericJob.class);

	// public GenericJob(DPEJobs id, AgentData data, Map<String, Good> goods) {
	// 	super((Logic) id, data, goods);
	// }

	// public void makeRoomFor(Market market, BasicAgent agent, String good, double amt) {

	// 	String toDrop = market.getCheapestGood(10, Arrays.asList(good));
	// 	if (toDrop != "") {
	// 		if (inventory.query(toDrop) > amt) {
	// 			consume(toDrop, amt);
	// 		} else {
	// 			consume(inventory.getMostHeld(), amt);
	// 		}

	// 		inventoryFull = false;
	// 		logger.info("Dropping " + toDrop + " " + amt);
	// 	} else {
	// 		logger.warn("!!! nothing to drop");
	// 	}
	// }

}
