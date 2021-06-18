package org.proterra.bazaar;

import java.util.List;

import org.proterra.bazaar.utils.History;
import org.proterra.events.EventsOrigin;

public interface Market extends EventsOrigin {

    String getName();

    List<Good> getGoods();
    
    void simulate(int rounds);
    
    float getAverageHistoricalPrice(String goodid, int lookback);   

    History getHistory();
    
    void ask(Offer offer);
    
    void bid(Offer offer);

}