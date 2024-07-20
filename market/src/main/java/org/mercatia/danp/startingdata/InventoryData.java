package org.mercatia.danp.startingdata;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**

 */
public class InventoryData {

	@JsonProperty("max_size")
	public double maxSize;
	
	@JsonProperty("ideal")
	public Map<String, Double> ideal;

	@JsonProperty("start")
	public Map<String, Double> start;


}
