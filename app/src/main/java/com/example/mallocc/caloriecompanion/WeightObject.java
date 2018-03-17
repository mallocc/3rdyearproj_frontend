package com.example.mallocc.caloriecompanion;

/**
 * Created by hercu on 12-Mar-18.
 */

public class WeightObject{

    public float weight = 0.0f;
    public String raw = "";

    public void setRaw (String raw)
    {
        weight = Float.parseFloat(raw);
        this.raw = raw;
    }

    public String getWeight()
    {
        return toString(weight);
    }

    public static String toString(float weight_grams)
    {
        return (int)(weight_grams / (GlobalSettings.units == GlobalSettings.UNITS_KILOS ? 1000f : 1f))
                + (GlobalSettings.units == GlobalSettings.UNITS_KILOS ? " kg" : " g");
    }
}
