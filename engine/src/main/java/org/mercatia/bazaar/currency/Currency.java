package org.mercatia.bazaar.currency;

public class Currency {
    
    private String currencyName;
    

    protected Currency(String name){
        this.currencyName = name;
    }


    public String getCurrencyName() {
        return currencyName;
    }


    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }


    public static final Currency DEFAULT = new Currency("ASOMA");
    
}
