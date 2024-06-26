package org.mercatia.bazaar.agent;

import java.util.HashMap;
import java.util.Map;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.Good;

/**
 */
public class Inventory implements Jsonable {

	public double maxSize = 0.0;
	private Map<String, Double> sizes;
	private Map<String, Double> stuff;
	private Map<String, Double> ideal;

	// key:commodity_id, val:amount
	// ideal counts for each thing
	// how much space each thing takes up
	public Inventory() {
		sizes = new HashMap<>();
		stuff = new HashMap<>();
		ideal = new HashMap<>();
		maxSize = 0;
	}

	
	private record j(double maxSize, Map<String, Double> things) implements Jsony {
	};

	@Override
	public Jsony jsonify() {
		return new j(maxSize,stuff);
	}

	protected Inventory(double maxSize, Map<String, Double> ideal, Map<String, Double> start, Map<String, Good> goods) {
		this();
		this.maxSize = maxSize;

		for (var entry : ideal.entrySet()) {
			this.ideal.put(entry.getKey(), entry.getValue());
		}

		for (var entry : start.entrySet()) {
			this.stuff.put(entry.getKey(), entry.getValue());
		}

		sizes = new HashMap<>();
		for (var good : goods.entrySet()) {
			sizes.put(good.getKey(), good.getValue().size);
		}
	}

	/**
	 * Returns how much of this
	 * 
	 * @param commodity_ string id of commodity
	 * @return
	 */

	public double query(String goodid) {
		if (stuff.containsKey(goodid)) {
			return stuff.get(goodid);
		}
		return 0;
	}

	public double ideal(String goodid) {
		if (ideal.containsKey(goodid)) {
			return ideal.get(goodid);
		}
		return 0;
	}

	public double getEmptySpace() {
		return maxSize - getUsedSpace();
	}

	public double getUsedSpace() {
		double space_used = 0.0f;
		for (String key : stuff.keySet()) {
			space_used += stuff.get(key) * sizes.get(key);
		}
		return space_used;
	}

	public double getCapacityFor(String goodid) {
		if (sizes.containsKey(goodid)) {
			return sizes.get(goodid);
		}
		return -1;
	}

	/**
	 * Change the amount of the given commodity by delta
	 * 
	 * @param commodity_ string id of commodity
	 * @param delta_     amount added
	 */

	public void change(String goodid, double delta) {
		double result;

		if (stuff.containsKey(goodid)) {
			double amount = stuff.get(goodid);
			result = amount + delta;
		} else {
			result = delta;
		}

		if (result < 0) {
			result = 0;
		}

		stuff.put(goodid, result);
	}

	/**
	 * Returns # of units above the desired inventory level, or 0 if @ or below
	 * 
	 * @param commodity_ string id of commodity
	 * @return
	 */

	public double surplus(String goodid) {
		double amt = query(goodid);
		if (ideal.containsKey(goodid)) {
			double idealAmt = ideal.get(goodid);
			if (amt > idealAmt) {
				return Math.floor(amt - idealAmt);
			}
		}

		return 0;
	}

	/**
	 * Returns # of units below the desired inventory level, or 0 if @ or above
	 * 
	 * @param commodity_
	 * @return
	 */

	public double shortage(String goodid) {
		if (!stuff.containsKey(goodid)) {
			return 0;
		}
		double amt = query(goodid);
		double idealAmt = ideal.get(goodid);
		if (amt < idealAmt) {
			return Math.floor(idealAmt - amt);
		}
		return 0;
	}

	public static Inventory builderFromData(InventoryData data, Map<String, Good> goods) {
		var invertory = new Inventory(data.maxSize, data.ideal, data.start, goods);
		return invertory;
	}

	public String toString() {
		var sb = new StringBuilder();
		for (var i : this.stuff.entrySet()) {
			sb.append(i.getKey()).append("==").append(i.getValue()).append("/");
		}

		return sb.toString();
	}

}
