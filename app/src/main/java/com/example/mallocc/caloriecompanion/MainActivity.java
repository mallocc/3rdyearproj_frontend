package com.example.mallocc.caloriecompanion;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;

import backend.Controller;

public class MainActivity extends AppCompatActivity {

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
    private BluetoothCommandService mCommandService = null;

    private void init() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if (false)
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("scales"));
        tabLayout.addTab(tabLayout.newTab().setText("nutrition"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
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


 /*       controller = new Controller(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS) + "/" +
                        getResources().getString(R.string.filename));*/

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If BT is not on, request that it be enabled.
        // setupCommand() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        // otherwise set up the command service
        else {
            if (mCommandService==null)
                setupCommand();
        }

        //startService();
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices)
            if(device.getAddress().equals(EXTRA_DEVICE_ADDRESS))
            {
                mCommandService.connect(device);
                break;
            }
        // Attempt to connect to the device

    }
    private void setupCommand() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mCommandService = new BluetoothCommandService(this, mHandler);
    }

    protected  void startService()
    {
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

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothCommandService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothCommandService.STATE_CONNECTED:
                            Toast.makeText(MainActivity.this, mConnectedDeviceName, Toast.LENGTH_SHORT);
                            break;
                        case BluetoothCommandService.STATE_CONNECTING:
                            Toast.makeText(MainActivity.this, "connecting...", Toast.LENGTH_SHORT);
                            break;
                        case BluetoothCommandService.STATE_LISTEN:
                        case BluetoothCommandService.STATE_NONE:
                            Toast.makeText(MainActivity.this, "not connected.", Toast.LENGTH_SHORT);
                            break;
                    }
                    break;
                case BluetoothCommandService.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(BluetoothCommandService.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
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

    public void loadCamera(View view) {

    }

    public void processSpeech(View view) {

    }

    public void processText(View view) {
        mCommandService.write(new String("AT").getBytes());
    }

}
