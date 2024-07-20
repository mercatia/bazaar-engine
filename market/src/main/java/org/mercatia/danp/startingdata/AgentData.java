package org.mercatia.danp.startingdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mercatia.bazaar.currency.Currency;
import org.mercatia.bazaar.currency.Money;
import org.mercatia.bazaar.goods.Good;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AgentData {

    @JsonProperty("money")
    float money;

    @JsonProperty("inventory")
    InventoryData inventory;

    @JsonProperty("logic")
    String logicName;



    @JsonIgnore
    public Money getMoney() {
        return Money.from(Currency.DEFAULT, money);
    }
}
