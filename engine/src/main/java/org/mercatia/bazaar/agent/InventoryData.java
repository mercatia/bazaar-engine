package org.mercatia.bazaar.agent;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**

 */
public class InventoryData {

	@JsonProperty("max_size")
	public float maxSize;
	
	@JsonProperty("ideal")
	public Map<String, Float> ideal;

	@JsonProperty("start")
	public Map<String, Float> start;


}
