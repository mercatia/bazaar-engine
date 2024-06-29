package org.mercatia.bazaar.agent;

import org.mercatia.bazaar.currency.Currency;
import org.mercatia.bazaar.currency.Money;
import org.mercatia.bazaar.market.Market;


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
				agent.money = agent.money.subtract(Money.from(Currency.DEFAULT,amount));
				
			}
			else
			{
				agent.changeInventory(commodity, -amount);
			}
		}
	}

}
