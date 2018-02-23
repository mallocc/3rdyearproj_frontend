package backend;

import java.io.IOException;
import java.util.Random;

import backend.simple.parser.ParseException;

public class App
{

	public static void main(String[] args) throws IOException, ParseException
	{
		//Controller con = new Controller("database.csv");
		//con.scanProduct();
		
		
		TescoAPI cloud = new TescoAPI();
		//System.out.println(cloud.searchName("cheese").size());
		
		//System.out.println(cloud.searchBarcode("5449000034335"));
		//System.out.println(cloud.searchBarcode("5024616003083"));
		//System.out.println(cloud.searchBarcode("5012583204749"));
		System.out.println(cloud.searchBarcode("5054775701377"));
		
		System.out.println(cloud.searchName("Tesco Finest Strawberry Pepper Dark Chocolate Popcorn 170 G").size());
	}

}
