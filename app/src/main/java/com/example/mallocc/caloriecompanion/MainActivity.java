package com.example.mallocc.caloriecompanion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;

import backend.Controller;
import backend.Product;
import backend.simple.parser.ParseException;

import com.google.android.gms.samples.vision.barcodereader.BarcodeCaptureActivity;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class MainActivity extends AppCompatActivity {

    //private IntentIntegrator scanner;

    private Controller controller;

    private static String EXTRA_DEVICE_ADDRESS = "00:14:03:05:FD:D7";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for Bluetooth Command Service
    private volatile BluetoothCommandService mCommandService = null;

    private volatile WeightObject weight_object;

    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("scales"));
        tabLayout.addTab(tabLayout.newTab().setText("nutrition"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
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

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        controller = new Controller(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS) + "/" +
                        getResources().getString(R.string.filename));

        //reconnectThread();

        //scanner = new IntentIntegrator(this);

        pollWeight();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                //Write your logic here
                this.finish();
                return true;
            case R.id.menu_item_1:
                loadSettings(item);
                return true;
            case R.id.menu_item_2:
                loadHelp(item);
                return true;
            case R.id.menu_item_3:
                loadAbout(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean loadSettings(MenuItem item)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }

    private boolean loadHelp(MenuItem item)
    {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
        return true;
    }

    private boolean loadAbout(MenuItem item)
    {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
        return true;
    }

    private void connectToDevice() {
        Toast.makeText(this, "Attempting to connect to scales...", Toast.LENGTH_SHORT).show();

        // If BT is not on, request that it be enabled.
        // setupCommand() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        // otherwise set up the command service
        else {
            if (mCommandService == null)
                setupCommand();
        }

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices)
            if (device.getAddress().equals(EXTRA_DEVICE_ADDRESS)) {
                mCommandService.connect(device);
                break;
            }

    }

    @Override
    protected void onStart() {
        super.onStart();

        connectToDevice();

    }

    private void setupCommand() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mCommandService = new BluetoothCommandService(this, mHandler);
    }

    protected void startService() {
        if (mCommandService != null) {
            if (mCommandService.getState() == BluetoothCommandService.STATE_NONE) {
                mCommandService.start();
                Toast.makeText(MainActivity.this, mConnectedDeviceName, Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        startService();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCommandService != null)
            mCommandService.stop();
    }


    public void tryReconnect(View view) {
        connectToDevice();
    }

    public void showReconnectButton() {
        pagerAdapter.disableInterface();
        FloatingActionButton fbt_reconnect = findViewById(R.id.reconnect);
        fbt_reconnect.setVisibility(View.VISIBLE);
    }

    public void hideReconnectButton() {
        pagerAdapter.enableInterface();
        FloatingActionButton fbt_reconnect = findViewById(R.id.reconnect);
        fbt_reconnect.setVisibility(View.GONE);
    }

    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothCommandService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothCommandService.STATE_CONNECTED:
                            Toast.makeText(MainActivity.this, mConnectedDeviceName, Toast.LENGTH_SHORT);

                            hideReconnectButton();

                            break;
                        case BluetoothCommandService.STATE_CONNECTING:
                            Toast.makeText(MainActivity.this, "connecting...", Toast.LENGTH_SHORT);
                            break;
                        case BluetoothCommandService.STATE_LISTEN:
                        case BluetoothCommandService.STATE_NONE:
                            Toast.makeText(MainActivity.this, "not connected.", Toast.LENGTH_SHORT);

                            showReconnectButton();

                            break;
                    }
                    break;
                case BluetoothCommandService.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(BluetoothCommandService.DEVICE_NAME);
//                    Toast.makeText(getApplicationContext(), "Connected to "
//                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "Connected to scales"
                            , Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothCommandService.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothCommandService.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void scan(View view) {

    }

    private static final int RC_BARCODE_CAPTURE = 9001;
    public void loadCamera(View view) {
        //scanner.initiateScan();

//        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
//        intent.putExtra("PRODUCT_MODE", "ONE_D_MODE");
//        startActivityForResult(intent, 0);

        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    public void processSpeech(View view) {

    }

    private void showProductList()
    {

    }

    public void processText(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter product name to search:");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Product product = controller.searchProductName(input.getText().toString()).get(0);
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
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

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

    public void resetWeight(View view) {
        controller.resetWeight();
    }

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
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
