package org.mercatia.bazaar;

import org.mercatia.bazaar.agent.Agent.ID;
import org.mercatia.bazaar.currency.Money;

/**

 */
public class Offer {
	public String good; // the thing offered
	public float units; // how many units
	private Money unit_price; // price per unit
	public ID agent_id; // who offered this

	public Money getUnitPrice() {
		return this.unit_price;
	}

	public Offer(ID agent_id, String commodity, float units, Money unit_price) {
		this.agent_id = agent_id;
		this.good = commodity;
		this.units = units;
		this.unit_price = unit_price;
	}

	public String toString() {
		return "(" + agent_id + "): " + good + "x " + units + " @ " + unit_price;
	}
}
