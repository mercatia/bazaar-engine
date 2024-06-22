package org.mercatia.bazaar;

import java.util.List;
import java.util.Set;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.Agent.ID;
import org.mercatia.bazaar.currency.Money;
import org.mercatia.bazaar.impl.MarketReport;
import org.mercatia.bazaar.utils.History;



public interface Market extends Jsonable  {

    String getName();

    List<Good> getGoods();
    
    void simulate(int rounds);
    
    Money getAverageHistoricalPrice(String goodid, int lookback);   

    History getHistory();
    
    void ask(Offer offer);
    
    void bid(Offer offer);

	public String getCheapestGood(int range, List<String> exclude);

    MarketReport getMarketReport();

    public Set<ID> getAgents();

    public Agent getAgent(ID id);


}