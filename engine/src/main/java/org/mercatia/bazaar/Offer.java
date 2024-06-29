package org.mercatia.bazaar;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.agent.Agent.ID;
import org.mercatia.bazaar.currency.Money;

/**

 */
public class Offer implements Jsonable {

	public static enum Type {
		BUY, SELL
	};

	public String good; // the thing offered
	public double units; // how many units
	private Money unit_price; // price per unit
	public ID agent_id; // who offered this
	private Type type;

	public Money getUnitPrice() {
		return this.unit_price;
	}

	public Offer(ID agent_id, String commodity, double units, Money unit_price) {
		this.agent_id = agent_id;
		this.good = commodity;
		this.units = units;
		this.unit_price = unit_price;

		if (unit_price.zeroOrLess() || units < 1.0) {
			throw new RuntimeException(
					"Offering to " + type.name() + " " + units + " of " + commodity + " for " + unit_price);
		}

	}

	public Offer setType(Type type) {
		this.type = type;
		return this;
	}

	public String toString() {
		return "(" + agent_id + ") " + type.name() + " " + good + " " + units + " of @ " + unit_price;
	}

	private record J(String good, String type, double units, double unit_price, String offeringAgent) implements Jsony {
	};

	@Override
	public Jsony jsonify() {
		return new J(good, type.name(), units, unit_price.as(), agent_id.toString());
	}
}
