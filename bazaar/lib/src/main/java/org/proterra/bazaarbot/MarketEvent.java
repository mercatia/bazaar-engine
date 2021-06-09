package org.proterra.bazaarbot;

import java.util.EventObject;

public class MarketEvent extends EventObject {

    public MarketEvent(Market market){
        super(market);
    }
    
}
