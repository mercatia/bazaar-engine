package org.mercatia.bazaar.agent;

import java.util.HashMap;
import java.util.List;
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

        protected static Map<String, AgentData.Factory> factoryCache = new HashMap<>();

        public static AgentData.Factory getCachedFactory(Agent.Logic logic) {
            return factoryCache.get(logic.label());
        }

        protected String id;
        protected AgentData data;
        protected Map<String, Good> goods;

        public Factory agentData(AgentData data) {
            this.data = data;
            return this;
        }

        public Agent.Logic logic() {
            return logicFrom(data.logicName);
        }

        protected abstract Agent.Logic logicFrom(String name);

        public Agent build() {
            return build(false);
        };

        public Agent build(boolean cache) {
            var a = buildAgent();

            if (cache) {
                if (!factoryCache.containsKey(this.data.logicName)) {
                    factoryCache.put(this.data.logicName, this);
                }
            }
            System.out.println(factoryCache);
            return a;
        };

        public abstract Agent buildAgent();

        public abstract List<Agent.Logic> listLogicTypes();

        public Factory goods(Map<String, Good> goods) {
            this.goods = goods;
            return this;
        }
    }

    @JsonIgnore
    public Money getMoney() {
        return Money.from(Currency.DEFAULT, money);
    }
}
