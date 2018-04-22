package com.example.mallocc.caloriecompanion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.backend.Product;
import com.backend.ScalesDevice;


public class NutritionFragment extends Fragment {

    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        v = inflater.inflate(R.layout.fragment_nurtition, container, false);
        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    public void update(final Product product)
    {
        Activity a = getActivity();
        if(a != null) {
            a.runOnUiThread(new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    TextView txt = getActivity().findViewById(R.id.text_name_2);
                    txt.setText(product.getName());

                    txt = getActivity().findViewById(R.id.text_barcode);
                    txt.setText(product.getBarcode());

                    if(product.getNutrition() != null) {
                        txt = getActivity().findViewById(R.id.text_calories_2);
                        txt.setText(product.getNutrition().getEnergy() + " kcal");

                        txt = getActivity().findViewById(R.id.text_fat);
                        txt.setText(ScalesDevice.toString(product.getNutrition().getFat()));
                        txt = getActivity().findViewById(R.id.text_sat);
                        txt.setText(ScalesDevice.toString(product.getNutrition().getSats()));
                        txt = getActivity().findViewById(R.id.text_carbs);
                        txt.setText(ScalesDevice.toString(product.getNutrition().getCarbs()));
                        txt = getActivity().findViewById(R.id.text_fibre);
                        txt.setText(ScalesDevice.toString(product.getNutrition().getFibre()));
                        txt = getActivity().findViewById(R.id.text_sugar);
                        txt.setText(ScalesDevice.toString(product.getNutrition().getSugars()));
                        txt = getActivity().findViewById(R.id.text_protein);
                        txt.setText(ScalesDevice.toString(product.getNutrition().getProtein()));
                        txt = getActivity().findViewById(R.id.text_salt);
                        txt.setText(ScalesDevice.toString(product.getNutrition().getSalt()));
                    }
                    else
                    {
                        txt = getActivity().findViewById(R.id.text_calories_2);
                        txt.setText("NA");

                        txt = getActivity().findViewById(R.id.text_fat);
                        txt.setText("NA");
                        txt = getActivity().findViewById(R.id.text_sat);
                        txt.setText("NA");
                        txt = getActivity().findViewById(R.id.text_carbs);
                        txt.setText("NA");
                        txt = getActivity().findViewById(R.id.text_fibre);
                        txt.setText("NA");
                        txt = getActivity().findViewById(R.id.text_sugar);
                        txt.setText("NA");
                        txt = getActivity().findViewById(R.id.text_protein);
                        txt.setText("NA");
                        txt = getActivity().findViewById(R.id.text_salt);
                        txt.setText("NA");
                    }
                }
            });
        }
    }
    public void interfaceState(final boolean state)
    {
        Activity a = getActivity();
        if(a != null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout holder = getActivity().findViewById(R.id.fragment_holder_2);
                    holder.animate().alpha(state ? 1.0f : 0.3f);
                    holder.setClickable(state);
                }
            });
        }
    }

    public void disableInterface()
    {
        interfaceState(false);
    }

    public void enableInterface()
    {
        interfaceState(true);
    }
}
