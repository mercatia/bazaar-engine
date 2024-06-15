package org.mercatia.bazaar;

import java.util.HashMap;
import java.util.Map;

import org.mercatia.bazaar.impl.MarketReport;

/**
 *
 */

public class EconomyReport  {
	
	Map<String,MarketReport> marketReports;

	public EconomyReport(Economy e){
		marketReports = new HashMap<String,MarketReport>();

		for (String name : e.getMarketNames()) {
			MarketReport mr = e.getMarket(name).getMarketReport();
			marketReports.put(name,mr);
		}

	}

}
