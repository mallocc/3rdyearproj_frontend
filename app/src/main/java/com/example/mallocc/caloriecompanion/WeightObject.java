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
        switch  (GlobalSettings.units)
        {
            case GlobalSettings.UNITS_KILOS:
                return (int)weight_grams / 1000f + " kg";
            case GlobalSettings.UNITS_GRAMS:
            default:
                return (int)weight_grams + " g";
        }
    }
}
