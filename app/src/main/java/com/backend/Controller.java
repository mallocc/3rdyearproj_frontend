package com.backend;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.example.mallocc.caloriecompanion.FileHandler;
import com.example.mallocc.caloriecompanion.GlobalSettings;
import com.simpleJSON.parser.ParseException;

public class Controller {
    // Create a singleton of Controller
    private static Controller instance;

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }


    private Model model;
    private TescoAPI cloud;

    private ArrayList<Product> lastSearchedProducts = new ArrayList<>();

    public Controller() {
        model = new Model();
        cloud = new TescoAPI();
    }


    public ArrayList<Product> searchProductName(String name, int querySize) throws IOException, ParseException {
        ArrayList<Product> products = new ArrayList<>();

        Product product = model.getProductName(name);
        if (product != null) {
            products.add(product);
            return products;
        }

        products = cloud.searchName(name, querySize);
        for (Product p : products)
            p.setSearchedName(name);
        return products;
    }

    public Product searchProductBarcode(String barcode) throws IOException, ParseException {
        Product product = model.getProductBarcode(barcode);
        if (product != null)
            return product;
        else
            return cloud.searchBarcode(barcode);
    }

    public void setCurrentProduct(Product product) {
        model.setCurrentProduct(product);
        model.addProduct(product);
    }

    public Product getCurrentProduct() {
        return model.getCurrentProduct();
    }

    public ArrayList<Product> getLastSearchedProducts() {
        return lastSearchedProducts;
    }

    public void setLastSearchedProducts(ArrayList<Product> lastSearchedProducts) {
        this.lastSearchedProducts = lastSearchedProducts;
    }


    public float getCurrentCalories(float weight) {
        return model.getCurrentCalories(weight);
    }

    public float getOffsetCalories() {
        return model.getOffsetCalories();
    }

    public void resetWeight() {
        model.resetWeight();
    }

    public void resetScales() {
        model.resetScales();
    }



    public void readDatabase(Context context) throws IOException {

        ArrayList<Product> list = new ArrayList<>();
        String line;

        BufferedReader bufferedReader = FileHandler.readDatabase(GlobalSettings.DATABASE_NAME, context);
        if(bufferedReader != null) {
            while ((line = bufferedReader.readLine()) != null)
                list.add(Product.CSV2Product(line));
            bufferedReader.close();
        }

        System.out.println(list.size() + " products loaded from local.");

        model.setLocalProducts(list);
    }


    public void writeDatabase(Context context) throws IOException {
        model.saveSessionProducts();
        String data = "";
        for (Product p : model.getLocalProductsList())
            data += p.toCSV() + '\n';
        FileHandler.writeDatabase(GlobalSettings.DATABASE_NAME, data, context);
    }

}
