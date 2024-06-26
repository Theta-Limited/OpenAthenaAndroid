// NewElevationMapActivity.java
// Bobby Krupczak, Matthew Krupczak et al
// rdk@theta.limited
//
// Activity to create new DEM

package com.openathena;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.util.Consumer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.openathena.databinding.ActivityAboutBinding;

import org.apache.commons.lang3.concurrent.LazyInitializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.text.ParseException;

public class NewElevationMapActivity extends DemManagementActivity
{
    public static String TAG = NewElevationMapActivity.class.getSimpleName();

    private TextView instructionsLabel;
    private ImageButton getPosGPSButton;
    private EditText latLonText;
    private EditText metersText;
    private Button downloadButton;
    private Button importButton;


    private TextView resultsLabel;
    private ActivityResultLauncher<String> importLauncher;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG,"NewDemActivity onCreate started");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dem);

        instructionsLabel = (TextView)findViewById(R.id.new_dem_label);
        getPosGPSButton = (ImageButton) findViewById(R.id.get_pos_gps_button);
        latLonText = (EditText)findViewById(R.id.new_dem_latlon);
        metersText = (EditText)findViewById(R.id.new_dem_meters);
        downloadButton = (Button)findViewById(R.id.new_dem_downloadbutton);
        importButton = (Button)findViewById(R.id.new_dem_importbutton);

        progressBar = (ProgressBar)  findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        showProgressBarSemaphore = 0;

        resultsLabel = (TextView)findViewById(R.id.new_dem_results);

        // If user has previously obtained self GPS location in another DemManagementActivity,
        //     load the result into this activity to save them time
        if (lastSelfLocation != null && !lastSelfLocation.isEmpty()) {
            latLonText.setText(lastSelfLocation);
        }

        getPosGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onClickGetPosGPS(); }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDownload();
            }
        });

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickImport();
            }
        });

        // create the activityresultlauncher here because of various threading
        // constraints
        importLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            Log.d(TAG,"NewDemActivity: picked a file!");
            if (uri != null) {
                copyFileToPrivateStorage(uri);
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = createLocationListener();

    } // onCreate()

    @Override
    protected void updateLatLonText(Location location) {
        super.updateLatLonText(location);
        if (lastSelfLocation != null && !lastSelfLocation.isEmpty()) {
            latLonText.setText(lastSelfLocation);
        }
    }

    // handle a download
    private void onClickDownload()
    {
        // Sanity check to prevent user from spamming the download button
        if (isGPSFixInProgress == false && showProgressBarSemaphore > 0) {
            return;
        }
        showProgressBarSemaphore++;
        progressBar.setVisibility(View.VISIBLE);

        String latlon = latLonText.getText().toString();
        latlon = latlon.trim();
        Log.d(TAG, "latlon is: " + latlon);
        if (latlon.equals("")) {
            postResults(getString(R.string.button_lookup_please_enter));
            showProgressBarSemaphore--;
            if (showProgressBarSemaphore<=0) {
                progressBar.setVisibility(View.GONE);
            }
            return;
        }

        String meters = metersText.getText().toString();
        double lat = 0, lon = 0, h = 15000;

        Log.d(TAG,"NewDemActivity going to download from InterWebs");

        // hide the keyboard
        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null & getCurrentFocus() != null) {
            im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // process meters or length field
        meters = meters.replaceAll("meters","");
        meters = meters.replaceAll("metres", "");
        meters = meters.replaceAll("m", "");
        meters = meters.trim();

        if (meters.equals("")) {
            h = 15000.00;
        }
        else {
            h = Double.parseDouble(meters);
        }

        // remove any () or degrees
        latlon = latlon.toUpperCase();
        latlon = latlon.toUpperCase().replaceAll("[()]", "");
        latlon = latlon.replaceAll("[Dd]egrees","°");
        latlon = latlon.replaceAll("[Dd]eg","°");


        try {
            double[] latLonPair = CoordTranslator.parseCoordinates(latlon);
            lat = latLonPair[0];
            lon = latLonPair[1];
        } catch (java.text.ParseException pe) {
            postResults(getString(R.string.button_lookup_please_enter));
            showProgressBarSemaphore--;
            if (showProgressBarSemaphore<=0) {
                progressBar.setVisibility(View.GONE);
            }
            return;
        }

        Log.d(TAG,"NewDemActivity going to fetch elevation map from the InterWebs");

        if (lat == 0 && lon == 0) {
            postResults("No elevation data for the middle of the ocean!");
            showProgressBarSemaphore--;
            if (showProgressBarSemaphore<=0) {
                progressBar.setVisibility(View.GONE);
            }
            return;
        }

        resultsLabel.setText("Going to fetch elevation map ("+lat+","+lon+") x"+h+" ...");

        DemDownloader aDownloader = new DemDownloader(getApplicationContext(),lat,lon,h);
        aDownloader.asyncDownload(new Consumer<String>() {
            @Override
            public void accept(String s) {
                Log.d(TAG,"NewDemActivity download returned "+s);
                postResults(s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgressBarSemaphore--;
                        if (showProgressBarSemaphore<=0) {
                            progressBar.setVisibility(View.GONE);
                        }
                        Toast t = Toast.makeText(NewElevationMapActivity.this,s,Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER,0,0);
                        t.show();
                    }
                });
            }
        });

    } // onClickDownload

    // once selected, import the file and test it
    // to make sure its a valid DEM tiff file
    private void copyFileToPrivateStorage(Uri fileUri)
    {
        String demFilename = "DEM_LatLon";

        // first, read the file and parse it as a DEM and see if its valid;
        // if not, return error
        // implement/resolve getFileName()
        // get the n,s,e,w values
        // write DEM_LatLon_s_w_n_e.tiff

        // first, copy the file to a local, private directory and call it
        // import.tiff; then we can evaluate it
        try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {

            try (OutputStream outputStream = this.openFileOutput("import.tiff",Context.MODE_PRIVATE)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            } catch (IOException e) {
                postResults("Error writing to private storage");
                Log.d(TAG,"NewDem: error writing to private storage "+e);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"NewDem: error reading the file to import "+e);
            postResults("Error accessing file");
            return;
        }

        // ok, we now have "import.tiff" to evaluate

        Log.d(TAG,"NewDem: imported file into local storage, now going to evaluate it");
        File importFile = new File(this.getFilesDir(),"import.tiff");
        DEMParser aParser = new DEMParser(importFile);
        if (aParser == null) {
            Log.d(TAG,"NewDem: are you sure this was a GeoTIFF file?");
            postResults("Are you sure this was a GeoTIFF file?");
            return;
        }

        // the DEM parser parsed it correctly; get the coordinates
        double n,s,e,w;
        n = truncateDouble(aParser.getMaxLat(),6);
        s = truncateDouble(aParser.getMinLat(),6);
        e = truncateDouble(aParser.getMaxLon(),6);
        w = truncateDouble(aParser.getMinLon(),6);

        // rename it; rename won't overwrite an existing
        // file of same name; thus, we delete the new file first

        String newFilename = "DEM_LatLon_"+s+"_"+w+"_"+n+"_"+e+".tiff";

        Log.d(TAG,"NewDem: "+s+","+w+","+n+","+e);
        File newFile = new File(this.getFilesDir(),newFilename);
        if (newFile.exists() && !newFile.delete()) {
            postResults("Failed to import file of same name");
            return;
        }
        boolean b = importFile.renameTo(newFile);
        if (b == true) {
            postResults("Imported "+newFilename);
        }
        else {
            postResults("Failed to import "+newFilename);
        }

    } // copyFileToPrivateStorage

    // post results to the label making sure we do so on the UI thread;
    // while we're at it, refresh the DEM cache since we may have
    // imported a new file
    private void postResults(String resultStr)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultsLabel.setText(resultStr);
            }
        });
        // no need for this op to block the UI thread
        athenaApp.demCache.refreshCache();
    }

    // handle an import button click
    private void onClickImport()
    {
        Log.d(TAG,"NewDemActivity: going to pick a file to import");
        // launch and give it .tiff as a restriction?
        // launcher takes mime-types; here are a few options
        // image/tiff -- just tiff files
        // image/* -- all images
        // */* -- all files, documents, images

        importLauncher.launch("*/*");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    } // onPause

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    } // onDestroy

    public void calculateImage(View view) { return; } // not used in this activity
    public void calculateImage(View view, boolean shouldISendCoT) { return; } // not used in this activity
    protected void saveStateToSingleton() { return; } // do nothing

    private double truncateDouble(double val, int precision) {
        double scale = Math.pow(10, precision);
        return Math.round(val * scale) / scale;
    }

} // NewElevationMapActivity
