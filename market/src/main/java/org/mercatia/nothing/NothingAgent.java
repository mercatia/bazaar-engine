package org.mercatia.nothing;

import java.util.List;

import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.agent.AgentFactory;
import org.mercatia.bazaar.market.Market;

public class NothingAgent extends Agent {

    public static enum Jobs implements Agent.Logic {
        JOB_ONE,
        JOB_TWO;

        @Override
        public String label() {
            return this.name();
        }
    }

    public static class Factory extends AgentFactory {

        @Override
        public Logic logicFrom(String name) {
            switch (name) {
                case "JOB_ONE":
                    return Jobs.JOB_ONE;
                case "JOB_TWO":
                    return Jobs.JOB_TWO;

                default:
                    return null;
            }
        }

        @Override
        public List<Agent> getStartingAgents() {
            return List.of(
                    new NothingAgent(Jobs.JOB_ONE),
                    new NothingAgent(Jobs.JOB_TWO));
        }

        @Override
        public Agent build() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'build'");
        }

        @Override
        public List<Logic> listLogicTypes() {
            return List.of(Jobs.JOB_ONE, Jobs.JOB_TWO);
        }

    }

    private Jobs job;

    public NothingAgent(Jobs job) {
        this.job = job;
    }

    @Override
    public void simulate(Market market) {

    }
}
