package com.example.mallocc.caloriecompanion;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import backend.Product;


public class TabFragScales extends Fragment
{
    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        v = inflater.inflate(R.layout.activity_tab_frag_scales, container, false);
        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    public void update(final String weight, final String calories, final String offsetCalories, final String name)
    {
        Activity a = getActivity();
        if(a != null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView weight_text = getActivity().findViewById(R.id.text_weight);
                    weight_text.setText(weight);

                    TextView calorie_text = getActivity().findViewById(R.id.text_calories);
                    calorie_text.setText(calories);

                    TextView calorie_offset_text = getActivity().findViewById(R.id.text_offset);
                    calorie_offset_text.setText(offsetCalories);

                    TextView name_text = getActivity().findViewById(R.id.text_name);
                    name_text.setText(name);
                }
            });
        }
    }
}
