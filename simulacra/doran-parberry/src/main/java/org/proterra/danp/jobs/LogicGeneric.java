package org.proterra.danp.jobs;
import org.proterra.bazaar.agent.*;
import org.proterra.bazaar.*;


/**
 * ...
 * @author larsiusprime
 */
public abstract class LogicGeneric extends Logic
{

	public LogicGeneric()
	{
		super();
	}

	private void makeRoomFor(Market market, BasicAgent agent, String good, float amt)
	{
		String toDrop = market.getCheapestGood(10,good);
		if (toDrop != "")
		{
			_consume(agent, toDrop, amt);
		}
	}
}
