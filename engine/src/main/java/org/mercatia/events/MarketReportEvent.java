package org.mercatia.events;

import java.util.EventObject;

import org.mercatia.bazaar.Market;
import org.mercatia.bazaar.impl.MarketReport;

public class MarketReportEvent extends EventObject{
    private MarketReport marketReport;
    
    public MarketReportEvent(Market origin){
        super(origin);
        this.marketReport = origin.getMarketReport();
    }

    public MarketReport getReport(){
        return this.marketReport;
    }
    

}
