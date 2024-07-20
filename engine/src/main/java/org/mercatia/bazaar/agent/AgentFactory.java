package org.mercatia.bazaar.agent;

import java.util.List;

import org.mercatia.bazaar.AbstractFactory;

public abstract class AgentFactory extends AbstractFactory{

    protected Agent.Logic agentLogic;

    public abstract Agent.Logic logicFrom(String name);

    public abstract List<Agent> getStartingAgents();

    public AgentFactory withLogic(Agent.Logic logic){
        this.agentLogic = logic;
        return this;
    }

    public abstract Agent build();

    public abstract List<Agent.Logic> listLogicTypes();
	
}