package org.mercatia.bazaar;

import java.util.List;

import org.mercatia.bazaar.impl.MarketReport;
import org.mercatia.bazaar.utils.History;
import org.mercatia.events.EventsOrigin;



public interface Market extends EventsOrigin {

    String getName();

    List<Good> getGoods();
    
    void simulate(int rounds);
    
    float getAverageHistoricalPrice(String goodid, int lookback);   

    History getHistory();
    
    void ask(Offer offer);
    
    void bid(Offer offer);

	public String getCheapestGood(int range, List<String> exclude);

    MarketReport getMarketReport();
}