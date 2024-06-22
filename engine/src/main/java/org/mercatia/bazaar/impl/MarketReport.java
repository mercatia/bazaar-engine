package org.mercatia.bazaar.impl;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MarketReport {

    @JsonProperty("name")
    public String name;

    @JsonProperty("agents")
    public List<String> agents = new ArrayList<String>();

    public String toString(){
        var sb = new StringBuilder(name);
        for (var agent: agents){
            sb.append(agent).append("\n");
        }
        
        return sb.toString();
    }

}
