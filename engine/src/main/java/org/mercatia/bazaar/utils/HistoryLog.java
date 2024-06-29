package org.mercatia.bazaar.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**

 */
public class HistoryLog<T extends Range.RangeType<T>> {
	EconNoun type;
	Map<String, ArrayList<T>> log;

	public HistoryLog(EconNoun type) {
		this.type = type;
		log = new HashMap<String, ArrayList<T>>();
	}

	public List<T> get(String n){
		return log.get(n);
	}

	/**
	 * Add a new entry to this log
	 * 
	 * @param name
	 * @param amount
	 */
	public void add(String name, T amount) {
		if (log.containsKey(name)) {
			var list = log.get(name);
			list.add(amount);
		}
	}

	/**
	 * Register a new category list in this log
	 * 
	 * @param name
	 */
	public void register(String name) {
		if (!log.containsKey(name)) {
			log.put(name, new ArrayList<T>());
		}
	}

	/**
	 * Returns the average amount of the given category, looking backwards over a
	 * specified range
	 * 
	 * @param name  the category of thing
	 * @param range how far to look back
	 * @return
	 */
	public T average(String name, int range) {
		if (log.containsKey(name)) {
			List<T> list = log.get(name);
			T amt = null;
			
			var length = list.size();
			if (length==0 || range < 1){
				return null;
			}


			if (length < range) {
				range = length;
			}
			for (var x=length-1; x>=length-range; x--){
				amt = list.get(x).add(amt);
			}

			return amt.multiply(1.0/range);
		}
		return null;
	}
}
