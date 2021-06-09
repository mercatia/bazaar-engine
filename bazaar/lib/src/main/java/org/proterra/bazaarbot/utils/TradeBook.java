package org.proterra.bazaarbot.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.proterra.bazaarbot.Offer;

/**
 * ...
 */
class TradeBook
{
	public Map<String, ArrayList<Offer>> bids;
	public Map<String, ArrayList<Offer>> asks;

	public TradeBook()
	{
		bids = new HashMap<String, ArrayList<Offer>>();
		asks = new HashMap<String, ArrayList<Offer>>();
	}

	public void register(String name)
	{
		asks.put(name, new ArrayList<Offer>());
		bids.put(name, new ArrayList<Offer>());
	}

	public boolean bid(Offer offer)
	{
		if (!bids.containsKey(offer.good))
			return false;

		bids.get(offer.good).add(offer);
		return true;
	}

	public boolean ask(Offer offer)
	{
		if (!bids.containsKey(offer.good))
			return false;

		asks.get(offer.good).add(offer);
		return true;
	}
}
