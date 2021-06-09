package org.proterra.bazaarbot.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ...
 * @author larsiusprime
 */
public class HistoryLog
{
	EconNoun type;
	Map<String, ArrayList<Float>> log;

	public HistoryLog(EconNoun type)
	{
		this.type = type;
		log = new HashMap<String, ArrayList<Float>>();
	}

	/**
	 * Add a new entry to this log
	 * @param	name
	 * @param	amount
	 */
	public void add(String name, float amount)
	{
		if (log.containsKey(name))
		{
			var list = log.get(name);
			list.add(amount);
		}
	}

	/**
	 * Register a new category list in this log
	 * @param	name
	 */
	public void register(String name)
	{
		if (!log.containsKey(name))
		{
			log.put(name, new ArrayList<Float>());
		}
	}

	/**
	 * Returns the average amount of the given category, looking backwards over a specified range
	 * @param	name the category of thing
	 * @param	range how far to look back
	 * @return
	 */
	public float average(String name, int range)
	{
		if (log.containsKey(name))
		{
			List<Float> list = log.get(name);
			float amt = 0.0f;
			var length = list.size();
			if (length < range)
			{
				range = length;
			}
			for (float i: list)
			{
				amt += i;
			}
			return amt / range;
		}
		return 0;
	}
}
