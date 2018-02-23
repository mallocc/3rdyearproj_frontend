package Backend;

import java.io.IOException;

import simple.parser.ParseException;

public class Controller
{
	private Model model;
	private BarcodeReader reader;
	private TescoAPI cloud;
	private ScalesDevice scales;
	
	Controller(String databasePath)
	{
		model = new Model(databasePath);
		reader = new BarcodeReader();
		cloud = new TescoAPI();
		scales = new ScalesDevice();
	}
	
	void scanProduct() throws IOException, ParseException
	{
		String barcode = reader.scanBarcode();	
		Product p = null;
		if(!model.productExists(barcode))
			model.addProduct(p = cloud.searchBarcode(barcode));
		else
			p = model.getProductBarcode(barcode);
		if(p == null)
			throw new RuntimeException("Couldn't load product.");
		model.setCurrentProduct(p);
	}
	
	Product getProductBarcode(String barcode)
	{
		return model.getProductBarcode(barcode);
	}
	Product getProductName(String name)
	{
		return model.getProductName(name);
	}
	
	boolean resetWeight()
	{
		return scales.reset();
	}
	
	float getCurrentWeight()
	{
		return scales.getCurrentWeight();
	}
	
	boolean disconnectScales()
	{
		return scales.disconnect();
	}
	
	boolean powerOff()
	{
		return scales.powerOff();
	}
	
	
}
