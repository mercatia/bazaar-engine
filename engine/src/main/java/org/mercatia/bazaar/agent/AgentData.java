package org.mercatia.bazaar.agent;

import java.util.Map;

import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.currency.Currency;
import org.mercatia.bazaar.currency.Money;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AgentData {

    @JsonProperty("money")
    float money;

    @JsonProperty("inventory")
    InventoryData inventory;

    @JsonProperty("logic")
    String logicName;
    
    public static abstract class Factory {
        protected String id;
        protected AgentData data;
        protected Map<String,Good> goods;
        

        public Factory agentData(AgentData data){
            this.data = data;
            return this;
        }

        public String logic(){
            return data.logicName;
        }
    
        public abstract Agent build();

        public Factory goods(Map<String, Good> goods) {
            this.goods = goods;
            return this;
        }
    }

    @JsonIgnore
    public Money getMoney(){
        return Money.from(Currency.DEFAULT,money);
    }
}
