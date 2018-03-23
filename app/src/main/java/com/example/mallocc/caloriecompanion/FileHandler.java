package com.example.mallocc.caloriecompanion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.backend.Product;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by hercu on 21-Mar-18.
 */

public class FileHandler {

    public static String TAG = "FileHandler";

    public static final String
            DATABASE_PATH = "CalorieCompanionDatabases",
            PHOTOS_PATH = "CalorieCompanionPictures";

    public static final String
            SETINGS_FILE = "settings.cfg";

    public static boolean productImageExists(Product product, Context context) {
        String path = context.getExternalFilesDir(null).toString();
        return new File(path + File.separator+ PHOTOS_PATH + File.separator + product.getBarcode() + ".jpg").exists();
    }

    public static Bitmap getBitmapUrl(String src)
    {
        Bitmap bitmap = null;
        try {
            URL url = new URL(src);
            bitmap = BitmapFactory.decodeStream((InputStream)url.getContent());
            Log.e(TAG, "scraped image");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return bitmap;
    }


    public static void writeImage(String name, Bitmap image, Context context) {
        File filename;
        try {
            String path = context.getExternalFilesDir(null).toString();

            new File(path + File.separator + PHOTOS_PATH).mkdirs();

            filename = new File(path + File.separator + PHOTOS_PATH + File.separator + name + ".jpg");

            FileOutputStream out = new FileOutputStream(filename);

            image.compress(Bitmap.CompressFormat.JPEG, 70, out);
            out.flush();
            out.close();

            Log.e(TAG, "File written at '" + filename + "'");
        } catch (Exception e) {
            Log.e(TAG, "Error writing image", e);
        }

    }

    public static Bitmap readImage(String name, Context context)
    {
        String path = context.getExternalFilesDir(null).toString();
        File file = new File(path + File.separator+ PHOTOS_PATH + File.separator + name + ".jpg");
        if(file.exists())
        {
            Log.e(TAG, "Read file at '" + file + "'");
            return BitmapFactory.decodeFile(file.toString());
        }
        else
        {
            Log.e(TAG, "No file at '" + file + "'");
            return null;
        }
    }

    public static void writeDatabase(String name, String data, Context context) {
        File filename;
        try {
            String path = context.getExternalFilesDir(null).toString();

            new File(path + File.separator + DATABASE_PATH).mkdirs();

            filename = new File(path + File.separator + DATABASE_PATH + File.separator + name);

            FileOutputStream out = new FileOutputStream(filename, false);

            out.write(data.getBytes());

            out.flush();
            out.close();

            Log.e(TAG, "File written at '" + filename + "'");
        } catch (Exception e) {
            Log.e(TAG, "Error writing database", e);
        }

    }

    public static BufferedReader readDatabase(String name, Context context)
    {
        String path = context.getExternalFilesDir(null).toString();
        File file = new File(path + File.separator + DATABASE_PATH + File.separator + name);
        if(file.exists())
        {
            Log.e(TAG, "Reading file at '" + file + "'");
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error reading file at '" + file + "'", e);
                return null;
            }
            return new BufferedReader(fileReader);
        }
        else
        {
            Log.e(TAG, "No file at '" + file + "'");
            return null;
        }
    }


    public static void saveSettings(Context context) {
        String settingsData = "";
        String delim = ",";
        settingsData += GlobalSettings.DATABASE_NAME + delim;
        settingsData += GlobalSettings.searchQuerySize + delim;
        settingsData += GlobalSettings.units + delim;

        File filename;
        try {
            String path = context.getExternalFilesDir(null).toString();

            filename = new File(path + File.separator + SETINGS_FILE);

            FileOutputStream out = new FileOutputStream(filename, false);

            out.write(settingsData.getBytes());

            out.flush();
            out.close();

            Log.e(TAG, "File written at '" + filename + "'");
        } catch (Exception e) {
            Log.e(TAG, "Error writing database", e);
        }

    }

    public static void loadSettings(Context context)
    {
        String data = "";

        String path = context.getExternalFilesDir(null).toString();
        File file = new File(path + File.separator + SETINGS_FILE);
        if(file.exists())
        {
            Log.e(TAG, "Reading file at '" + file + "'");
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
                String line = "";
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                if(bufferedReader != null) {
                    while ((line = bufferedReader.readLine()) != null)
                        data += line;
                    bufferedReader.close();
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error reading file at '" + file + "'", e);
            } catch (IOException e) {
                Log.e(TAG, "Error reading file at '" + file + "'", e);
            }

        }
        else
        {
            Log.e(TAG, "No file at '" + file + "'");
        }

        if(!data.equals(""))
        {
            String[] settings = data.split(",");
            if(settings.length == 3)
            {
                GlobalSettings.DATABASE_NAME = settings[0];
                GlobalSettings.searchQuerySize = Integer.parseInt(settings[1]);
                GlobalSettings.units = Integer.parseInt(settings[2]);

                Log.e(TAG, "Saved settings at '" + file + "'");
            }
        }
        else
        {
            saveSettings(context);
        }
    }

}
