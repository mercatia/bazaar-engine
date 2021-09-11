package org.proterra.bazaar.agent;

import org.proterra.bazaar.Market;


/**
 * ...
 * @author
 */
public abstract class Logic
{

	public Logic(String data)
	{
		
	}

	/**
	 * Perform this logic on the given agent
	 * @param	agent
	 */

	public abstract void perform(BasicAgent agent, Market market);
	{
		//no implemenation -- provide your own in a subclass
	}

	protected void produce(BasicAgent agent, String commodity, float amount, float chance)
	{
		if (chance >= 1.0 || Math.random() < chance)
		{
			agent.changeInventory(commodity, amount);
		}
	}

	protected void roduce(BasicAgent agent, String commodity, float amount, float chance)
	{
		if (chance >= 1.0 || Math.random() < chance)
		{
			if (commodity == "money")
			{
				agent.money -= amount;
			}
			else
			{
				agent.changeInventory(commodity, -amount);
			}
		}
	}

}
