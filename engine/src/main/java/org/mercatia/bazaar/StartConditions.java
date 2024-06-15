package org.mercatia.bazaar;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StartConditions {

    @JsonProperty("agents")
    Map<String,Integer> agents;

    public StartConditions(){
    }

    public String toString(){
        return agents.toString();
    }
}
