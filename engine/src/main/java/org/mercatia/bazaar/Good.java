package org.mercatia.bazaar;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */
public class Good {
	@JsonProperty("id")
	public String id = ""; // string id of good

	@JsonProperty("size")
	public double size = 1.0; // inventory size taken up

	public Good() {

	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Good@").append(Integer.toHexString(System.identityHashCode(this)))
				.append("[");
		sb.append("id=").append(id).append(" ");
		sb.append("size=").append(size).append("]");

		return sb.toString();
	}
}
