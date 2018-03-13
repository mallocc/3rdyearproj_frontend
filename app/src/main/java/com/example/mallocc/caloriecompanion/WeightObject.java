package com.example.mallocc.caloriecompanion;

import android.os.Parcel;
import android.os.Parcelable;

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
}
