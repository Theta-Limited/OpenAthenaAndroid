// ActivityLog.java
// Copyright 2022
// Bobby Krupczak, rdk@krupczak.org

// this activity shows our running log contents

package com.openathena;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.openathena.R;

public class MainActivity extends AppCompatActivity {

    public final static String PREFS_NAME = "openathena.preferences";
    public final static String version = "0.2";

    TextView textView;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get our prefs that we have saved

        textView = (TextView)findViewById(R.id.text_view);

        // check for saved state

        // load the logfile contents and put in the text view
        // need to add an ability to clear the log as well

        textView.setText("OpenAthena for Android\nMatthew Krupczak, Bobby Krupczak, et al.\nCopyright 2022\n");
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
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_about) {
           intent = new Intent(getApplicationContext(),AboutActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_log) {
           intent = new Intent(getApplicationContext(),ActivityLog.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        //appendText("onPause\n");
        super.onPause();

    } // onPause()

    @Override
    protected void onDestroy()
    {
        //appendText("onDestroy\n");
        // do whatever here
        super.onDestroy();

    } // onDestroy()

    public void clickButton(View view) {

        appendText("Button clicked\n");

    } // button click

    private void appendText(final String aStr)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(aStr);
            }
        });

    } // appendText to textView but do so on UI thread

}