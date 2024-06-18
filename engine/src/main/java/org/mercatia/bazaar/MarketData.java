package org.mercatia.bazaar;

import java.util.List;
import java.util.Map;

import org.mercatia.bazaar.agent.AgentData;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */

public class MarketData {

	@JsonProperty("start_conditions")
	public StartConditions startConditions;

	@JsonProperty("agents")
	public Map<String, AgentData> agents;

	@JsonProperty("goods")
	public Map<String, Good> goods;

	public String toString() {
		StringBuilder sb = new StringBuilder("MarketData").append(System.lineSeparator());
		sb.append(startConditions).append(System.lineSeparator());
		sb.append(goods).append(System.lineSeparator());
		sb.append(agents).append(System.lineSeparator());
		return sb.toString();
	}
}
