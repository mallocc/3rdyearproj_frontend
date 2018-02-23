package Backend;

import java.io.IOException;
import java.util.Random;

import simple.parser.ParseException;

public class App
{

	public static void main(String[] args) throws IOException, ParseException
	{
		//Controller con = new Controller("database.csv");
		//con.scanProduct();
		
		
		TescoAPI cloud = new TescoAPI();
		//System.out.println(cloud.searchName("cheese").size());
		
		System.out.println(cloud.searchName("chair").size());
		
	}

}
