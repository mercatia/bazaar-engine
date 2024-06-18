package org.mercatia.bazaar;

import java.util.List;

import org.mercatia.bazaar.impl.MarketReport;
import org.mercatia.bazaar.utils.History;



public interface Market  {

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