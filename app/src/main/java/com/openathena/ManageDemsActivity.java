// ManageDemsActivity
// Bobby Krupczak
// rdk@theta.limited

// Manage DEMs activity; from where, search, view,
// add/download, delete, import, export DEMs
// Re issue #85

package com.openathena;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;

import java.util.Locale;

public class ManageDemsActivity extends AthenaActivity
{
    public static String TAG = ManageDemsActivity.class.getSimpleName();
    private EditText latLonText;

    private ImageButton getPosGPSButton;
    private Button manageButton;
    private Button lookupButton;
    private Button resultsButton;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG,"ManageDems onCreate started");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_dems);

        manageButton = (Button)findViewById(R.id.manageCacheButton);

        getPosGPSButton = (ImageButton) findViewById(R.id.get_pos_gps_button);
        latLonText = (EditText)findViewById(R.id.lookup_latlon_text);
        lookupButton = (Button)findViewById(R.id.lookupButton);
        resultsButton = (Button)findViewById(R.id.lookupResultsButton);

        manageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                i = new Intent(getApplicationContext(),DemCacheActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Update the EditText with the latest location
                updateLatLonText(location);
                // Remove updates to save battery after location is obtained
                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {
                // Prompt user to enable GPS if disabled
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

    } // onCreate()

    private void onClickGetPosGPS() {
        boolean hasGPSAccess = requestPermissionGPS();
        if (hasGPSAccess) {
            try {
                // Request location updates; you might want to customize the request parameters
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            } catch (SecurityException se) {
                Toast.makeText(this, "Need GPS permission to fetch location", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.permissions_toast_error_msg), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLatLonText(Location location) {
        if (location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            String mgrs = CoordTranslator.toMGRS1m(lat,lon);
            if (outputModeIsMGRS() ) {
                latLonText.setText(mgrs);
            } else {
                latLonText.setText(String.format(Locale.getDefault(), "%f,%f", lat, lon));
            }
        }
    }

    private boolean requestPermissionGPS() {
        if (!hasAccessCoarseLocation() && !hasAccessFineLocation()) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, requestNo);
            requestNo++;
        }
        return (hasAccessCoarseLocation() || hasAccessFineLocation());
    }

    private boolean hasAccessFineLocation() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasAccessCoarseLocation() {
        return checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
        i = new Intent(getApplicationContext(),ElevationMapDetailsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
        return;
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
        latLonStr = latLonStr.trim();
        latLonStr = latLonStr.toUpperCase();
        latLonStr = latLonStr.toUpperCase().replaceAll("[()]", "");
        latLonStr = latLonStr.replaceAll("[Dd]egrees","°");
        latLonStr = latLonStr.replaceAll("[Dd]eg","°");

        try {
            double[] latLonPair = CoordTranslator.parseCoordinates(latLonStr);
            lat = latLonPair[0];
            lon = latLonPair[1];
        } catch (java.text.ParseException pe) {
            resultsButton.setText(R.string.button_lookup_please_enter);
            return;
        }

        if (lat == 0.0 && lon == 0.0) {
            resultsButton.setText(R.string.button_lookup_no_data);
            return;
        }

        // finally, do the lookup!
        resultsButton.setText("Going to lookup "+lat+","+lon);

        String aFilename = athenaApp.demCache.searchCacheFilename(lat,lon);

        if (aFilename == null || aFilename == "") {
            resultsButton.setText(R.string.lookup_nothing_found);
        }
        else {
            resultsButton.setText("Found "+aFilename);
            // set the cache entry selected
            athenaApp.demCache.setSelectedItem(aFilename);
            resultsButton.setEnabled(true);
        }
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
    protected void saveStateToSingleton() { return; } // do nothing

} // ManageDemsActivity

