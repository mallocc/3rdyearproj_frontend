package Backend;

import java.util.Random;

public class BarcodeReader
{
	public String scanBarcode()
	{
		return new Random().nextInt(Integer.MAX_VALUE/2) + Integer.MAX_VALUE/2 + "";
	}
}
