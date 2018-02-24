package backend;

import com.example.mallocc.caloriecompanion.BluetoothSerial;

public class ScalesDevice
{
	float currentWeight;
	BluetoothSerial comms;
	
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

	public BluetoothSerial getBluetoothSerial()
	{
		return comms;
	}

	public void setBluetoothSerial(BluetoothSerial bt)
	{
		comms = bt;
	}
}
