package com.backend;

import java.io.BufferedReader;

public class Product
{
	private String name;
	private String searchedName;
	private NutritionTable nutrition;
	private String barcode = "";
	private String tpnc = "";
	private String gtin = "";
	private String imageUrl = null;

	private static int PRODUCT_OFFSET_BARCODE = 0;
	private static int PRODUCT_OFFSET_GTIN = 1;
	private static int PRODUCT_OFFSET_TPNC = 2;
	private static int PRODUCT_OFFSET_NAME = 3;
	private static int PRODUCT_OFFSET_SEARCHED_NAME= 4;
	private static int PRODUCT_OFFSET_NUTRITION= 5;


	public Product(String name, String barcode, NutritionTable nutrition)
	{
		this.name = name;
		this.barcode = barcode;
		this.nutrition = nutrition;
		this.searchedName = "";
	}
	public Product(String name, String tpnc, String gtin, NutritionTable nutrition)
	{
		this.name = name;
		this.tpnc = tpnc;
		this.gtin = this.barcode = gtin;
		this.nutrition = nutrition;
		this.searchedName = "";
	}
	public Product(String name, String tpnc, String gtin, String barcode, NutritionTable nutrition)
	{
		this.name = name;
		this.tpnc = tpnc;
		this.gtin = gtin;
		this.barcode = barcode;
		this.nutrition = nutrition;
		this.searchedName = "";
	}

	public String getBarcode()
	{
		return barcode;
	}

	public String getName()
	{
		return name;
	}
	
	public void setNutrition(NutritionTable table)
	{
		this.nutrition = table;
	}
	public NutritionTable getNutrition()
	{
		return nutrition;
	}

	public static Product CSV2Product(String line)
	{
		BufferedReader br = null;
		String delim = ",";
		String[] record = line.split(delim);

		NutritionTable t = null;
		if(!record[PRODUCT_OFFSET_NUTRITION].equals("null"))
		{
			int pos = HelperUtils.ordinalIndexOf(line,delim,PRODUCT_OFFSET_NUTRITION);
			String[] fields = line.substring(pos+1).split(delim);
			t = NutritionTable.CSV2Table(fields);
		}

		Product p = new Product(
				record[PRODUCT_OFFSET_NAME],
				record[PRODUCT_OFFSET_TPNC],
				record[PRODUCT_OFFSET_GTIN],
				record[PRODUCT_OFFSET_BARCODE],
				t);

		p.setSearchedName(record[PRODUCT_OFFSET_SEARCHED_NAME]);

		return p;
	}
	
	public String toString()
	{
		return    "-Product: "
				+ "\n -GTIN:    " + gtin
				+ "\n -TPNC:    " + tpnc
				+ "\n -name:    " + name
				+ (nutrition != null ? nutrition.toString() : "\nNo nutrition.");
	}
	
	public String toCSV()
	{
		char delim = ',';
		return barcode + delim + gtin + delim + tpnc + delim + name + delim + searchedName + delim + (nutrition != null ? nutrition.toCSV() : "null");
	}

	public String getSearchedName() {
		return searchedName;
	}

	public void setSearchedName(String searchedName) {
		this.searchedName = searchedName;
	}

	public String getTPNC()
	{
		return tpnc;
	}

	public String getGTIN()
	{
		return gtin;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
