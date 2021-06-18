package org.proterra.bazaar.agent;

import java.util.Map;

/**
 * ...
 * @author larsiusprime
 */
class InventoryData
{
	public float  maxSize;
	public  Map<String, Float> ideal;
	public  Map<String, Float> start;
	public  Map<String, Float> size;

	public  InventoryData(float maxSize, Map<String,Float> ideal, Map<String,Float> start, Map<String,Float> size)
	{
		this.maxSize = maxSize;
		this.ideal = ideal;
		this.start = start;
		this.size = size;
	}

	//public static InventoryData fromJson(String data)
	//{
		//  maxSize:Int = data.max_size;
		//  ideal = new Map<String, Float>();
		//  start = new Map<String, Float>();
		//  size  = new Map<String, Float>();

		//  startArray = Reflect.fields(data.start);
		// if (startArray != null)
		// {
		// 	for (s in startArray)
		// 	{
		// 		start.set(s, cast Reflect.field(data.start, s));
		// 		size.set(s, 1);	//initialize size of every item to 1 by default
		// 	}
		// }
		//  idealArray = Reflect.fields(data.ideal);
		// if (idealArray != null)
		// {
		// 	for (i in idealArray)
		// 	{
		// 		ideal.set(i, cast Reflect.field(data.ideal, i));
		// 	}
		// }

		// return new InventoryData(maxSize, ideal, start, size);
//	}
}
