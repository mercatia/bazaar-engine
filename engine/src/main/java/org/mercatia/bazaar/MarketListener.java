package org.mercatia.bazaar;

import org.mercatia.events.EventsListener;

public interface MarketListener extends EventsListener {
    public void agentBankurpt(AgentBankruptEvent evt);
}
