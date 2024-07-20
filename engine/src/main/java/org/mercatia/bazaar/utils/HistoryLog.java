package org.mercatia.bazaar.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.mercatia.Jsonable;

/**

 */
public class HistoryLog<K extends Typed, T extends Range.RangeType<T>> implements Jsonable {
	HistoryClass historyClass;
	Map<K, ArrayList<T>> log;

	protected ReentrantLock mutex = new ReentrantLock();

	public HistoryLog(HistoryClass historyClass) {
		this.historyClass = historyClass;
		log = new HashMap<K, ArrayList<T>>();
	}

	public List<T> get(K n) {
		try {
			mutex.lock();
			return log.get(n);
		} finally {
			mutex.unlock();
		}

	}

	/**
	 * Add a new entry to this log
	 * 
	 * @param name
	 * @param amount
	 */
	public void add(K name, T amount) {
		try {
			mutex.lock();
			if (log.containsKey(name)) {
				var list = log.get(name);
				list.add(amount);
			} else {
				var list = new ArrayList<T>();
				list.add(amount);
				log.put(name,list);
			}
		} finally {
			mutex.unlock();
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
	public T average(K name, int range) {
		try {
			mutex.lock();
			if (!log.containsKey(name)) {
				log.put(name, new ArrayList<T>());
			}
			List<T> list = log.get(name);
			T amt = null;

			var length = list.size();
			if (length == 0 || range < 1) {
				return null;
			}

			if (length < range) {
				range = length;
			}
			for (var x = length - 1; x >= length - range; x--) {
				amt = list.get(x).add(amt);
			}

			return amt.multiply(1.0 / range);

		} finally {
			mutex.unlock();
		}
	}

	private static record J(Map<String, List<Jsony>> log) implements Jsony {
	};

	@Override
	public Jsony jsonify() {
		try {
			mutex.lock();

			var m = new HashMap<String, List<Jsony>>();
			for (var logEntry : this.log.entrySet()) {
				K key = logEntry.getKey();
				List<T> list = logEntry.getValue();
				var l = list.stream().map(x -> x.jsonify()).collect(Collectors.toList());
				m.put(key.toString(), l);

				// var newList = list.steam().map(e->e.)
			}

			return new J(m);
		} finally {
			mutex.unlock();
		}

	}

	public String toString() {
		return this.log.toString();
	}
}
