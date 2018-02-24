package com.example.mallocc.caloriecompanion;

import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import backend.Controller;

public class MainActivity extends AppCompatActivity {

    Controller controller;


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


        controller = new Controller(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS) + "/" +
                        getResources().getString(R.string.filename));

        //MessageHandler is call when bytes are read from the serial input
        controller.setBluetoothSerial(new BluetoothSerial(this, new BluetoothSerial.MessageHandler() {
            @Override
            public int read(int bufferSize, byte[] buffer) {
                return doRead(bufferSize, buffer);
            }
        }, "newnam"));
    }

    public int doRead(int bufferSize, byte[] buffer) {
        //Log.e("output", new String(buffer));
        Toast.makeText(this, "got something", Toast.LENGTH_SHORT);
        return 1;
    }

    public void scan(View view) {

    }

    public void loadCamera(View view) {
        try {
            controller.getBluetoothSerial().read();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void processSpeech(View view) {

    }

    public void processText(View view) {

    }

    protected void onResume() {
        super.onResume();

        //onResume calls connect, it is safe
        //to call connect even when already connected
        controller.getBluetoothSerial().onResume();
    }
}
