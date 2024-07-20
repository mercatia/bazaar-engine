package org.mercatia.bazaar.utils;

/**
 *
 */

public enum HistoryClass implements Typed
{
	Price,
	Ask,
	Bid,
	Trade,
	Profit,;

	@Override
	public String label() {
		return this.name();
	}
}
