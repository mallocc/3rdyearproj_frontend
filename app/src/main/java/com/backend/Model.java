package com.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Model {

    public static String TAG = "MODEL";

    private Product currentProduct;
    private float currentCalories;
    private HashMap<String, Product> sessionProducts;
    private HashMap<String, Product> localProducts;

    float offsetCalories = 0f;

    Model() {
        this.localProducts = new HashMap<>();
        this.sessionProducts = new HashMap<>();
        this.currentCalories = 0;
        this.currentProduct = null;
    }


    public Product getCurrentProduct() {
        return currentProduct;
    }

    public void setCurrentProduct(Product currentProduct) {
        this.currentProduct = currentProduct;
    }



    public HashMap<String, Product> getSessionProducts() {
        return sessionProducts;
    }

    public HashMap<String, Product> getLocalProducts() {
        return localProducts;
    }

    public ArrayList<Product> getLocalProductsList() {

        return new ArrayList<>(localProducts.values());
    }

    public void setLocalProducts(HashMap<String, Product> localProducts) {
        this.localProducts = localProducts;
    }

    public void setLocalProducts(ArrayList<Product> localProducts) {
        for(Product p : localProducts)
            this.localProducts.put(p.getBarcode(),p);
    }

    public void saveSessionProducts()
    {
        localProducts.putAll(sessionProducts);
        sessionProducts.clear();
    }



    void addProduct(Product product) {
        sessionProducts.put(product.getBarcode(), product);
    }

    boolean productExists(String barcode) {
        return getProductBarcode(barcode) != null;
    }

    Product getProductBarcode(String barcode) {
        Product p = localProducts.get(barcode);
        if(p != null)
            return p;
        p = sessionProducts.get(barcode);
        if(p != null)
            return p;
        return null;
    }

    Product getProductName(String name) {
        Iterator it = localProducts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Product p = (Product) pair.getValue();
            if (p.getName().toLowerCase().equals(name.toLowerCase())
                    || p.getSearchedName().toLowerCase().contains(name.toLowerCase()))
                return p;
        }
        it = sessionProducts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Product p = (Product) pair.getValue();
            if (p.getName().toLowerCase().equals(name.toLowerCase())
                    || p.getSearchedName().toLowerCase().contains(name.toLowerCase()))
                return p;
        }
        return null;
    }



    public float getCurrentCalories(float weight) {
        if (currentProduct != null && currentProduct.getNutrition() != null)
            return (currentCalories = weight * currentProduct.getNutrition().getEnergy() / 100.0f) + offsetCalories;
        return 0f;
    }

    public void resetWeight() {
        offsetCalories += currentCalories;
    }

    public void resetScales() {
        currentCalories = 0f;
        offsetCalories = 0f;
    }

    public float getOffsetCalories() {
        return offsetCalories;
    }
}
