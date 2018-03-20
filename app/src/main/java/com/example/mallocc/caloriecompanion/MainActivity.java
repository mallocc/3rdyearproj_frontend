package com.example.mallocc.caloriecompanion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import backend.Controller;
import backend.Product;
import backend.simple.parser.ParseException;

import com.google.android.gms.samples.vision.barcodereader.BarcodeCaptureActivity;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class MainActivity extends AppCompatActivity {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set tab layout titles for each tab
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("scales"));
        tabLayout.addTab(tabLayout.newTab().setText("nutrition"));
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
        controller = Controller.getInstance(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/" +
                getResources().getString(R.string.filename));

        // Listen for changes in the weight from the scales device
        pollWeight();
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
        connectToDevice();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mCommandService != null)
//            mCommandService.stop();
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
                Toast.makeText(this, "Attempting to connect to scales...", Toast.LENGTH_SHORT).show();
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
     * @param view
     */
    public void tryReconnect(View view) {
        connectToDevice();
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


    private static final int RC_BARCODE_CAPTURE = 9001;

    /**
     * Starts the scanner activity.
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
     * @param view
     */
    public void processSpeech(View view) {

    }

    /**
     * Method for the search for item button.
     * Creates a dialog that the user inputs the search query.
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
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String query = input.getText().toString();
                showSearchedProductList(searchForProducts(query));
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
                dialog.cancel();
            }
        });

        builder.show();

    }


    /**
     * Updates the current product selected and updates the tabs.
     * @param product
     */
    private void updateCurrentProduct(Product product) {
        controller.setCurrentProduct(product);
        pagerAdapter.update(product);
    }

    /**
     * This uses the TescoAPI to get a list of products from a search query.
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

        if (foundProducts == null)
            return null;

        if (foundProducts.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No results");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();
        } else {
            controller.setLastSearchedProducts(foundProducts);
        }

        return foundProducts;
    }

    /**
     * This creates a dialog of the list of product previously obtained from a past search query.
     * @param products
     */
    private void showSearchedProductList(ArrayList<Product> products) {
        if (products == null || products.size() == 0)
            return;

        final AlertDialog builder = new AlertDialog.Builder(this).create();
        builder.setTitle("Results:");

        ItemAdapter adapter = new ItemAdapter(this, products);

        ListView listView = new ListView(this);
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
     * Starts a thread to listen to changes in the weight being sent from the scales device.
     */
    public void pollWeight() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mCommandService != null) {
                        pagerAdapter.update(
                                mCommandService.weight.getWeight(),
                                ((int) controller.getCurrentCalories(mCommandService.weight.weight) - (int) controller.getOffsetCalories()) + " kcal",
                                (int) controller.getCurrentCalories(mCommandService.weight.weight) + " kcal",
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
     * Resets the weight to zero.
     * @param view
     */
    public void resetWeight(View view) {
        controller.resetWeight();
    }

    /**
     * This resets the calories stored so far.
     * @param view
     */
    public void resetScales(View view) {
        controller.resetScales();
    }


    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    //If qr code has data, set clubid as qrcode
                    try {
                        Product product = controller.searchProductBarcode(barcode.displayValue);
                        if (product != null) {
                            controller.setCurrentProduct(product);
                            Toast.makeText(MainActivity.this, product.toString(), Toast.LENGTH_SHORT).show();
                            pagerAdapter.update(product);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                }
            } else {
            }
        } else {
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
                            hideReconnectButton();
                            break;
                        case BluetoothManager.STATE_CONNECTING:
                            Toast.makeText(MainActivity.this, "connecting...", Toast.LENGTH_SHORT);
                            break;
                        case BluetoothManager.STATE_LISTEN:
                        case BluetoothManager.STATE_NONE:
                            Toast.makeText(MainActivity.this, "not connected.", Toast.LENGTH_SHORT);
                            showReconnectButton();
                            break;
                    }
                    break;
                case BluetoothManager.MESSAGE_DEVICE_NAME:
                    Toast.makeText(getApplicationContext(), "Connected to scales.", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothManager.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothManager.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
