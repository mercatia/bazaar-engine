package org.mercatia.bazaar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.RandomStringGenerator;
import org.mercatia.Jsonable;
import org.mercatia.bazaar.agent.Agent.ID;
import org.mercatia.bazaar.currency.Money;

/**

 */
public class Offer implements Jsonable {

	static RandomStringGenerator generator = new RandomStringGenerator.Builder()
			.withinRange('a', 'z').get();

	public static class Resoultion implements Jsonable {
		double units;
		Money price;

		public Resoultion(double units, Money price) {
			this.units = units;
			this.price = price;
		}

		private record J(String units, Money unit_price) implements Jsony {};
		@Override
		public Jsony jsonify() {
			return new J(String.format("%.2f",this.units),price);
		};
	}

	public static enum Type {
		BUY, SELL
	};

	private String offerID;
	public String good; // the thing offered
	public double units; // how many units
	private Money unit_price; // price per unit
	public ID agent_id; // who offered this
	private Type type;

	private List<Resoultion> resoultions;

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

		this.offerID = generator.generate(20);
		this.resoultions = new ArrayList<>();
	}

	public boolean hasResolutions() {
		return this.resoultions.size() > 0;
	}

	public List<Resoultion> getResoultions() {
		return this.resoultions;
	}

	public void addResoultion(Resoultion r) {
		this.resoultions.add(r);
	}

	public String getOfferID() {
		return this.offerID;
	}

	public Offer setType(Type type) {
		this.type = type;
		return this;
	}

	public String toString() {
		return "(" + agent_id + "/" + offerID + ") " + type.name() + " " + good + " " + units + " of @ "
				+ unit_price;
	}

	private record J(String offerID, String good, String type, String units, Money unit_price, String offeringAgent,
			List<Jsony> resoultions) implements Jsony {
	};

	@Override
	public Jsony jsonify() {

		List<Jsony> res = this.resoultions.stream().map(e->e.jsonify()).collect(Collectors.toList());			

		return new J(offerID, good, type.name(), String.format("%.2f",units), unit_price, agent_id.toString(), res);
	}
}
