package org.mercatia.bazaar;

import org.mercatia.events.EventsOrigin;

public class MarketEventOrigin implements EventsOrigin{

    public void addMarketListener(MarketListener listener){
        this.addListener(listener);
    }
    
}
