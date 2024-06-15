package org.mercatia.bazaar.agent;

import org.mercatia.bazaar.Market;


/**
 * ...
 * @author
 */
public interface Logic
{

	/**
	 * Perform this logic on the given agent
	 * @param	agent
	 */

	public void perform(Agent agent, Market market);


	public default void produce(Agent agent, String commodity, float amount, float chance)
	{
		if (chance >= 1.0 || Math.random() < chance)
		{
			agent.changeInventory(commodity, amount);
		}
	}

	public default void consume (Agent agent, String commodity, float amount, float chance)
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
