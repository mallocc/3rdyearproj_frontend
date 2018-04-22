package com.backend;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.simpleJSON.JSONArray;
import com.simpleJSON.JSONObject;
import com.simpleJSON.parser.JSONParser;
import com.simpleJSON.parser.ParseException;

public class TescoAPI
{
	static String TESCO_PRIVATE_KEY = "88f817216fc34b8083b96b2e39d628fa\n";

	static String TAG = "TESCO_API";

	static private String TESCO_IMAGE_TAG = "https://img.tesco.com/Groceries/pi/";
	static private String TESCO_TABLE_TAG = "<table>";
	static private String TESCO_TABLE_ENDTAG = "</table>";

	private Product getJSONProduct(String url) throws ParseException, IOException
	{
		// form request
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Ocp-Apim-Subscription-Key", TESCO_PRIVATE_KEY);

		// get response
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null)
			response.append(inputLine);
		in.close();
		String s = response.toString();
		System.out.println(s);
		// parse to JSON form
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse(s);

		// get products element
		JSONArray msg = (JSONArray) jsonObject.get("products");
		// iterate through the array
		Iterator iterator = msg.iterator();
		if (iterator.hasNext())
		{
			JSONObject j = (JSONObject) iterator.next();
			// get name of product
			String name = j.get("description").toString();
			// get barcode_24dp of product
			String gtin = j.get("gtin").toString();
			String tpnc = j.get("tpnc").toString();

			// if there is a calcNutrition section
			if (j.get("calcNutrition") != null)
			{
				// get the element
				s = j.get("calcNutrition").toString();
				j = (JSONObject) parser.parse(s);

				// create a new nutrition table to fill
				NutritionTable nt = new NutritionTable();

				// iterate through the nutrients array
				JSONArray m = (JSONArray) j.get("calcNutrients");

				// set all of the nutrients into the table
				s = getNutrient(m,"kcal");
				if (s != null)
				{
					String[] ss = s.split("kj");
					if (ss.length > 1)
						nt.setEnergy(checkValue(ss[1]));
					else
						nt.setEnergy(checkValue(s));
				}

				s = getNutrient(m, "fat");
				if (s != null)
					nt.setFat(checkValue(s));

				s = getNutrient(m, "sat");
				if (s != null)
					nt.setSats(checkValue(s));

				s = getNutrient(m, "carb");
				if (s != null)
					nt.setCarbs(checkValue(s));

				s = getNutrient(m, "sugar");
				if (s != null)
					nt.setSugars(checkValue(s));

				s = getNutrient(m, "fibre");
				if (s != null)
					nt.setFibre(checkValue(s));

				s = getNutrient(m,"protein");
				if (s != null)
					nt.setProtein(checkValue(s));

				s = getNutrient(m, "salt");
				if (s != null)
					nt.setSalt(checkValue(s));

				// return new product
				return new Product(name, tpnc, gtin, nt);
			}
			return new Product(name, tpnc, gtin, null);
		}
		return null;
	}




	private Product getTPNCProduct(String number) throws IOException, ParseException
	{
		// form request
		String url = "https://dev.tescolabs.com/product/?tpnc=" + number;
		return getJSONProduct(url);
	}

	private Product getProduct(String tpnc) throws IOException, ParseException {
		String TAG = this.TAG + "-getProduct";
		Product product = getTPNCProduct(tpnc);
		if(product == null)
		{
			Log.e(TAG,"Product failed to be created : " + tpnc);
			return null;
		}
		if (product.getNutrition() != null)
		{
			Log.e(TAG,"Nutrition table found on API");
			if(!product.getTPNC().equals(""))
				product.setImageUrl(scrapeTPNCImage(product.getTPNC()));
			return product;
		}
		else
		{
			Log.e(TAG,"Nutrition table not found on API");
			scapeNutrition(product);
			if(product.getNutrition() !=  null)
				Log.e(TAG,"Nutrition table scaped");
			else
				Log.e(TAG,"Nutrition table not able to be scaped");
			return product;
		}
	}

	public ArrayList<Product> searchName(String name, int results) throws IOException, ParseException
	{

		String TAG = this.TAG + "-SEARCH_NAME";

		// form request
		String query = name.replaceAll(" ", "+");
		String url = "https://dev.tescolabs.com/grocery/products/?query=" + query + "&offset=0&limit=" + results;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Ocp-Apim-Subscription-Key", TESCO_PRIVATE_KEY);

		// get response
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null)
			response.append(inputLine);
		in.close();
		String s = response.toString();

		// parse to JSON form
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse(s);

		// get to the results subsection
		s = jsonObject.get("uk").toString();
		jsonObject = (JSONObject) parser.parse(s);
		s = jsonObject.get("ghs").toString();
		jsonObject = (JSONObject) parser.parse(s);
		s = jsonObject.get("products").toString();
		jsonObject = (JSONObject) parser.parse(s);
		JSONArray msg = (JSONArray) jsonObject.get("results");

		// iterate through each section
		ArrayList<Product> ps = new ArrayList<>();
		Iterator iterator = msg.iterator();
		while (iterator.hasNext())
		{
			JSONObject j = (JSONObject) iterator.next();
			String tpnc = j.get("id").toString();
			Product product = getProduct(tpnc);
			if(product != null)
				ps.add(product);
		}

		return ps;
	}




	private Product getGTINProduct(String number) throws IOException, ParseException
	{
		// form request
		String url = "https://dev.tescolabs.com/product/?gtin=" + number;
		return getJSONProduct(url);
	}

	public Product searchBarcode(String barcode) throws IOException, ParseException
	{
		String TAG = this.TAG + "-searchBarcode";
		Product product = null;

		product = getGTINProduct(barcode);
		if(product == null)
		{
			Log.e(TAG,"Product failed to be created : " + barcode);
			return null;
		}
		if (product.getNutrition() != null)
		{
			Log.e(TAG,"Nutrition table found on API");
			if(!product.getTPNC().equals(""))
				product.setImageUrl(scrapeTPNCImage(product.getTPNC()));
			return product;
		}
		else
		{
			Log.e(TAG,"Nutrition table not found on API");
			scapeNutrition(product);
			if(product.getNutrition() !=  null)
				Log.e(TAG,"Nutrition table scaped");
			else
				Log.e(TAG,"Nutrition table not able to be scaped");
			return product;
		}
	}




	private String getNutrient(JSONArray m, String nutrientName)
	{
		Iterator itr = m.iterator();
		while(itr.hasNext())
		{
			JSONObject j = (JSONObject) itr.next();
			Object name = j.get("name");
			if(name != null
					&& name.toString().toLowerCase()
					.contains(
							nutrientName.toLowerCase()))
			{
				Object o = j.get("valuePer100");
				if(o!=null)
					return o.toString();
			}
		}
		Log.e(TAG + "-GET_NUTRIENT", nutrientName + " was not found");
		return null;
	}

	private float checkValue(String val)
	{
		if(val == null)
			return 0f;
		if(HelperUtils.checkFloat(val))
			return Float.parseFloat(val);
		else
			return 0f;
	}

	private Product scapeNutrition(Product product) throws IOException
	{
		String TAG = this.TAG + "-scapeNutrition";
		NutritionTable nt = new NutritionTable();

		// form request
		String url = "https://www.tesco.com/groceries/en-GB/products/" + product.getTPNC();

		Log.e(TAG, "Starting query...");
		Connection.Response response = null;
		Document doc = null;
		String body = null;
		try {
			response = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
					.referrer("http://www.google.com")
					.maxBodySize(0).execute();
			if (response== null)
			{
				Log.e(TAG, "connection response null");
				return null;
			}
			if(response.statusCode() == 404)
			{
				Log.e(TAG, "item not found on tesco.com");
				return null;
			}
			Log.e(TAG, "status code : " + response.statusCode());

			body = response.body();
			if (body == null)
			{
				Log.e(TAG, "null body");
				return null;
			}

		}catch (HttpStatusException e)
		{
			Log.e(TAG, "http status error", e);
			return null;
		}

		int pos = body.indexOf(TESCO_IMAGE_TAG);
		String image = body.substring(pos, body.indexOf(".jpg", pos)+4);
		System.out.println(image);
		product.setImageUrl(image);

		pos = body.indexOf(TESCO_TABLE_TAG);
		if(pos < 0)
			return null;

		String table = body.substring(pos, body.indexOf(TESCO_TABLE_ENDTAG, pos) + TESCO_TABLE_ENDTAG.length());
		doc = Jsoup.parse(table);
		if (doc == null)
		{
			Log.e(TAG, "null doc");
			return null;
		}

		Elements headerrow = doc.select("th");
		int col = 0;
		int i = 0;
		for (Element column : headerrow)
		{
			if (column.text().contains("100"))
				col = i;
			i++;
		}

		Elements rows = doc.select("tr");
		for (Element row : rows)
		{
			i = 0;
			Elements columns = row.select("td");
			for (Element c : columns)
			{
				String ct = c.text().toLowerCase();
				for (Element column : columns)
				{
					if (i == col)
					{
						String s = column.text();
						s = s.toLowerCase();
						if (s.contains("kcal"))
						{
							s = s.replaceAll("\\/", "");
							s = s.replaceAll("\\(", "");
							s = s.replaceAll("\\)", "");
							String[] split = s.split("kj");
							if (split.length > 1)
								nt.setEnergy(checkValue(split[1].replaceAll("kcal", "")));
							else if (ct.contains("energy") || ct.contains("-"))
								nt.setEnergy(checkValue(s.replaceAll("kcal", "")));

						} else
						{
							s = s.replaceAll("g", "");
						}

						if (ct.contains("fat"))
							nt.setFat(checkValue(s));
						if (ct.contains("sat"))
							nt.setSats(checkValue(s));
						if (ct.contains("sugar"))
							nt.setSugars(checkValue(s));
						if (ct.contains("fibre"))
							nt.setFibre(checkValue(s));
						if (ct.contains("protein"))
							nt.setProtein(checkValue(s));
						if (ct.contains("salt"))
							nt.setSalt(checkValue(s));
						if (ct.contains("carb"))
							nt.setCarbs(checkValue(s));
					}
					i++;
				}
				break;
			}
		}
		product.setNutrition(nt);
		return product;
	}




	private String scrapeTPNCImage(String tpnc) throws IOException {
		String TAG = this.TAG + "-IMAGE_SCAPE";

		// form request
		String url = "https://www.tesco.com/groceries/en-GB/products/" + tpnc;

		Connection.Response response = null;
		String body = null;
		try {
			response = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
					.referrer("http://www.google.com")
					.maxBodySize(0).execute();
			if (response== null)
			{
				Log.e(TAG, "connection response null");
				return "";
			}
			if(response.statusCode() == 404)
			{
				Log.e(TAG, "item not found on tesco.com");
				return "";
			}
			Log.e(TAG, "status code : " + response.statusCode());

			body = response.body();

			if (body == null)
			{
				Log.e(TAG, "null body");
				return "";
			}
		}catch (HttpStatusException e)
		{
			Log.e(TAG, "http status error", e);
			return "";
		}

		int pos = body.indexOf(TESCO_IMAGE_TAG);
		String image = body.substring(pos, body.indexOf(".jpg", pos)+4);
		System.out.println(image);
        return image;
    }


}
