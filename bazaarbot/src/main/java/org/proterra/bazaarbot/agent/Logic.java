package org.proterra.bazaarbot.agent;

/**
 * ...
 * @author
 */
public class Logic
{
	private boolean init = false;

	public Logic(String data)
	{
		//no implemenation -- provide your own in a subclass
	}

	/**
	 * Perform this logic on the given agent
	 * @param	agent
	 */

	public void perform(BasicAgent agent, Market market)
	{
		//no implemenation -- provide your own in a subclass
	}

	private void _produce(agent:BasicAgent, commodity:String, amount:Float, chance:Float = 1.0)
	{
		if (chance >= 1.0 || Math.random() < chance)
		{
			agent.changeInventory(commodity, amount);
		}
	}

	private void _consume(BasicAgent agent, String commodity, float amount, float chance)
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
