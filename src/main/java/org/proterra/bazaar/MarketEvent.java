package org.proterra.bazaar;

import org.proterra.events.EventsListener;
import org.proterra.events.EventsObject;

public class MarketEvent extends EventsObject {

    public MarketEvent(Market market){
        super(market);
    }

    @Override
    public void dispatch(EventsListener listener) {
       ((MarketListener)listener).agentBankurpt(this);   
    }
    
}
