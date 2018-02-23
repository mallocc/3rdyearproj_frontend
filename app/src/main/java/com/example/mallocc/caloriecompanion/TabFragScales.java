package com.example.mallocc.caloriecompanion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


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

}
