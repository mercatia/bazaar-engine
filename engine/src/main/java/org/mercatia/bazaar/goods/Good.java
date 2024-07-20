package org.mercatia.bazaar.goods;

import java.util.List;

import org.mercatia.bazaar.BootstrappedEntity;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.utils.Typed;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */
public class Good extends BootstrappedEntity {

	public static interface GoodType extends Typed {}



	@JsonProperty("id")
	private String id = ""; // string id of good

	@JsonProperty("size")
	private double size = 1.0; // inventory size taken up

	private GoodType type;

	public Good(GoodType type) {
		this.type = type;
	}

	public GoodType getType(){
		return this.type;
	}

	public double getSize(){
		return this.size;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Good@").append(Integer.toHexString(System.identityHashCode(this)))
				.append("[");
		sb.append("id=").append(id).append(" ");
		sb.append("size=").append(size).append("]");

		return sb.toString();
	}
}
