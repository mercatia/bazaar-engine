package org.mercatia.bazaar;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.RandomStringGenerator;
import org.mercatia.Jsonable;
import org.mercatia.bazaar.agent.Agent.ID;
import org.mercatia.bazaar.currency.Currency;
import org.mercatia.bazaar.currency.Money;
import org.mercatia.bazaar.goods.Good.GoodType;
import org.mercatia.bazaar.utils.QtyPrice;

/**

 */
public class Offer implements Jsonable {

	public static enum STATE {
		ACCEPTED, REJECTED, PARTIAL, PENDING, VOID
	}

	public static enum Type {
		BUY, SELL
	};

	static RandomStringGenerator generator = new RandomStringGenerator.Builder()
			.withinRange('a', 'z').get();

	/**
	 * Return -
	 * -1 if the first is less than the second,
	 * 0 if equal
	 * 1 if first is > second
	 */
	public static Comparator<Offer> offerSortAsc = (Offer a, Offer b) -> {
		if (a.getMeanUnitPrice().less(b.getMeanUnitPrice())) {
			return -1;
		} else if (a.getMeanUnitPrice().greater(b.getMeanUnitPrice())) {
			return 1;
		} else {
			return 0;
		}

	};

	public static Comparator<Offer> offerSortDsc = offerSortAsc.reversed();

	private int lineIndex = 0;

	public static class OfferLine extends QtyPrice {

		STATE resolution;
		Offer parent;
		int index;

		public OfferLine(double units, Money price, Offer parent) {
			super(units, price);
			this.parent = parent;
			this.resolution = STATE.PENDING;
			this.index = parent.lineIndex++;
			;
		}

		public Integer index() {
			return index;
		}

		public OfferLine setResolution(STATE resolution) {
			this.resolution = resolution;
			return this;
		}

		public Offer getParent() {
			return this.parent;
		}

		public STATE getResolution() {
			return this.resolution;
		}

		public String toString() {
			return "{" + units + " @ $" + unit_price + " " + this.resolution.name() + "}";
		}

		public double getUnits() {
			return this.units;
		}
	}

	protected String offerID;
	protected GoodType good; // the thing offered
	protected ID agent_id; // who offered this
	protected Type type;

	protected Map<Integer, OfferLine> offerLines;

	public Offer(ID agent_id, GoodType commodity, double units, Money unit_price) {
		this.agent_id = agent_id;
		this.good = commodity;

		if (unit_price.zeroOrLess() || units < 1.0) {
			throw new RuntimeException(
					"Offering to " + type.name() + " " + units + " of " + commodity + " for " + unit_price);
		}

		this.offerID = generator.generate(20);

		// create initial order line
		this.offerLines = new HashMap<Integer, OfferLine>();
		var newOffer = new Offer.OfferLine(units, unit_price, this);
		this.offerLines.put(newOffer.index(), newOffer);

	}

	public List<Offer.OfferLine> getOfferLines() {
		return this.offerLines.values().stream().collect(Collectors.toList());
	}

	public String getOfferID() {
		return this.offerID;
	}

	public ID getOfferingAgent() {
		return this.agent_id;
	}

	public boolean isBuy() {
		return this.type == Type.BUY;
	}

	public boolean isSell() {
		return this.type == Type.SELL;
	}

	public Offer setType(Type type) {
		this.type = type;
		return this;
	}

	public GoodType getGoodType() {
		return this.good;
	}

	/** Aveage */
	public Money getMeanUnitPrice() {
		var prices = this.offerLines.values().stream().map(e -> e.getUnitPrice()).collect(Collectors.toList());
		return Money.average(prices);
	}

	public double getTotalUnits() {
		double qty = this.offerLines.values().stream().map(e -> e.getUnits()).reduce(0.0, Double::sum);
		return qty;
	}

	public Money getLowestUnitPrice() {
		double lowest = this.offerLines.values().stream().map(e -> e.getUnitPrice().as()).reduce(Double.MAX_VALUE,
				(a, b) -> a < b ? a : b);
		return Money.from(Currency.DEFAULT, lowest);
	}

	public Money getHighestUnitPrice() {
		double highest = this.offerLines.values().stream().map(e -> e.getUnitPrice().as()).reduce(Double.MIN_VALUE,
				(a, b) -> a > b ? a : b);
		return Money.from(Currency.DEFAULT, highest);	
	}

	public String toString() {
		var sb = new StringBuilder();
		sb.append("(" + agent_id + "/" + offerID + ") " + String.format("%10s", type.name()) + " " + good);
		for (var ol : offerLines.values()) {
			sb.append(ol.toString()).append(" ");
		}
		return sb.toString();
	}

	private record J(String offerID, String good, String type, String units, Money unit_price, String offeringAgent,
			List<Jsony> resoultions) implements Jsony {
	};

	@Override
	public Jsony jsonify() {

		// List<Jsony> res = this.resoultions.stream().map(e ->
		// e.jsonify()).collect(Collectors.toList());

		// return new J(offerID, good, type.name(), agent_id.toString());
		return null;
	}

	public boolean isFullyResolved() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isFullyResolved'");
	}

	public void completeLines(double quantity_traded, Money clearing_price) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'completeLines'");
	}

	public void setOverallResolution(STATE state) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setOverallResolution'");
	}

}
