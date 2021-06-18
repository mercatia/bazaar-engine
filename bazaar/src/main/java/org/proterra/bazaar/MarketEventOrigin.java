package org.proterra.bazaar;

import org.proterra.events.EventsOrigin;

public class MarketEventOrigin implements EventsOrigin{

    public void addMarketListener(MarketListener listener){
        this.addListener(listener);
    }
    
}
