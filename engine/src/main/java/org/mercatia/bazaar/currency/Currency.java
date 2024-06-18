package org.mercatia.bazaar.currency;

public class Currency {
    
    private String currencyName;

    private long unit;
    private long fractional;

    protected Currency(String name, long unit, long fractional){
        this.currencyName = name;
        this.unit = unit;
        this.fractional = fractional;
    }


    
}
