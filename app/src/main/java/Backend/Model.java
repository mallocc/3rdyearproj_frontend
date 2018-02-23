package Backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Model
{

	
	Product currentProduct;
	float currentCalories;
	ArrayList<Product> sessionProducts;
	ArrayList<Product> localProducts;
	String databasePath;

	Model(String databasePath)
	{
		this.databasePath = databasePath;		
		this.localProducts = new ArrayList<>();
		this.sessionProducts = new ArrayList<>();
		this.currentCalories = 0;
		this.currentProduct = null;		
		
		loadDatabase(databasePath);
		
	}

	private void loadDatabase(String file)
	{
		File f = new File(databasePath);
		if (f.exists())
		{
			try
			{
				String line = null;	

				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				while ((line = bufferedReader.readLine()) != null)
					localProducts.add(Product.CSV2Product(line));
				bufferedReader.close();
				
				for(Product p : localProducts)
					System.out.println(p.toString());
				
				System.out.println(localProducts.size() + " products loaded from local.");
				
			} catch (FileNotFoundException ex)
			{
				System.out.println("Unable to open file '" + databasePath + "'");
			} catch (IOException ex)
			{
				System.out.println("Error reading file '" + databasePath + "'");
				// Or we could just do this:
				// ex.printStackTrace();
			}
		}
		else
		{
			try
			{
				f.createNewFile();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	public Product getCurrentProduct()
	{
		return currentProduct;
	}

	public void setCurrentProduct(Product currentProduct)
	{
		this.currentProduct = currentProduct;
	}

	public float getCurrentCalories(float weight)
	{
		return (currentCalories = weight * currentProduct.getNutrition().getEnergy()/100.0f);
	}

	public ArrayList<Product> getSessionProducts()
	{
		return sessionProducts;
	}

	public ArrayList<Product> getLocalProducts()
	{
		return localProducts;
	}

	void saveSessionProducts() throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(databasePath, true));
		for(Product p : sessionProducts)
		{
			writer.append(p.toCSV()+'\n');
			localProducts.add(p);
		}
	    writer.close();
	    sessionProducts = new ArrayList<>();
	}

	void saveScannedProducts()
	{

	}

	void addProduct(Product p)
	{
		sessionProducts.add(p);
	}
	
	boolean productExists(String barcode)
	{
		for(Product p : localProducts)
			if(p.getBarcode().equals(barcode))
				return true;
		for(Product p : sessionProducts)
			if(p.getBarcode().equals(barcode))
				return true;
		return false;
	}
	
	Product getProductBarcode(String barcode)
	{
		for(Product p : localProducts)
			if(p.getBarcode().equals(barcode))
				return p;
		for(Product p : sessionProducts)
			if(p.getBarcode().equals(barcode))
				return p;
		return null;
	}
	
	Product getProductName(String name)
	{
		for(Product p : localProducts)
			if(p.getName().equals(name))
				return p;
		for(Product p : sessionProducts)
			if(p.getName().equals(name))
				return p;
		return null;
	}
	
}
