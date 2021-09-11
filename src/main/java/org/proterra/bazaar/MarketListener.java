package org.proterra.bazaar;

import org.proterra.events.EventsListener;

public interface MarketListener extends EventsListener {
    public void agentBankurpt(MarketEvent evt);
}
