package org.proterra.bazaarbot.utils;

import java.util.List;

import org.proterra.bazaarbot.Offer;
import org.proterra.bazaarbot.agent.BasicAgent;

/**
 * A quick & dirty utility class
 * 
 * @author larsiusprime
 */
public class Quick {

	public static float avgf(float a, float b) {
		return (a + b) / 2;
	}

	public static float listAvgf(List<Float> list) {
		float avg = 0;
		for (float j : list) {
			avg += j;
		}
		avg /= list.size();
		return avg;
	}

	public static float minArr(List<Float> list) {
		float min = Float.POSITIVE_INFINITY;
		for (float j : list) {
			min = Math.min(min, j);
		}
		return min;
	}

	public static float maxArr(List<Float> list) {
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
		return positionInRange(value,min,max,true);
	}
	public static int randomInteger(int min, int max) {
		return (int) ((Math.random() * (max - min)) + min);
	}

	public static float randomRange(float a,float b)
	{
		
		float min = a < b ? a : b;
		float max = a < b ? b : a;

		return (float) (Math.random() * (max - min)) + min;
	}

	public static int sortAgentAlpha(BasicAgent a, BasicAgent b) {
		// if (a.name < b.name) return -1;
		// if (a.name > b.name) return 1;
		return 0;
	}

	public static int sortAgentId(BasicAgent a, BasicAgent b) {
		if (a.id < b.id)
			return -1;
		if (a.id > b.id)
			return 1;
		return 0;
	}

	public static int sortDecreasingPrice(Offer a, Offer b) {
		// Decreasing means: highest first
		if (a.unit_price < b.unit_price)
			return 1;
		if (a.unit_price > b.unit_price)
			return -1;
		return 0;
	}

	public static int sortIncreasingPrice(Offer a, Offer b) {
		// Increasing means: lowest first
		if (a.unit_price > b.unit_price)
			return 1;
		if (a.unit_price < b.unit_price)
			return -1;
		return 0;
	}
}
