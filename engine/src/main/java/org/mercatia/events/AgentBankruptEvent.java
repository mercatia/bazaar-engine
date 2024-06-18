package org.mercatia.events;

import java.util.EventObject;

import org.mercatia.bazaar.Market;
import org.mercatia.bazaar.agent.Agent;

public class AgentBankruptEvent extends EventObject {

    Agent agent;

    public AgentBankruptEvent(Market market, Agent agent) {
        super(market);
        this.agent = agent;
    }

}
