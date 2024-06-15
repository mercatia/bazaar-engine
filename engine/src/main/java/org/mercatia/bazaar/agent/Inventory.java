package org.mercatia.bazaar.agent;

import java.util.HashMap;
import java.util.Map;

/**
 * ...
 * @author
 */
class Inventory
{
	public float maxSize = 0;
	private Map<String, Float> sizes;
	private Map<String, Float> stuff;
	private Map<String, Float> ideal;
			// key:commodity_id, val:amount
			// ideal counts for each thing
			// how much space each thing takes up
	public Inventory()
	{
		sizes = new HashMap<String, Float>();
		stuff = new HashMap<String, Float>();
		ideal = new HashMap<String, Float>();
		maxSize = 0;
	}


	/**
	 * Returns how much of this
	 * @param	commodity_ string id of commodity
	 * @return
	 */

	public float query(String goodid)
	{
		if (stuff.containsKey(goodid))
		{
			return stuff.get(goodid);
		}
		return 0;
	}

	public float ideal(String goodid)
	{
		if (ideal.containsKey(goodid))
		{
			return ideal.get(goodid);
		}
		return 0;
	}

	public float getEmptySpace()
	{
		return maxSize - getUsedSpace();
	}

	public float getUsedSpace()
	{
		float space_used = 0.0f;
		for (String key: stuff.keySet())
		{
			space_used += stuff.get(key) * sizes.get(key);
		}
		return space_used;
	}

	public float getCapacityFor(String goodid)
	{
		if (sizes.containsKey(goodid))
		{
			return sizes.get(goodid);
		}
		return -1;
	}

	/**
	 * Change the amount of the given commodity by delta
	 * @param	commodity_ string id of commodity
	 * @param	delta_ amount added
	 */

	public void change(String goodid, float delta)
	{
		float result;

		if (stuff.containsKey(goodid))
		{
			float amount = stuff.get(goodid);
			result = amount + delta;
		}
		else
		{
			result = delta;
		}

		if (result < 0)
		{
			result = 0;
		}

		stuff.put(goodid, result);
	}

	/**
	 * Returns # of units above the desired inventory level, or 0 if @ or below
	 * @param	commodity_ string id of commodity
	 * @return
	 */

	public float surplus(String goodid)
	{
		float amt = query(goodid);
		float idealAmt = ideal.get(goodid);
		if (amt > idealAmt)
		{
			return (amt - idealAmt);
		}
		return 0;
	}

	/**
	 * Returns # of units below the desired inventory level, or 0 if @ or above
	 * @param	commodity_
	 * @return
	 */

	public float shortage(String goodid)
	{
		if (!stuff.containsKey(goodid))
		{
			return 0;
		}
		float amt = query(goodid);
		float idealAmt = ideal.get(goodid);
		if (amt < idealAmt)
		{
			return (idealAmt - amt);
		}
		return 0;
	}

	//private static  _index:Map<String, Commodity>;


}
