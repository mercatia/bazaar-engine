package org.mercatia.events;

import java.util.EventListener;

public interface MarketEventListener extends EventListener{

    public static class Adapter implements MarketEventListener {

        @Override
        public void marketReport(MarketReportEvent event) {
        }

        @Override
        public void agentBankrupt(AgentBankruptEvent event) {
        }

    }

    public void marketReport(MarketReportEvent event);

    public void agentBankrupt(AgentBankruptEvent event);
}
