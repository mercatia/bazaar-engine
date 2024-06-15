package org.mercatia.bazaar;

import org.mercatia.bazaar.agent.Agent;
import org.mercatia.events.EventsListener;
import org.mercatia.events.EventsObject;

public class AgentBankruptEvent extends EventsObject {

    Agent agent;

    public AgentBankruptEvent(Market market,Agent agent){
        super(market);
        this.agent = agent;
    }

    @Override
    public void dispatch(EventsListener listener) {
       ((MarketListener)listener).agentBankurpt(this);   
    }
    
}
