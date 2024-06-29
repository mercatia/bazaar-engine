package org.mercatia.danp.jobs;
import java.util.Arrays;
import java.util.Map;

import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.agent.AgentData;
import org.mercatia.bazaar.agent.BasicAgent;
import org.mercatia.bazaar.market.Market;


/**
 * ...
 * @author larsiusprime
 */
public abstract class GenericJob extends BasicAgent
{

	public GenericJob(String id, AgentData data, Map<String,Good> goods)
	{
		super(id,data, goods);
	}

	public void makeRoomFor(Market market, BasicAgent agent, String good, double amt)
	{

		String toDrop = market.getCheapestGood(10, Arrays.asList(good));
		if (toDrop != "")
		{
			consume(toDrop, amt);
		}
	}

}
