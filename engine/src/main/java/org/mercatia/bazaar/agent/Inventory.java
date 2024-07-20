package org.mercatia.bazaar.agent;

import java.util.HashMap;
import java.util.Map;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.goods.Good;
import org.mercatia.bazaar.goods.Good.GoodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class Inventory implements Jsonable {

	static Logger logger = LoggerFactory.getLogger(Inventory.class);

	public double maxSize = 0.0;
	private Map<Good.GoodType, Good> goodMap;
	private Map<Good.GoodType, Double> stuff;
	private Map<Good.GoodType, Double> ideal;

	// key:commodity_id, val:amount
	// ideal counts for each thing
	// how much space each thing takes up
	public Inventory() {
		goodMap = new HashMap<>();
		stuff = new HashMap<>();
		ideal = new HashMap<>();
	}

	
	private record j(double maxSize, Map<String, Double> things) implements Jsony {
	};

	@Override
	public Jsony jsonify() {
		return null;//new j(maxSize,stuff);
	}

	public Inventory(double maxSize, Map<GoodType, Double> ideal, Map<GoodType, Double> start, Map<GoodType, Good> goods) {
		this();
		this.maxSize = maxSize;

		for (var entry : ideal.entrySet()) {
			this.ideal.put(entry.getKey(), entry.getValue());
		}

		for (var entry : start.entrySet()) {
			this.stuff.put(entry.getKey(), entry.getValue());
		}

		for (var entry : goods.entrySet()) {
			this.goodMap.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Returns how much of this
	 * 
	 * @param commodity_ string id of commodity
	 * @return
	 */

	public double query(GoodType goodid) {
		if (stuff.containsKey(goodid)) {
			return stuff.get(goodid);
		}
		return 0.0;
	}

	public double ideal(GoodType goodid) {
		if (ideal.containsKey(goodid)) {
			return ideal.get(goodid);
		}
		return 0.0;
	}

	public double getEmptySpace() {
		return maxSize - getUsedSpace();
	}

	public double getUsedSpace() {
		double space_used = 0.0f;
		for (GoodType key : stuff.keySet()) {
			space_used += stuff.get(key) * goodMap.get(key).getSize();
		}
		return space_used;
	}

	public double getCapacityFor(GoodType goodid) {
		if (goodMap.containsKey(goodid)) {
			return goodMap.get(goodid).getSize();
		}
		return -1;
	}

	/**
	 * Change the amount of the given commodity by delta
	 * 
	 * @param commodity_ string id of commodity
	 * @param delta_     amount added
	 */

	public void change(GoodType goodid, double delta) {
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

	public double surplus(GoodType goodid) {
		double amt = query(goodid);
		if (ideal.containsKey(goodid)) {
			double idealAmt = ideal.get(goodid);
			logger.info("{} Ideal {} Actual {}",goodid,idealAmt,amt);
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

	public double shortage(GoodType goodid) {
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

	public GoodType getMostHeld(){
		GoodType mostHeld =null;
		double held = 0.0;
		for (var e : stuff.entrySet()){
			if (e.getValue()>held){
				held = e.getValue();
				mostHeld = e.getKey();
			}
		}

		return mostHeld;
	}



	public String toString() {
		var sb = new StringBuilder();
		for (var i : this.stuff.entrySet()) {
			sb.append(String.format("%-6s",i.getKey())).append("==").append(String.format("%5.2f",i.getValue())).append("|");
		}

		return sb.toString();
	}

}
