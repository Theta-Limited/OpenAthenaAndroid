// ManageDemsActivity
// Bobby Krupczak
// rdk@theta.limited

// Manage DEMs activity; from where, search, view,
// add/download, delete, import, export DEMs
// Re issue #85

package com.openathena;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import java.util.Locale;

public class ManageDemsActivity extends DemManagementActivity
{
    public static String TAG = ManageDemsActivity.class.getSimpleName();
    private EditText latLonText;
    private Button manageButton;
    private Button loadNewMapButton;
    private SwitchCompat maritimeModeSwitch;

    private ImageButton getPosGPSButton;
    private Button lookupButton;
    private Button resultsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG,"ManageDems onCreate started");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_dems);

        if (athenaApp == null) {
            athenaApp = (AthenaApp) getApplication();
        }

        manageButton = (Button)findViewById(R.id.manageCacheButton);
        loadNewMapButton = (Button) findViewById(R.id.loadNewMapButton);
        maritimeModeSwitch = (SwitchCompat) findViewById(R.id.maritime_mode_switch);

        dangerousMaritimeModeActivatedCount = athenaApp.getInt("dangerousMaritimeModeActivatedCount");
        maritimeModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean newIsMaritimeModeEnabled) {
                // inherited method from AthenaActivity
                // sets both the AthenaActivity and AthenaApp singleton static variable
                setIsMaritimeModeEnabled(newIsMaritimeModeEnabled);
                // Tell AthenaApp singleton that target should be recalculated
                AthenaApp.needsToCalculateForNewSelection = true;
                // Set label text to 'Enabled' or 'Disabled'
                compoundButton.setText(newIsMaritimeModeEnabled ? R.string.label_switch_enabled : R.string.label_switch_disabled);
                if (newIsMaritimeModeEnabled) {
                    displayMaritimeModeAlert();
                }
            }
        });
        maritimeModeSwitch.setChecked(isMaritimeModeEnabled);

        getPosGPSButton = (ImageButton) findViewById(R.id.get_pos_gps_button);
        latLonText = (EditText)findViewById(R.id.lookup_latlon_text);
        lookupButton = (Button)findViewById(R.id.lookupButton);
        resultsButton = (Button)findViewById(R.id.lookupResultsButton);

        progressBar = (ProgressBar)  findViewById(R.id.progressBar);
        if (showProgressBarSemaphore < 1) {
            progressBar.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }

        // If user has previously obtained self GPS location in another DemManagementActivity,
        //     load the result into this activity to save them time
        if (lastPointOfInterest != null && !lastPointOfInterest.isEmpty()) {
            latLonText.setText(lastPointOfInterest);
        }

        manageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                i = new Intent(getApplicationContext(), DemCacheListActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });
        loadNewMapButton.setOnClickListener(new View.OnClickListener() {
            Intent intent;

            @Override
            public void onClick(View v) {
                Log.d(TAG,"ManageDemsActivity: going to add/create a new DEM");
                intent = new Intent(getApplicationContext(),NewElevationMapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });



        getPosGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onClickGetPosGPS(); }
        });

        lookupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLookup();
            }
        });

        resultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onClickResults(); }
        });

        resultsButton.setEnabled(false);

    } // onCreate()

    private void displayMaritimeModeAlert() {
        if (dangerousMaritimeModeActivatedCount < 2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ManageDemsActivity.this);
            builder.setMessage(R.string.danger_maritime_mode_is_enabled);
            builder.setPositiveButton(R.string.i_understand_this_risk, (DialogInterface.OnClickListener) (dialog, which) -> {
                dangerousMaritimeModeActivatedCount += 1;
            });
            builder.setNegativeButton(R.string.disable_maritime_mode_action, (DialogInterface.OnClickListener) (dialog, which) -> {
                maritimeModeSwitch.setChecked(false);
                dangerousMaritimeModeActivatedCount += 1;
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }
    }

    @Override
    protected void updateLatLonText(Location location) {
        super.updateLatLonText(location);
        if (lastPointOfInterest != null && !lastPointOfInterest.isEmpty()) {
            latLonText.setText(lastPointOfInterest);
        }
    }


    // we have looked up a DEM and found an answer; click the results button
    // and we will jump to the ElevationMapDetails activity
    private void onClickResults()
    {
        Intent i;

        // cache selected item set in button callback; could be set here
        // as well; frankly, this function could be eliminated too
        // and rolled into the callback

        // create, launch intent
        i = new Intent(getApplicationContext(), DemDetailsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
    }

    // lookup a DEM based on the lat,lon coordinates; display results
    private void onClickLookup()
    {
        // hide the keyboard
        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null & getCurrentFocus() != null) {
            im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }

        resultsButton.setEnabled(false);
        athenaApp.demCache.selectedItem = -1;

        double lat = 0, lon = 0;
        String latLonStr = latLonText.getText().toString();

        Log.d(TAG,"ManageDems: splitting up "+latLonStr);

        // remove any () or degrees or leading/trailing whitespace
        // TODO add other languages and their degrees abbreviations
        latLonStr = latLonStr.trim();
        latLonStr = latLonStr.toUpperCase(Locale.ENGLISH);
        latLonStr = latLonStr.toUpperCase(Locale.ENGLISH).replaceAll("[()]", "");
        latLonStr = latLonStr.replaceAll("[Dd]egrees","°");
        latLonStr = latLonStr.replaceAll("[Dd]eg","°");

        try {
            double[] latLonPair = CoordTranslator.parseCoordinates(latLonStr);
            lat = latLonPair[0];
            lon = latLonPair[1];
        } catch (java.text.ParseException pe) {
            postResults(getString(R.string.button_lookup_please_enter));
            return;
        }

        if (lat == 0.0 && lon == 0.0) {
            postResults(getString(R.string.button_lookup_no_data));
            return;
        }

        // Save the user's location typed in the latLonText EditText field
        //     so they don't have to re-type it when navigating to other activities
        lastPointOfInterest = lat + "," + lon;
        saveStateToSingleton();

        // finally, do the lookup!
        postResults(getString(R.string.results_managedemsactivity_going_to_lookup)+lat+","+lon);

        String aFilename = athenaApp.demCache.searchCacheFilename(lat,lon);

        if (aFilename == null || aFilename == "") {
            postResults(getString(R.string.lookup_nothing_found));
        }
        else {
            postResults(getString(R.string.results_managedemsactivity_found_dem) + " " + aFilename);
            // set the cache entry selected
            athenaApp.demCache.setSelectedItem(aFilename);
            resultsButton.setEnabled(true);
        }
    }

    @Override
    protected void saveStateToSingleton() {
        super.saveStateToSingleton();
        athenaApp.putInt("dangerousMaritimeModeActivatedCount", dangerousMaritimeModeActivatedCount);
    }

    @Override
    protected void postResults(String resultStr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultsButton.setText(resultStr);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"ManageDems onResume");
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG,"ManageDems onPause");

        //appendText("onPause\n");
        super.onPause();

    } // onPause

    @Override
    protected void onDestroy()
    {
        Log.d(TAG,"ManageDems onDestroy started");

        super.onDestroy();

    } // onDestroy

    public void calculateImage(View view) { return; } // not used in this activity
    public void calculateImage(View view, boolean shouldISendCoT) { return; } // not used in this activity

} // ManageDemsActivity
