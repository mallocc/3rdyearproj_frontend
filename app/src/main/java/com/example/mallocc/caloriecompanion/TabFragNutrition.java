package com.example.mallocc.caloriecompanion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class TabFragNutrition extends Fragment {
    private View v;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        v = inflater.inflate(R.layout.fragment_tab_frag_nutrition, container, false);
        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }
}
