package org.mercatia.bazaar;

import java.util.List;

import org.mercatia.bazaar.agent.AgentData;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */

public class MarketData {

	@JsonProperty("goods")
	public List<Good> goods;

	@JsonProperty("start_conditions")
	public StartConditions startConditions;

	@JsonProperty("agents")
	public List<AgentData> agents;

	public String toString() {
		StringBuilder sb = new StringBuilder("MarketData").append(System.lineSeparator());
		sb.append(startConditions).append(System.lineSeparator());
		sb.append(goods).append(System.lineSeparator());
		sb.append(agents).append(System.lineSeparator());
		return sb.toString();
	}
}
