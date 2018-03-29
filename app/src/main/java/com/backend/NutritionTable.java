package com.backend;

public class NutritionTable
{
	float energy =0, fat=0, sats=0, sugars=0, fibre=0, protein=0, salt=0, carbs=0;

	public NutritionTable()
	{
		
	}

	public NutritionTable(String energy, String fat, String sats, String sugars, String fibre, String protein, String salt, String carbs)
	{
		this.energy = Float.parseFloat(energy);
		this.fat = Float.parseFloat(fat);
		this.sats = Float.parseFloat(sats);
		this.sugars = Float.parseFloat(sugars);
		this.fibre = Float.parseFloat(fibre);
		this.protein = Float.parseFloat(protein);
		this.salt = Float.parseFloat(salt);
		this.carbs =  Float.parseFloat(carbs);;
	}

	public NutritionTable(float energy, float fat, float sats, float sugars, float fibre, float protein, float salt, float carbs)
	{
		this.energy = energy;
		this.fat = fat;
		this.sats = sats;
		this.sugars = sugars;
		this.fibre = fibre;
		this.protein = protein;
		this.salt = salt;
		this.carbs = carbs;
	}

	public float getCarbs()
	{
		return carbs;
	}

	public void setCarbs(float carbs)
	{
		this.carbs = carbs;
	}

	public void setEnergy(float energy)
	{
		this.energy = energy;
	}

	public void setFat(float fat)
	{
		this.fat = fat;
	}

	public void setSats(float sats)
	{
		this.sats = sats;
	}

	public void setSugars(float sugars)
	{
		this.sugars = sugars;
	}

	public void setFibre(float fibre)
	{
		this.fibre = fibre;
	}

	public void setProtein(float protein)
	{
		this.protein = protein;
	}

	public void setSalt(float salt)
	{
		this.salt = salt;
	}

	public float getEnergy()
	{
		return energy;
	}

	public float getFat()
	{
		return fat;
	}

	public float getSats()
	{
		return sats;
	}

	public float getSugars()
	{
		return sugars;
	}

	public float getFibre()
	{
		return fibre;
	}

	public float getProtein()
	{
		return protein;
	}

	public float getSalt()
	{
		return salt;
	}

	public String toString()
	{
		return "\n --Nutrition (per 100g): " + "\n   -Energy  = " + energy + " kcal" + "\n   -Fat     = " + fat + " g"
				+ "\n   -Sats    = " + sats + " g" + "\n   -Sugars  = " + sugars + " g" + "\n   -Fibre   = " + fibre
				+ " g" + "\n   -Protein = " + protein + " g" + "\n   -Salt    = " + salt + " g" + "\n   -Carbs   = " + carbs + " g";
	}

	public String toCSV()
	{
		String delim = ",";
		return energy + delim + fat + delim + sats + delim + sugars + delim + fibre + delim + protein + delim
				+ salt + delim + carbs;
	}

	public static NutritionTable CSV2Table(String[] fields)
	{
		return new NutritionTable(
				fields[0],
				fields[1],
				fields[2],
				fields[3],
				fields[4],
				fields[5],
				fields[6],
				fields[7]);
	}

}
