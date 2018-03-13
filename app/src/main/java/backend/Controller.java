package backend;

import java.io.IOException;
import java.util.ArrayList;

import backend.simple.parser.ParseException;

public class Controller
{
	private Model model;
	private BarcodeReader reader;
	private TescoAPI cloud;
	
	public Controller(String databasePath)
	{
		model = new Model(databasePath);
		reader = new BarcodeReader();
		cloud = new TescoAPI();
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

	public ArrayList<Product> searchProductName(String name) throws IOException, ParseException {
		return cloud.searchName(name,1);
	}
	public Product searchProductBarcode(String barcode) throws IOException, ParseException {
		return cloud.searchBarcode(barcode);
	}

	public void setCurrentProduct(Product product)
	{
		model.setCurrentProduct(product);
	}

	public Product getCurrentProduct()
	{
		return model.getCurrentProduct();
	}

	public float getCurrentCalories(float weight)
	{
		return model.getCurrentCalories(weight);
	}

	public float getOffsetCalories()
	{
		return model.getOffsetCalories();
	}

	public void resetWeight()
	{
		model.resetWeight();
	}

	public void resetScales()
	{
		model.resetScales();
	}
}
