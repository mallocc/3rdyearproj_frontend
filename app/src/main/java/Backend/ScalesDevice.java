package Backend;

public class ScalesDevice
{
	float currentWeight;
	BluetoothComms comms;
	
	public ScalesDevice()
	{
		super();
		this.currentWeight = 0;
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
