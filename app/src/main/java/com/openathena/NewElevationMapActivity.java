// NewElevationMapActivity.java
// Bobby Krupczak, Matthew Krupczak et al
// rdk@theta.limited
//
// Activity to create new DEM

package com.openathena;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.agilesrc.dem4j.dted.DTEDLevelEnum;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    protected File demDir;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG,"NewElevationMapActivity onCreate started");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dem);

        demDir = new File(getCacheDir(), "DEMs");

        instructionsLabel = (TextView)findViewById(R.id.new_dem_label);
        getPosGPSButton = (ImageButton) findViewById(R.id.get_pos_gps_button);
        latLonText = (EditText)findViewById(R.id.new_dem_latlon);
        metersText = (EditText)findViewById(R.id.new_dem_meters);
        downloadButton = (Button)findViewById(R.id.new_dem_downloadbutton);
        importButton = (Button)findViewById(R.id.new_dem_importbutton);

        progressBar = (ProgressBar)  findViewById(R.id.progressBar);
        if (showProgressBarSemaphore < 1) {
            progressBar.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }

        resultsLabel = (TextView)findViewById(R.id.new_dem_results);

        // If user has previously obtained self GPS location in another DemManagementActivity,
        //     load the result into this activity to save them time
        if (lastPointOfInterest != null && !lastPointOfInterest.isEmpty()) {
            latLonText.setText(lastPointOfInterest);
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
            Log.d(TAG,"NewElevationMapActivity: picked a file!");
            if (uri != null) {
                copyFileToCache(uri);
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = createLocationListener();

    } // onCreate()

    @Override
    protected void updateLatLonText(Location location) {
        super.updateLatLonText(location);
        if (lastPointOfInterest != null && !lastPointOfInterest.isEmpty()) {
            latLonText.setText(lastPointOfInterest);
        }
    }

    // handle a download
    private void onClickDownload()
    {
        // Sanity check to prevent user from spamming the download button
        if (!isGPSFixInProgress && showProgressBarSemaphore > 0) {
            return;
        }
        incrementAndShowProgressBar();

        String latlon = latLonText.getText().toString();
        latlon = latlon.trim();
        Log.d(TAG, "latlon is: " + latlon);
        if (latlon.equals("")) {
            postResults(getString(R.string.button_lookup_please_enter));
            decrementProgressBar();
            return;
        }

        String meters = metersText.getText().toString();
        double lat = 0, lon = 0, h = 15000;

        Log.d(TAG,"NewElevationMapActivity going to download from InterWebs");

        // hide the keyboard
        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null & getCurrentFocus() != null) {
            im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // process meters or length field
        meters = meters.replaceAll("meters","");
        meters = meters.replaceAll("meter", "");
        meters = meters.replaceAll("metres", "");
        meters = meters.replaceAll("metre", "");
        meters = meters.replaceAll("m", "");
        meters = meters.replaceAll("米", "");
        meters = meters.replaceAll("mètres", ""); // plural in French
        meters = meters.replaceAll("mètre", ""); // singular in French
        meters = meters.replaceAll("メートル", "");
        meters = meters.replaceAll("미터", "");
        meters = meters.replaceAll("미", "");
        meters = meters.replaceAll("metrów", ""); // plural in Polish
        meters = meters.replaceAll("metr", ""); // singular in Polish
        meters = meters.replaceAll("метров", "");
        meters = meters.replaceAll("метрів", "");
        meters = meters.replaceAll("метр", ""); // singular in Russian and Ukrainian
        meters = meters.trim();

        if (!meters.isEmpty()) {
            h = Double.parseDouble(meters);
        }

        // remove any () or degrees
        latlon = latlon.toUpperCase();
        latlon = latlon.toUpperCase().replaceAll("[()]", "");
        latlon = latlon.replaceAll("[Dd]egrees","°");
        latlon = latlon.replaceAll("[Dd]eg","°");
        // TODO remove degrees unit label from other languages as well

        try {
            double[] latLonPair = CoordTranslator.parseCoordinates(latlon);
            lat = latLonPair[0];
            lon = latLonPair[1];
        } catch (java.text.ParseException pe) {
            postResults(getString(R.string.button_lookup_please_enter));
            decrementProgressBar();
            return;
        }

        if (lat == 0 && lon == 0) {
            postResults(getString(R.string.results_zero_lat_zero_lon_msg));
            decrementProgressBar();
            return;
        }

        resultsLabel.setText(getString(R.string.results_going_to_fetch_elevation_map) + " ("+lat+","+lon+") x "+h+" ...");

        downloadNewDEM(lat, lon, h);

    } // onClickDownload

    // once selected, import the file and test it
    // to make sure its a valid DEM tiff file
    private void copyFileToCache(Uri fileUri) {
        String filePath = fileUri.getPath();
        if (filePath == null) filePath = "";
        resultsLabel.setText(getString(R.string.results_importing_local_file_msg) + "...");

        incrementAndShowProgressBar();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String demFilename = "DEM_LatLon";

                try (InputStream inputStream = getContentResolver().openInputStream(fileUri);
                     FileOutputStream outputStream = new FileOutputStream(new File(demDir,"import"))) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                } catch (IOException e) {
                    postResults(getString(R.string.results_error_accessing_file));
                    Log.e(TAG, "Error reading or writing the file to import", e);
                    decrementProgressBar();
                    return;
                }

                File importFile = new File(demDir, "import");
                DEMParser aParser = null;
                try {
                    aParser = new DEMParser(importFile);
                } catch (IllegalArgumentException e) {
                    postResults(getString(R.string.reults_failed_to_import_file) + "\n" + e.getMessage());
                    decrementProgressBar();
                    return;
                }

                double n = truncateDouble(aParser.getMaxLat(), 6);
                double s = truncateDouble(aParser.getMinLat(), 6);
                double e = truncateDouble(aParser.getMaxLon(), 6);
                double w = truncateDouble(aParser.getMinLon(), 6);
                String fileExt = ".tiff";
                // Use alternatively file names for DTED format files
                if (aParser.isDTED && aParser.dtedLevel != null) {
                    // OpenAthena only supports DTED level 2 or higher
                    // DTED4 not yet supported
                    if (aParser.dtedLevel == DTEDLevelEnum.DTED2) {
                        fileExt = ".dt2";
                    } else {
                        fileExt = ".dt3";
                    }
                }
                String newFilename = "DEM_LatLon_" + s + "_" + w + "_" + n + "_" + e + fileExt;

                File newFile = new File(demDir, newFilename);
                if (newFile.exists() && !newFile.delete()) {
                    postResults(getString(R.string.reults_failed_to_import_file_of_same_name));
                    decrementProgressBar();
                    return;
                }
                if (importFile.renameTo(newFile)) {
                    postResults(getString(R.string.results_imported_file_as)+ ": " + newFilename);
                } else {
                    postResults(getString(R.string.results_failed_to_import_file) + ": " + newFilename);
                }
                decrementProgressBar();
            }
        }).start();
    } // copyFileToCache

    // post results to the label making sure we do so on the UI thread;
    // while we're at it, refresh the DEM cache since we may have
    // imported a new file
    @Override
    protected void postResults(String resultStr)
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
        Log.d(TAG,"NewElevationMapActivity: going to pick a file to import");
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

    private double truncateDouble(double val, int precision) {
        double scale = Math.pow(10, precision);
        return Math.round(val * scale) / scale;
    }

} // NewElevationMapActivity
