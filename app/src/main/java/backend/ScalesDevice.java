package backend;

public class ScalesDevice
{
	Float currentWeight;
	
	public ScalesDevice()
	{
		super();
		this.currentWeight = 0f;
	}

	public float getCurrentWeight()
	{
		return currentWeight;
	}

	void poll()
	{
		
	}

	boolean disconnect()
	{
		return true;
	}
	
	boolean reset()
	{
		return true;
	}
	
	boolean powerOff()
	{
		return true;
	}

}
