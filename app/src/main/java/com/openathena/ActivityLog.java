// MainActivity.java
// Copyright 2022
// Bobby Krupczak, rdk@krupczak.org

package com.openathena;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.openathena.databinding.ActivityLogBinding;

public class ActivityLog extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        // get our prefs that we have saved

        textView = (TextView)findViewById(R.id.log_text_view);

        // check for saved state

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

        // don't do anything if user selects log

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