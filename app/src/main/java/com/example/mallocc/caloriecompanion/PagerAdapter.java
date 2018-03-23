package com.example.mallocc.caloriecompanion;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.backend.Product;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    ScalesFragment tab1= new ScalesFragment();
    NutritionFragment tab2 = new NutritionFragment();

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

    public void update(String weight, String calories, String offsetCalories, Product product)
    {
        tab1.update(weight,calories, offsetCalories,(product != null ? product.getName() : ""));
        if(product != null)
            tab2.update(product);
    }

    public void update(Product product)
    {
        if(product != null)
            tab2.update(product);
    }

    public void disableInterface()
    {
        tab1.disableInterface();
        tab2.disableInterface();
    }

    public void enableInterface()
    {
        tab1.enableInterface();
        tab2.enableInterface();
    }
}
