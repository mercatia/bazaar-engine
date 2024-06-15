package org.mercatia.danp.jobs;
import org.mercatia.bazaar.agent.*;

import java.util.Arrays;

import org.mercatia.bazaar.*;


/**
 * ...
 * @author larsiusprime
 */
public abstract class GenericJob extends BasicAgent
{

	public GenericJob(String id, AgentData data)
	{
		super(id,data);
	}

	public void makeRoomFor(Market market, BasicAgent agent, String good, float amt)
	{

		String toDrop = market.getCheapestGood(10, Arrays.asList(good));
		if (toDrop != "")
		{
			consume(toDrop, amt);
		}
	}

}
