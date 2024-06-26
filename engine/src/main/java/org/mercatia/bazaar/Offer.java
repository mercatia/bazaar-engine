package org.mercatia.bazaar;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.agent.Agent.ID;
import org.mercatia.bazaar.currency.Money;

/**

 */
public class Offer implements Jsonable {
	public String good; // the thing offered
	public double units; // how many units
	private Money unit_price; // price per unit
	public ID agent_id; // who offered this

	public Money getUnitPrice() {
		return this.unit_price;
	}

	public Offer(ID agent_id, String commodity, double units, Money unit_price) {
		this.agent_id = agent_id;
		this.good = commodity;
		this.units = units;
		this.unit_price = unit_price;
	}

	public String toString() {
		return "(" + agent_id + "): " + good + "x " + units + " @ " + unit_price;
	}

	private record J(String good, double units, double unit_price, String offeringAgent) implements Jsony {
	};

	@Override
	public Jsony jsonify() {
		return new J(good, units, unit_price.as(), agent_id.toString());
	}
}
