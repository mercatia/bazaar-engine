package org.proterra.bazaar;

/**

 */
public class Offer
{
	public String good;	//the thing offered
	public float units;			//how many units
	public float unit_price;	//price per unit
	public int agent_id;		//who offered this

	public Offer(int agent_id,String commodity,float units,float unit_price)
	{
		this.agent_id = agent_id;
		this.good = commodity;
		this.units = units;
		this.unit_price = unit_price;
	}

	public String toString()
	{
		return "("+agent_id + "): " + good + "x " + units + " @ " + unit_price;
	}
}
