// MainActivity.java
// Copyright 2022
// Bobby Krupczak, rdk@krupczak.org

// main activity; launch everything from here

// we need to figure out how to go back and forth between activities
// via our menu w/o forcing destroy and create
// Do this by adding flag to newly created intent which tells
// android to use existing activity rather than create new on
// if possible; otherwise create new activity
// intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

package com.openathena;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.FileOutputStream;

import com.openathena.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    public final static String PREFS_NAME = "openathena.preferences";
    public final static String version = "0.2";
    public static String TAG = MainActivity.class.getSimpleName();
    public final static String LOG_NAME = "openathena.log";

    TextView textView;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;
    protected String versionName;
    ImageView iView;
    Uri imageUri = null;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Handle the returned Uri
                        //appendText("Back from chooser\n");

                        if (uri == null)
                            return;

                        appendText("Back from chooser\n");
                        Log.d(TAG,"back from chooser");
                        imageSelected(uri);
                    }
                });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG,"onCreate started");

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.test_layout);

        // get our prefs that we have saved

        textView = (TextView)findViewById(R.id.textView);
        iView = (ImageView)findViewById(R.id.imageView);

        // try to get our version out of app/build.gradle
        // versionName field
        try {

            versionName = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
            Log.d(TAG, "Got version " + versionName);
        }
        catch (Exception e) {
            versionName = "unknown";
        }

        // check for saved state

        // open logfile for logging?  No, only open when someone calls
        // append

        textView.setText("OpenAthena for Android version "+versionName+"\nMatthew Krupczak, Bobby Krupczak, et al.\nCopyright 2022\n");
        appendLog("OpenAthena for Android version "+versionName+"\nMatthew Krupczak, Bobby Krupczak, et al.\nCopyright 2022\n");

    }

    // stolen from InterWebs
    // https://mobikul.com/pick-image-gallery-android/
    private String getPathFromURI(Uri uri)
    {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    // back from image selection dialog; handle it
    private void imageSelected(Uri uri)
    {
        String aPath;

        // save uri for later calculation
        imageUri = uri;

        //appendText("imageSelected: uri is "+uri+"\n");
        //appendText(uri.toString()+"\n");

        Log.d(TAG,"imageSelected: uri is "+uri);
        aPath = getPathFromURI(uri);
        Log.d(TAG,"imageSelected: path is "+aPath);

        iView.setImageURI(uri);
        appendLog("Selected image "+imageUri+"\n");

        //appendText("Image selected "+aPath+"\n");
        //appendLog("Image selected "+aPath+"\n");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;

        int id = item.getItemId();

        // don't do anything if user chooses Calculate; we're already there.

        if (id == R.id.action_prefs) {
            intent = new Intent(getApplicationContext(), PrefsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_about) {
            intent = new Intent(getApplicationContext(),AboutActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_log) {
            intent = new Intent(getApplicationContext(),ActivityLog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume started");
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG,"onPause started");
        //appendText("onPause\n");
        super.onPause();

    } // onPause()

    @Override
    protected void onDestroy()
    {
        Log.d(TAG,"onDestroy started");
        //appendText("onDestroy\n");
        // do whatever here

        // no need to close logfile; its closed after each use

        super.onDestroy();

    } // onDestroy()

    public void calculateImage(View view) {

        appendText("calculateImage clicked\n");
        appendLog("Going to start calculation\n");


    } // button click

    // select image button clicked; launch chooser and get result
    // in callback
    public void selectImage(View view)
    {
        Log.d(TAG,"selectImageClick started");
        appendLog("Going to start selecting image\n");
        appendText("selectImageClick started\n");

        //Intent i = new Intent();
        //i.setType("image/*");
        //i.setAction(Intent.ACTION_GET_CONTENT);
        //startActivity(Intent.createChooser(i,"Select Picture"));

        mGetContent.launch("image/*");

        // pass the constant to compare it
        // with the returned requestCode
        // StartActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);

        appendText("Chooser started\n");
        appendLog("Chooser started\n");
    }

    private void appendText(final String aStr)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(aStr);
            }
        });

    } // appendText to textView but do so on UI thread

    private void appendLog(String str)
    {
        FileOutputStream fos;
        PrintWriter pw;

        Log.d(TAG,"appendLog started");

        try {
            fos = openFileOutput(MainActivity.LOG_NAME, Context.MODE_PRIVATE|Context.MODE_APPEND);
            pw = new PrintWriter(fos);
            pw.print(str);
            pw.close();
            fos.close();
            Log.d(TAG,"appendLog: wrote to logfile");

        } catch (Exception e) {
            Log.d(TAG,"appendLog: failed to write log:"+e.getMessage());
        }

    } // appendLog()

}