package com.example.mallocc.caloriecompanion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import backend.Product;

/**
 * Created by Alex on 27/06/2016.
 */
class ItemAdapter extends ArrayAdapter<Product>
{
    private final Context context;
    private ArrayList<Product> objects = new ArrayList<>();

    public ItemAdapter(Context context, ArrayList<Product> itemArrayList)
    {
        super(context, R.layout.row_item, itemArrayList);

        this.context = context;
        this.objects = itemArrayList;
    }


    @Override
    public int getCount()
    {
        return objects.size();
    }

    @Override
    public Product getItem(int i)
    {
        if (getCount() > 0)
            return objects.get(i);
        return null;
    }

    @Override
    public long getItemId(int i)
    {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent)
    {
        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.row_item, parent, false);

        // 3. Get the two text view from the rowView
        TextView labelView = rowView.findViewById(R.id.row_item_name);
        ImageView imageView = rowView.findViewById(R.id.image_view);

        // 4. Set the text for textView
        labelView.setText(objects.get(i).getName());


        // 5. return rowView
        return rowView;
    }

}
