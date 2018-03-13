package com.example.mallocc.caloriecompanion;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    TabFragScales tab1= new TabFragScales();
    TabFragNutrition tab2 = new TabFragNutrition();

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return tab1;
            case 1:

                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    public void update(String weight, String calories, String offsetCalories, String name)
    {
        tab1.update(weight,calories, offsetCalories, name);
    }
}
