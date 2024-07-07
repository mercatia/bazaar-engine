package org.mercatia.bazaar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.ml.neuralnet.twod.util.QuantizationError;
import org.apache.commons.text.RandomStringGenerator;
import org.mercatia.Jsonable;
import org.mercatia.bazaar.agent.Agent.ID;
import org.mercatia.bazaar.currency.Money;

/**

 */
public class Offer implements Jsonable {

	public static enum STATE {
		ACCEPTED, REJECTED, PARTIAL, OFFERING
	}

	protected static class QtyPrice implements Jsonable {
		double units;
		Money unit_price;

		public QtyPrice(double units, Money price) {
			this.units = units;
			this.unit_price = price;
		}

		public Money getUnitPrice() {
			return this.unit_price;
		}

		public double getUnits() {
			return units;
		}

		private record J(String units, Money unit_price) implements Jsony {
		};

		@Override
		public Jsony jsonify() {
			return new J(String.format("%.2f", this.units), unit_price);
		};
	}

	static RandomStringGenerator generator = new RandomStringGenerator.Builder()
			.withinRange('a', 'z').get();

	public static class OfferLine extends QtyPrice {

		Resoultion resoultion;
		Offer parent;

		public OfferLine(Money price, Offer parent) {
			this(1.0, price, parent);
		}

		public OfferLine(double units, Money price, Offer parent) {
			super(units, price);
			this.parent = parent;
			this.resoultion = new Resoultion(STATE.OFFERING);
		}

		public OfferLine setResoultion(Resoultion resoultion) {
			this.resoultion = resoultion;
			return this;
		}

		public Offer getParent() {
			return this.parent;
		}

		public Resoultion getResoultion() {
			return this.resoultion;
		}

		public String toString() {
			return "(" + getParent().agent_id + "/" + getParent().offerID + ") " + getParent().type.name() + " "
					+ getParent().good + " " + units + " of @ "
					+ unit_price + " " + this.resoultion;
		}

	}

	public static class Resoultion extends QtyPrice {
		STATE resoultion;

		public Resoultion(STATE resoultion) {
			this(0.0, Money.NONE(), resoultion);
		}

		public Resoultion(double units, Money price, STATE resoultion) {
			super(units, price);
			this.resoultion = resoultion;
		}

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
	private List<OfferLine> offerLines;

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
		this.offerLines = new ArrayList<>();

		for (var x = 0; x < units; x++) {
			offerLines.add(new Offer.OfferLine(unit_price, this));
		}

	}

	public List<Offer.OfferLine> getOfferLines() {
		return this.offerLines;
	}

	public boolean hasResolutions() {
		// this.offerLines.stream().reduce(false , )
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

		List<Jsony> res = this.resoultions.stream().map(e -> e.jsonify()).collect(Collectors.toList());

		return new J(offerID, good, type.name(), String.format("%.2f", units), unit_price, agent_id.toString(), res);
	}
}
