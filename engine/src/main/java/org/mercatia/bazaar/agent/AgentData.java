package org.mercatia.bazaar.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AgentData {

    @JsonProperty("id")
    String id;

    @JsonProperty("money")
    float money;

    @JsonProperty("inventory")
    InventoryData inventory;

    @JsonProperty("logic")
    String logicName;
    
    public static abstract class Factory {
        protected String id;
        protected AgentData data;

        public Factory id(String id){
            this.id = id;
            return this;
        }

        public Factory agentData(AgentData data){
            this.data = data;
            return this;
        }

        public abstract Agent build();
    }
}
