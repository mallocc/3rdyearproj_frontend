package com.example.mallocc.caloriecompanion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import com.backend.Controller;
import com.backend.HelperUtils;
import com.backend.Model;
import com.backend.NutritionTable;
import com.backend.Product;
import com.simpleJSON.parser.ParseException;

import com.google.android.gms.samples.vision.barcodereader.BarcodeCaptureActivity;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class MainActivity extends AppCompatActivity {

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final int REQUEST_IMAGE_CAPTURE = 9002;
    private static final int TEST = 9003;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    // Controller for the link to the model
    private Controller controller;

    // Bluetooth device MAC address to connect to
    private static String EXTRA_DEVICE_ADDRESS = "00:14:03:05:FD:D7";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Member object for Bluetooth Command Service
    private volatile BluetoothManager mCommandService = null;

    // Pager adapter to communicate to the tabs
    private PagerAdapter pagerAdapter;

    // beeper
    MediaPlayer beeper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Scales");

        // load settings
        FileHandler.loadSettings(this);

        // Set tab layout titles for each tab
        TabLayout tabLayout = findViewById(R.id.tab_layout);
//        tabLayout.addTab(tabLayout.newTab().setText("scales"));
//        tabLayout.addTab(tabLayout.newTab().setText("nutrition"));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.scale_icon));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.information_outline));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Create the pager adapter for the tabs
        final ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        pagerAdapter = adapter;
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                android.support.v7.app.ActionBar actionBar = getSupportActionBar();
                if (tab.getPosition() == 0) actionBar.setTitle("Scales");
                if (tab.getPosition() == 1) actionBar.setTitle("Product Information");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



        // Gets the singleton instance of the Controller
        controller = Controller.getInstance();
        try {
            controller.readDatabase(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Listen for changes in the scalesDevice from the scales device
        pollWeight();

        beeper = MediaPlayer.create(this, R.raw.beep);

        tryReconnect(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Write your logic here
                this.finish();
                return true;
            case R.id.menu_item_1:
                loadSettings();
                return true;
            case R.id.menu_item_2:
                loadHelp();
                return true;
            case R.id.menu_item_3:
                loadAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //connectToDevice();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //startService();
    }

    @Override
    protected void onPause() {
        try {
            controller.writeDatabase(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {

//        if (mCommandService != null)
//            mCommandService.stop();


        super.onDestroy();
    }

    /**
     * Start settings Activity.
     */
    private void loadSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Start help activity.
     */
    private void loadHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    /**
     * Start about activity.
     */
    private void loadAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    /**
     * This handles the attempt to connect to the scales device.
     * Will create a new BluetoothManager if one doesn't exist.
     * Will test the bluetooth adapter.
     */
    private void connectToDevice() {

        if (mCommandService == null)
            getBluetoothManager();

        if (mCommandService != null) {

            if (mCommandService.getAdapter() == null) {
                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                finish();
                return;
            } else if (!mCommandService.adapterIsEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }


            // Get a set of currently paired devices
            Set<BluetoothDevice> pairedDevices = mCommandService.getBondedDevices();

            if (pairedDevices != null && mCommandService.getState() != BluetoothManager.STATE_CONNECTED) {
                //Toast.makeText(this, "Attempting to connect to scales...", Toast.LENGTH_SHORT).show();
                for (BluetoothDevice device : pairedDevices)
                    if (device.getAddress().equals(EXTRA_DEVICE_ADDRESS)) {
                        mCommandService.connect(device);
                        break;
                    }
            }
        }
    }

    private void getBluetoothManager() {
        // Initialize the BluetoothManager to perform bluetooth connections
        mCommandService = BluetoothManager.getInstance(mHandler);
    }

    /**
     * Method for the bluetooth reconnect button.
     *
     * @param view
     */
    public void tryReconnect(View view) {
        showLoadingBluetooth();
        connectToDevice();
    }


    public void showLoadingBluetooth() {
        findViewById(R.id.loading_circle_bluetooth).setVisibility(View.VISIBLE);
    }

    public void hideLoadingBluetooth() {
        findViewById(R.id.loading_circle_bluetooth).setVisibility(View.GONE);
    }

    public void showLoading() {
        findViewById(R.id.loading_circle_normal).setVisibility(View.VISIBLE);
        findViewById(R.id.textSearch).setVisibility(View.GONE);
        findViewById(R.id.speechSearch).setVisibility(View.GONE);
        findViewById(R.id.scanBarcode).setVisibility(View.GONE);
    }

    public void hideLoading() {
        findViewById(R.id.loading_circle_normal).setVisibility(View.GONE);
        findViewById(R.id.textSearch).setVisibility(View.VISIBLE);
        findViewById(R.id.speechSearch).setVisibility(View.VISIBLE);
        findViewById(R.id.scanBarcode).setVisibility(View.VISIBLE);
    }

    /**
     * Hides UI and shows reconnect button.
     */
    public void showReconnectButton() {
        pagerAdapter.disableInterface();
        FloatingActionButton fbt_reconnect = findViewById(R.id.reconnect);
        fbt_reconnect.setVisibility(View.VISIBLE);
    }

    /**
     * Shows UI and hides reconnect button.
     */
    public void hideReconnectButton() {
        pagerAdapter.enableInterface();
        FloatingActionButton fbt_reconnect = findViewById(R.id.reconnect);
        fbt_reconnect.setVisibility(View.GONE);
    }




    /**
     * Starts the scanner activity.
     *
     * @param view
     */
    public void startScanner(View view) {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    /**
     * Method for speech listening button.
     * Converts speech to text and processes the text.
     *
     * @param view
     */
    public void processSpeech(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Hi speak something");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            a.printStackTrace();
        }
    }

    /**
     * Method for the search for item button.
     * Creates a dialog that the user inputs the search query.
     *
     * @param view
     */
    public void processText(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter product name to search:");

// Set up the input
        final EditText input = new EditText(this);
        FrameLayout frame = new FrameLayout(this);

// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);

        input.setLayoutParams(params);
        frame.addView(input);

        builder.setView(frame);

// Set up the buttons
        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        if (controller.getLastSearchedProducts().size() > 0) {
            builder.setNeutralButton("Last Search", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showSearchedProductList(controller.getLastSearchedProducts());
                }
            });
        }
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String query = input.getText().toString().trim();
                if(!query.equals(""))
                {
                    new ProductSearchAsyncTask().execute(query);
                    dialog.dismiss();
                }
            }
        });

    }


    /**
     * Updates the current product selected and updates the tabs.
     *
     * @param product
     */
    private void updateCurrentProduct(Product product) {
        controller.setCurrentProduct(product);
        pagerAdapter.update(product);
    }


    private class ProductSearchAsyncTask extends AsyncTask<String, Void, ArrayList<Product>> {
        @Override
        protected ArrayList<Product> doInBackground(String... strings) {
            return searchForProducts(strings[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<Product> products) {
            super.onPostExecute(products);
            hideLoading();
            showSearchedProductList(products);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }
    }

    private class BarcodeSearchAsyncTask extends AsyncTask<String, Void, Product> {
        private String barcode = null;
        private boolean timedOut = false;
        @Override
        protected Product doInBackground(String... strings) {
            try {
                barcode = strings[0];
                return controller.searchProductBarcode(barcode);
            } catch(SocketTimeoutException e)
            {
                timedOut = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Product product) {
            super.onPostExecute(product);
            hideLoading();

            if(timedOut) {
                Snackbar.make(findViewById(R.id.content), "Product search time out. Please scan again.", Snackbar.LENGTH_SHORT).show();
                startScanner(null);
            }
            if (product != null) {
                controller.setCurrentProduct(product);
                pagerAdapter.update(product);
                Snackbar.make(findViewById(R.id.content), "Product found.", Snackbar.LENGTH_SHORT).show();
            } else {
                // new yesno dialog
                final AlertDialog.Builder yesnoBuilder = new AlertDialog.Builder(MainActivity.this);
                yesnoBuilder.setMessage("Product not found. Would you like to create a new one?");
                yesnoBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createNewProductFromBarcode(barcode);
                    }
                });
                yesnoBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                yesnoBuilder.show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }
    }

    /**
     * This uses the TescoAPI to get a list of products from a search query.
     *
     * @param query
     * @return List of products.
     */
    private ArrayList<Product> searchForProducts(String query) {

        ArrayList<Product> foundProducts = null;

        try {
            foundProducts = controller.searchProductName(query,
                    Integer.parseInt(GlobalSettings.searchQuerySizes
                            [GlobalSettings.searchQuerySize]));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return foundProducts;
    }

    /**
     * This creates a dialog of the list of product previously obtained from a past search query.
     *
     * @param products
     */
    private void showSearchedProductList(ArrayList<Product> products) {

        if (products == null || products.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No results");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();

            return;
        } else
            controller.setLastSearchedProducts(products);



        final AlertDialog builder = new AlertDialog.Builder(this).create();
        builder.setTitle("Results:");

        final ItemAdapter adapter = new ItemAdapter(this, products);

        for(final Product product : products)
            if(!FileHandler.productImageExists(product, this))
                if(product.getImageUrl() != null)
                    new AsyncTask<String, Void, Bitmap>(){
                        @Override
                        protected Bitmap doInBackground(String... strings) {
                            return  FileHandler.getBitmapUrl(strings[0]);
                        }

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            super.onPostExecute(bitmap);
                            if(bitmap != null)
                                MainActivity.this.savePicture(product, bitmap);
                                MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }.execute(product.getImageUrl());

        View v = LayoutInflater.from(this).inflate(R.layout.item_list_holder, null, false);
        ListView listView = v.findViewById(R.id.list_item_list_holder);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = (Product) adapterView.getItemAtPosition(i);
                if (product != null)
                    updateCurrentProduct(product);
                builder.dismiss();
            }
        });
        builder.setView(listView);

        builder.setButton(Dialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Starts a thread to listen to changes in the scalesDevice being sent from the scales device.
     */
    public void pollWeight() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mCommandService != null) {
                        pagerAdapter.update(
                                mCommandService.scalesDevice.getWeight(),
                                ((int) controller.getCurrentCalories(mCommandService.scalesDevice.weight) - (int) controller.getOffsetCalories()) + " kcal",
                                (int) controller.getCurrentCalories(mCommandService.scalesDevice.weight) + " kcal",
                                controller.getCurrentProduct()
                        );
                    }
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    /**
     * Resets the scalesDevice to zero.
     *
     * @param view
     */
    public void resetWeight(View view) {
        controller.resetWeight();
    }

    /**
     * This resets the calories stored so far.
     *
     * @param view
     */
    public void resetScales(View view) {
        controller.resetScales();
    }



    private boolean validateString(String data, String fieldName) {
        boolean check = HelperUtils.checkFloat(data);
        if (!check)
            Toast.makeText(this, "Please enter valid number for '" + fieldName + "'", Toast.LENGTH_SHORT).show();
        return check;
    }


    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private Bitmap currentPhoto = null;

    public void viewTakenImage(View view) {
        showImage(currentPhoto);
    }

    public void showCurrentProductPhoto(View view)
    {
        final Product product = controller.getCurrentProduct();
        if(product != null)
            if(product.getImageUrl() == null)
            {
                showImage(getProductBitmap(product));
            }
            else if(!product.getImageUrl().equals(""))
            {
                if(!FileHandler.productImageExists(product, MainActivity.this))
                    new AsyncTask<String, Void, Bitmap>(){
                        @Override
                        protected Bitmap doInBackground(String... strings) {
                           return  FileHandler.getBitmapUrl(strings[0]);
                        }

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            showLoading();
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            super.onPostExecute(bitmap);
                            hideLoading();
                            if(bitmap != null)
                                savePicture(product, bitmap);
                            showImage(getProductBitmap(product));
                        }
                    }.execute(product.getImageUrl());
                else
                    showImage(getProductBitmap(product));
            }
    }

    public void showImage(Bitmap image)
    {
        if (image == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No photo taken.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.show();
        } else {
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(image);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setMinimumHeight(800);
            imageView.setMinimumWidth(450);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(imageView);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.show();
        }
    }

    private void createNewProductFromBarcode(final String barcode) {

        // new item dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create new product:");

        final View view = getLayoutInflater().inflate(R.layout.new_product_form, null);
        builder.setView(view);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText name = view.findViewById(R.id.new_product_name);
                if (name.getText().toString().equals("")) return;
                EditText cals = view.findViewById(R.id.new_product_calories);
                if (!validateString(cals.getText().toString(), "Calories")) return;
                EditText fat = view.findViewById(R.id.new_product_fat);
                if (!validateString(fat.getText().toString(), "Fat")) return;
                EditText sats = view.findViewById(R.id.new_product_sats);
                if (!validateString(sats.getText().toString(), "Saturated")) return;
                EditText sugars = view.findViewById(R.id.new_product_sugars);
                if (!validateString(sugars.getText().toString(), "Sugars")) return;
                EditText fibre = view.findViewById(R.id.new_product_fibre);
                if (!validateString(fibre.getText().toString(), "Fibre")) return;
                EditText protein = view.findViewById(R.id.new_product_protein);
                if (!validateString(protein.getText().toString(), "Protein")) return;
                EditText salt = view.findViewById(R.id.new_product_salt);
                if (!validateString(salt.getText().toString(), "Salt")) return;
                EditText carbs = view.findViewById(R.id.new_product_carbs);
                if (!validateString(carbs.getText().toString(), "Carbohydrates")) return;

                if(currentPhoto == null)
                {
                    Toast.makeText(MainActivity.this, "Please add a photo.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Product product = new Product(
                        name.getText().toString(),
                        barcode,
                        new NutritionTable(
                                cals.getText().toString(),
                                fat.getText().toString(),
                                sats.getText().toString(),
                                sugars.getText().toString(),
                                fibre.getText().toString(),
                                protein.getText().toString(),
                                salt.getText().toString(),
                                carbs.getText().toString()
                        )
                );
                updateCurrentProduct(product);

                savePicture(product,currentPhoto);
                currentPhoto = null;

                Snackbar.make(findViewById(R.id.content), "New product '"+product.getName()+"' created.", Snackbar.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        // new yesno dialog
        final AlertDialog.Builder yesnoBuilder = new AlertDialog.Builder(this);

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yesnoBuilder.show();
            }
        });

        // yes no on cancel dialog
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog1, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        currentPhoto = null;
                        dialog.dismiss();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        yesnoBuilder.setMessage("Do you want to discard current details?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener);

    }


    public void savePicture(Product product, Bitmap data)
    {
        FileHandler.writeImage(product.getBarcode(), data, this);
        product.setImageUrl(null);
    }

    public Bitmap getProductBitmap(Product product)
    {
        return FileHandler.readImage(product.getBarcode(), this);
    }

    public void editProductButton(View view)
    {
        editProduct(controller.getCurrentProduct());
    }

    public void editProduct(final Product product)
    {
        // new item dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit product:");

        final View view = getLayoutInflater().inflate(R.layout.new_product_form, null);
        builder.setView(view);

        final EditText name = view.findViewById(R.id.new_product_name);
        name.setText(product.getName());
        final EditText cals = view.findViewById(R.id.new_product_calories);
        cals.setText(""+product.getNutrition().getEnergy());
        final EditText fat = view.findViewById(R.id.new_product_fat);
        fat.setText(""+product.getNutrition().getFat());
        final EditText sats = view.findViewById(R.id.new_product_sats);
        sats.setText(""+product.getNutrition().getSats());
        final EditText sugars = view.findViewById(R.id.new_product_sugars);
        sugars.setText(""+product.getNutrition().getSugars());
        final EditText fibre = view.findViewById(R.id.new_product_fibre);
        fibre.setText(""+product.getNutrition().getFibre());
        final EditText protein = view.findViewById(R.id.new_product_protein);
        protein.setText(""+product.getNutrition().getProtein());
        final EditText salt = view.findViewById(R.id.new_product_salt);
        salt.setText(""+product.getNutrition().getSalt());
        final EditText carbs = view.findViewById(R.id.new_product_carbs);
        carbs.setText(""+product.getNutrition().getCarbs());

        currentPhoto = getProductBitmap(product);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (name.getText().toString().equals("")) return;
                if (!validateString(cals.getText().toString(), "Calories")) return;
                if (!validateString(fat.getText().toString(), "Fat")) return;
                if (!validateString(sats.getText().toString(), "Saturated")) return;
                if (!validateString(sugars.getText().toString(), "Sugars")) return;
                if (!validateString(fibre.getText().toString(), "Fibre")) return;
                if (!validateString(protein.getText().toString(), "Protein")) return;
                if (!validateString(salt.getText().toString(), "Salt")) return;
                if (!validateString(carbs.getText().toString(), "Carbohydrates")) return;

                if(currentPhoto == null)
                {
                    Toast.makeText(MainActivity.this, "Please add a photo.", Toast.LENGTH_SHORT).show();
                    return;
                }

                product.setName(name.getText().toString());
                product.setNutrition(new NutritionTable(
                        cals.getText().toString(),
                        fat.getText().toString(),
                        sats.getText().toString(),
                        sugars.getText().toString(),
                        fibre.getText().toString(),
                        protein.getText().toString(),
                        salt.getText().toString(),
                        carbs.getText().toString()
                ));

                savePicture(product,currentPhoto);
                currentPhoto = null;

                Snackbar.make(findViewById(R.id.content), "Product '"+product.getName()+"' updated.", Snackbar.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        // new yesno dialog
        final AlertDialog.Builder yesnoBuilder = new AlertDialog.Builder(this);

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yesnoBuilder.show();
            }
        });

        // yes no on cancel dialog
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog1, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        currentPhoto = null;
                        dialog.dismiss();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        yesnoBuilder.setMessage("Do you want to discard current details?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener);

    }


    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == RESULT_OK)
            if (requestCode == RC_BARCODE_CAPTURE) {
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {
                        Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

                        beeper.start();

                        new BarcodeSearchAsyncTask().execute(barcode.displayValue);
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                currentPhoto = (Bitmap) extras.get("data");
            }
            else if (requestCode == TEST && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Product product = new Product("", "testbarcode", null );
                savePicture(product, (Bitmap) extras.get("data"));
                showImage(getProductBitmap(product));

            }
            else if(requestCode == REQ_CODE_SPEECH_INPUT && null != data)
            {
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                new ProductSearchAsyncTask().execute(result.get(0));
            }
            else {
                super.onActivityResult(requestCode, resultCode, data);
            }

    }


    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothManager.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothManager.STATE_CONNECTED:
                            hideLoadingBluetooth();
                            hideReconnectButton();
                            break;
                        case BluetoothManager.STATE_CONNECTING:
                            break;
                        case BluetoothManager.STATE_LISTEN:
                        case BluetoothManager.STATE_NONE:
                            hideLoadingBluetooth();
                            showReconnectButton();
                            break;
                    }
                    break;
                case BluetoothManager.MESSAGE_DEVICE_NAME:
                    hideLoadingBluetooth();
                    Snackbar.make(findViewById(R.id.content), "Connected to scales.", Snackbar.LENGTH_SHORT).show();
                    break;
                case BluetoothManager.MESSAGE_TOAST:
                    hideLoadingBluetooth();
                    Snackbar.make(findViewById(R.id.content), msg.getData().getString(BluetoothManager.TOAST), Snackbar.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
