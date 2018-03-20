package backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import backend.simple.JSONArray;
import backend.simple.JSONObject;
import backend.simple.parser.JSONParser;
import backend.simple.parser.ParseException;

public class TescoAPI
{
	static String TESCO_PRIVATE_KEY = "01516e1ca7814d9ea646b1f723556cef";

	public ArrayList<Product> searchName(String name, int results) throws IOException, ParseException
	{
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
			String desc = j.get("name").toString();
			Product p = getTPNC(tpnc);
			if (p.getNutrition() != null)
			{
				System.out.println("Nutrition table found");
				ps.add(p);
			}
			else
			{
				System.out.println("Nutrition table not found");
				NutritionTable nt = scapeTPNC(tpnc);
				if(nt !=  null)
					System.out.println("Nutrition table scaped");
				p.setNutrition(nt);
				ps.add(p);
			}
		}

		return ps;
	}

	public Product searchBarcode(String barcode) throws IOException, ParseException
	{
		Product p = null;

		p = getGTIN(barcode);
		// System.out.println(p);
		if (p == null)
			return null;
		else if (p.getNutrition() == null)
			p.setNutrition(scapeTPNC(p.tpnc));
		return p;
		// use getGTIN
		// if product is null then ask to search by name
		// if table is null then scrape tesco using tpnc (not guaranteed to be
		// correct if it isnt food)
		// else it should return a product with a table

	}

	private String getNutrient(JSONObject j)
	{
		Object o = j.get("valuePer100");
		if(o!=null)
			return o.toString();
		return null;
	}

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

				// iterate through the nutritents array
				JSONArray m = (JSONArray) j.get("calcNutrients");
				Iterator itr = m.iterator();
				j = (JSONObject) itr.next();

				// set all of the nutrients into the table
				j = (JSONObject) itr.next();
				s = getNutrient(j);
				if (s != null)
				{
					String[] ss = s.split("kj");
					if (ss.length > 1)
						nt.setEnergy(Float.parseFloat(ss[1]));
					else
						nt.setEnergy(Float.parseFloat(s));
				}

				j = (JSONObject) itr.next();
				s = getNutrient(j);
				if (s != null)
					nt.setFat(Float.parseFloat(s));

				j = (JSONObject) itr.next();
				s = getNutrient(j);
				if (s != null)
					nt.setSats(Float.parseFloat(s));

				j = (JSONObject) itr.next();
				s = getNutrient(j);
				if (s != null)
					nt.setCarbs(Float.parseFloat(s));

				j = (JSONObject) itr.next();
				s = getNutrient(j);
				if (s != null)
					nt.setSugars(Float.parseFloat(s));

				j = (JSONObject) itr.next();
				s = getNutrient(j);
				if (s != null)
					nt.setFibre(Float.parseFloat(s));

				j = (JSONObject) itr.next();
				s = getNutrient(j);
				if (s != null)
					nt.setProtein(Float.parseFloat(s));

				j = (JSONObject) itr.next();
				s = getNutrient(j);
				if (s != null)
					nt.setSalt(Float.parseFloat(s));

				// return new product
				return new Product(name, tpnc, gtin, nt);
			}
			return new Product(name, tpnc, gtin, null);
		}
		return null;
	}

	private Product getTPNC(String number) throws IOException, ParseException
	{
		// form request
		String url = "https://dev.tescolabs.com/product/?tpnc=" + number;

		return getJSONProduct(url);
	}

	private Product getGTIN(String number) throws IOException, ParseException
	{
		// form request
		String url = "https://dev.tescolabs.com/product/?gtin=" + number;

		return getJSONProduct(url);
	}
	private NutritionTable scapeTPNC(String number) throws IOException
	{
		NutritionTable nt = new NutritionTable();

		// form request
		String url = "https://www.tesco.com/groceries/en-GB/products/" + number;

		Connection con = null;
		Document doc = null;
		try {
			con = Jsoup.connect(url);
			if (con.response() == null)
				return null;
			doc = con.get();
			if (doc == null)
				return null;
		}catch (HttpStatusException e)
		{
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
			System.out.println(row.toString());
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
								nt.setEnergy(Float.parseFloat(split[1].replaceAll("kcal", "")));
							else if (ct.contains("energy") || ct.contains("-"))
								nt.setEnergy(Float.parseFloat(s.replaceAll("kcal", "")));

						} else
						{
							s = s.replaceAll("g", "");
						}

						if (ct.contains("fat"))
							nt.setFat(Float.parseFloat(s));
						if (ct.contains("sat"))
							nt.setSats(Float.parseFloat(s));
						if (ct.contains("sugar"))
							nt.setSugars(Float.parseFloat(s));
						if (ct.contains("fibre"))
							nt.setFibre(Float.parseFloat(s));
						if (ct.contains("protein"))
							nt.setProtein(Float.parseFloat(s));
						if (ct.contains("salt"))
							nt.setSalt(Float.parseFloat(s));
						if (ct.contains("carb"))
							nt.setCarbs(Float.parseFloat(s));
					}
					i++;
				}
				break;
			}
		}

		return nt;
	}
}
