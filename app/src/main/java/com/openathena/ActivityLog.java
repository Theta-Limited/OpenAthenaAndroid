// ActivityLog.java
// Bobby Krupczak, rdk@krupczak.org Matthew Krupczak, mwk@krupczak.org, et. al
// access, display, clear the OpenAthena for Android logfile

package com.openathena;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.openathena.databinding.ActivityLogBinding;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;

public class ActivityLog extends AppCompatActivity {

    public static String TAG = ActivityLog.class.getSimpleName();

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        String aStr;

        Log.d(TAG,"onCreate started");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        // get our prefs that we have saved

        textView = (TextView)findViewById(R.id.log_text_view);

        // check for saved state

        // load contents of log file and insert them into textView
        aStr = readLog();

        textView.setText(aStr);
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

        if (id == R.id.action_calculate) {
            // jump to main activity
            intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }

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

        // don't do anything if user selects log

        return super.onOptionsItemSelected(item);
    }

    public void clearLogButton(View view) {

        // need to add an ability to clear the log as well
        Log.d(TAG,"clearLogButton starting");

        // truncate file
        FileOutputStream fos;
        PrintWriter pw;

        try {
            fos = openFileOutput(MainActivity.LOG_NAME, Context.MODE_PRIVATE);
            pw = new PrintWriter(fos);
            pw.print("OpenAthena for Android\nMatthew Krupczak, Bobby Krupczak, et al.\nGPL-3.0, some rights reserved\n");
            pw.close();
            fos.close();
            Log.d(TAG,"clearLogButton: reset logfile");

        } catch (Exception e) {
            Log.d(TAG,"clearLogButtonl: failed to write log:"+e.getMessage());
        }

        // read-read and insert into viewer
        String aStr = readLog();

        textView.setText(aStr);

    } // clear log button click

    @Override
    protected void onResume()
    {
        String aStr;

        Log.d(TAG,"onResume started");

        // re-read and re-display logfile
        aStr = readLog();

        textView.setText(aStr);

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

        // close log file not needed; closed after each use

        //appendText("onDestroy\n");
        // do whatever here
        super.onDestroy();

    } // onDestroy()

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

        Log.d(TAG,"appendLogLocal started");

        try {
            fos = openFileOutput(MainActivity.LOG_NAME, Context.MODE_PRIVATE|Context.MODE_APPEND);
            pw = new PrintWriter(fos);
            pw.print(str);
            pw.close();
            fos.close();
            Log.d(TAG,"appendLogLocal: wrote to logfile");

        } catch (Exception e) {
            Log.d(TAG,"appendLogLocal: failed to write log:"+e.getMessage());
        }

    } // appendLog()

    private String readLog()
    {
        FileInputStream fis;
        FileReader fr;
        int b;
        String aStr;

        try {
            fis = openFileInput(MainActivity.LOG_NAME);
            aStr = new String("");
            while ((b = fis.read()) != -1) {
                aStr = aStr + (char) b;
            }
            fis.close();
            Log.d(TAG,"readLog: opened and closed the log");
        } catch (Exception e) {
            Log.d(TAG,"failed to open or read from log file");
            aStr = "Error reading from logfile";
        }

        return aStr;

    } // readLog()

}