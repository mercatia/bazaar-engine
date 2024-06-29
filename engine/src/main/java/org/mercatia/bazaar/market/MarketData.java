package org.mercatia.bazaar.market;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.Good;
import org.mercatia.bazaar.StartConditions;
import org.mercatia.bazaar.agent.AgentData;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.vertx.core.Vertx;

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

	public static abstract class Factory {
		
		protected Factory(){
			this.names = new ArrayList<String>();
		}

		protected List<String> names;
		protected MarketData startingMarketData;
		protected Economy economy;

		public MarketData.Factory name(String name){
			this.names.add(name);
			return this;
		}

		public MarketData.Factory name(List<String> names){
			this.names.addAll(names);
			return this;
		}


		public MarketData.Factory economy(Economy economy){
			this.economy = economy;
			return this;
		}

		public MarketData.Factory startingData(MarketData startingMarketData){
			this.startingMarketData = startingMarketData;
			return this;
		}

		public abstract List<Market> build(Vertx vertx);
	}

}
