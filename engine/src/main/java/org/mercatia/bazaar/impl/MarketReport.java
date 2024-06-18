package org.mercatia.bazaar.impl;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MarketReport {

    @JsonProperty("name")
    public String name;

    @JsonProperty("agents")
    public Map<String,String> agents = new HashMap<String,String>();

    public String toString(){
        var sb = new StringBuilder(name);
        for (var agent: agents.values()){
            sb.append(agent).append("\n");
        }
        
        return sb.toString();
    }

}
