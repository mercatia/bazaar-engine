package org.mercatia.danp.agents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.Agent.Logic;
import org.mercatia.bazaar.agent.AgentFactory;

public class DPAgentFactory extends AgentFactory{

    @Override
    public Logic logicFrom(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logicFrom'");
    }

    @Override
    public List<Agent> getStartingAgents() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStartingAgents'");
    }

    @Override
    public Agent build() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildAndReset'");
    }

    @Override
    public List<Logic> listLogicTypes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listLogicTypes'");
    }
    // var agentData = data.agents;

    // for(
    // var agentStart:data.startConditions.getAgents().entrySet())
    // {
    //     int count = agentStart.getValue();
    //     String type = agentStart.getKey();

    //     for (int i = 0; i < count; i++) {
    //         var agent = af.agentData(agentData.get(type)).goods(this._mapGoods).build(true);
    //         agent.init(this, this.eventBus);
    //         this._agents.put(agent.getId(), agent);
    //     }
    // }

    // protected static Map<String, AgentFactory> factoryCache = new HashMap<>();

    // public static AgentFactory getCachedFactory(Agent.Logic logic) {
    //     return factoryCache.get(logic.label());
    // }
}
