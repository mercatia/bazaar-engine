package org.mercatia.bazaar.agent;

import java.util.HashMap;
import java.util.Map;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.Good;

/**
 */
public class Inventory implements Jsonable {

	public float maxSize = 0.0f;
	private Map<String, Float> sizes;
	private Map<String, Float> stuff;
	private Map<String, Float> ideal;

	// key:commodity_id, val:amount
	// ideal counts for each thing
	// how much space each thing takes up
	public Inventory() {
		sizes = new HashMap<String, Float>();
		stuff = new HashMap<String, Float>();
		ideal = new HashMap<String, Float>();
		maxSize = 0;
	}

	
	private record j(float maxSize, Map<String, Float> things) implements Jsony {
	};

	@Override
	public Jsony jsonify() {
		return new j(maxSize,stuff);
	}

	protected Inventory(float maxSize, Map<String, Float> ideal, Map<String, Float> start, Map<String, Good> goods) {
		this();
		this.maxSize = maxSize;

		for (var entry : ideal.entrySet()) {
			this.ideal.put(entry.getKey(), entry.getValue());
		}

		for (var entry : start.entrySet()) {
			this.stuff.put(entry.getKey(), entry.getValue());
		}

		sizes = new HashMap<String, Float>();
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

	public float query(String goodid) {
		if (stuff.containsKey(goodid)) {
			return stuff.get(goodid);
		}
		return 0;
	}

	public float ideal(String goodid) {
		if (ideal.containsKey(goodid)) {
			return ideal.get(goodid);
		}
		return 0;
	}

	public float getEmptySpace() {
		return maxSize - getUsedSpace();
	}

	public float getUsedSpace() {
		float space_used = 0.0f;
		for (String key : stuff.keySet()) {
			space_used += stuff.get(key) * sizes.get(key);
		}
		return space_used;
	}

	public float getCapacityFor(String goodid) {
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

	public void change(String goodid, float delta) {
		float result;

		if (stuff.containsKey(goodid)) {
			float amount = stuff.get(goodid);
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

	public float surplus(String goodid) {
		float amt = query(goodid);
		if (ideal.containsKey(goodid)) {
			float idealAmt = ideal.get(goodid);
			if (amt > idealAmt) {
				return (amt - idealAmt);
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

	public float shortage(String goodid) {
		if (!stuff.containsKey(goodid)) {
			return 0;
		}
		float amt = query(goodid);
		float idealAmt = ideal.get(goodid);
		if (amt < idealAmt) {
			return (idealAmt - amt);
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
