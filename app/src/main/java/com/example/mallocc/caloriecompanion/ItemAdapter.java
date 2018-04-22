package com.example.mallocc.caloriecompanion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.backend.Model;
import com.backend.Product;
import com.backend.TescoAPI;

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
    public View getView(int i, View convertView, ViewGroup parent)
    {
        final Product product = objects.get(i);
        final MainActivity activity = (MainActivity) context;

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.row_item, parent, false);

        // 3. Get the two text view from the rowView
        TextView labelView = rowView.findViewById(R.id.row_item_name);

        final ImageView imageView = rowView.findViewById(R.id.image_view);

        if(FileHandler.productImageExists(product, context))
        {
            Bitmap bitmap = activity.getProductBitmap(product);
            if (bitmap != null)
                imageView.setImageBitmap(bitmap);
        }

        if(product.getNutrition() == null)
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.no_information_outline));

        // 4. Set the text for textView
        labelView.setText(product.getName());


        // 5. return rowView
        return rowView;
    }

}
