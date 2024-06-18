package org.mercatia.events;
import java.util.ArrayList;
import java.util.List;


public class EventsOrigin {
    final List<MarketEventListener> listeners = new ArrayList<MarketEventListener>();

    public void addListener(MarketEventListener listener){
        listeners.add(listener);
    }

    public void fireMarketReportEvent(MarketReportEvent event){
        for (var l : listeners) {
            l.marketReport(event);
        }
    }

}
