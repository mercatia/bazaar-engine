package org.mercatia.bazaar.utils;

import java.util.List;

import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.currency.Money;

/**
 * A quick & dirty utility class
 * 
 * @author larsiusprime
 */
public class Quick {

	@Deprecated
	public static Money avgf(Money a, Money b) {
		return Money.from(a.getCurrency(), avgf(a.as(), b.as()));
	}

	public static double avgf(double a, double b) {
		return (a + b) / 2;
	}

	public static double listAvgf(List<Double> list) {
		double avg = 0;
		for (double j : list) {
			avg += j;
		}
		avg /= list.size();
		return avg;
	}

	public static double _minArr(List<Double> list) {
		double min = Double.POSITIVE_INFINITY;
		for (double j : list) {
			min = Math.min(min, j);
		}
		return min;
	}

	public static <T extends Range.RangeType<T>> T minArr(List<T> list) {
		var eg = list.get(0);
		double min = Double.POSITIVE_INFINITY;
		for (var j : list) {
			min = Math.min(min, j.as());
		}
		return eg.toNew(min);
	}

	public static <T extends Range.RangeType<T>> T maxArr(List<T> list) {
		var eg = list.get(0);
		double min = Double.NEGATIVE_INFINITY;
		for (var j : list) {
			min = Math.max(min, j.as());
		}
		return eg.toNew(min);
	}

	public static float _maxArr(List<Float> list) {
		float max = Float.NEGATIVE_INFINITY;
		for (float j : list) {
			max = Math.max(max, j);
		}
		return max;
	}

	/**
	 * Turns a number into a string with the specified number of decimal points
	 * 
	 * @param num
	 * @param decimals
	 * @return
	 */
	public static String numStr(float num, int decimals) {
		return String.format("%." + decimals + "f", num);
	}

	public static float positionInRange(float value, float min, float max, boolean clamp) {
		value -= min;
		max -= min;
		min = 0;
		value = (value / (max - min));
		if (clamp) {
			if (value < 0) {
				value = 0;
			}
			if (value > 1) {
				value = 1;
			}
		}
		return value;
	}

	public static float positionInRange(float value, float min, float max) {
		return positionInRange(value, min, max, true);
	}

	public static int randomInteger(int min, int max) {
		return (int) ((Math.random() * (max - min)) + min);
	}

	public static Money randomRange(Money a, Money b) {
		return Money.from(a.getCurrency(), randomRange(a.as(), b.as()));
	}

	public static double randomRange(double a, double b) {

		double min = a < b ? a : b;
		double max = a < b ? b : a;

		return (double) (Math.random() * (max - min)) + min;
	}

	public static int sortAgentAlpha(Agent a, Agent b) {
		// if (a.name < b.name) return -1;
		// if (a.name > b.name) return 1;
		return 0;
	}

	// public static int sortAgentId(Agent a, Agent b) {
	// if (a.id < b.id)
	// return -1;
	// if (a.id > b.id)
	// return 1;
	// return 0;
	// }

	// public static int sortDecreasingPrice(Offer a, Offer b) {
	// // Decreasing means: highest first
	// if (a.unit_price < b.unit_price)
	// return 1;
	// if (a.unit_price > b.unit_price)
	// return -1;
	// return 0;
	// }

	// public static int sortIncreasingPrice(Offer a, Offer b) {
	// // Increasing means: lowest first
	// if (a.unit_price > b.unit_price)
	// return 1;
	// if (a.unit_price < b.unit_price)
	// return -1;
	// return 0;
	// }
}
