package org.mercatia.danp.agents.logic;

import java.util.List;

import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.Agent.Logic;
import org.mercatia.bazaar.agent.AgentFactory;
import org.mercatia.danp.DoranParberryEconomy.DPEJobs;

public class JobFactory extends AgentFactory {

	@Override
	public Agent build() {
		DPEJobs jobs = (DPEJobs) (agentLogic);
		switch (jobs) {
			case FARMER:
				
				return new LogicFarmer(data, goods);
			case MINER:
				return new LogicMiner(data, goods);
			case REFINER:
				return new LogicRefiner(data, goods);
			case WOODCUTTER:
				return new LogicWoodcutter(data, goods);
			case BLACKSMITH:
				return new LogicBlacksmith(data, goods);
			default:
				throw new RuntimeException(this.logic() + " unknown");
		}
	}

	@Override
	public Agent.Logic logicFrom(String name) {
		return DPEJobs.logicOfLabel(name);
	}

	@Override
	public List<Agent> getStartingAgents() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getStartingAgents'");
	}

	@Override
	public List<Logic> listLogicTypes() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'listLogicTypes'");
	}

}
