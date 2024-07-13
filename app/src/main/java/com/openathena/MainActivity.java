// MainActivity.java
// Bobby Krupczak, rdk@krupczak.org, Matthew Krupczak, mwk@krupzak.org, et. al

// main activity; launch everything from here

// we need to figure out how to go back and forth between activities
// via our menu w/o forcing destroy and create
// Do this by adding flag to newly created intent which tells
// android to use existing activity rather than create new on
// if possible; otherwise create new activity
// intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

package com.openathena;
import static com.openathena.TargetGetter.degNormalize;
import static com.openathena.TargetGetter.haversine;

// import veraPDF fork of Adobe XMP core Java v5.1.0
import com.adobe.xmp.XMPException;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.text.Html;

import androidx.core.util.Consumer;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Map;

// Libraries from the U.S. National Geospatial Intelligence Agency https://www.nga.mil
import mil.nga.tiff.util.TiffException;

public class MainActivity extends DemManagementActivity {
    public static String TAG = MainActivity.class.getSimpleName();

    public static int dangerousAutelAwarenessCount;

    public static int dangerousMissingCameraIntrinsicsCount;

    public static int noDemApiKeyPresentDialogCount;

    // calculates elevation diff between WGS84 reference ellipsoid and EGM96 geoid
    //
    // Vertical datum of input and output elevation is usally EGM96,
    // but OpenAthena converts to WGS84 height above ellipsoid internally for calculation (as well as CoT output)
    // For more information on vertical datums see: https://vdatum.noaa.gov/docs/datums.html
    public static EGMOffsetProvider offsetAdapter = new EGM96OffsetAdapter();


    ScrollView scrollView;
    TextView textView;

//    Button buttonSelectDEM;
    Button buttonSelectImage;
    Button buttonCalculateAndSendCoT;

    protected String versionName;

    MetadataExtractor theMeta = null;
    DEMParser theParser = null;
    TargetGetter theTGetter = null;

    // This flag controls whether extra calculation info is added to Cursor on Target message output
    // Many Cursor on Target consumer applications use a small buffer size, so by default this information is not included
    public static final boolean IS_EXTENDED_COT_MODE_ACTIVE = false;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Handle the returned Uri
                        //appendText("Back from chooser\n");

                        if (uri == null)
                            return;

                        //appendText("Back from chooser\n");
                        Log.d(TAG,"back from chooser for image");
                        imageSelected(uri);
                    }
                });

//    ActivityResultLauncher<String> mGetDEM = registerForActivityResult(new ActivityResultContracts.GetContent(),
//            new ActivityResultCallback<Uri>() {
//                @Override
//                public void onActivityResult(Uri uri) {
//                    // Handle the returned Uri
//                    //appendText("Back from chooser\n");
//
//                    if (uri == null)
//                        return;
//
//                    //appendText("Back from chooser\n");
//                    Log.d(TAG,"back from chooser for DEM");
//                    demSelected(uri);
//                }
//            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate started");

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);

        outputModeRadioGroup = null;

        progressBar = (ProgressBar)  findViewById(R.id.progressBar);
        if (showProgressBarSemaphore < 1) {
            progressBar.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }

        buttonSelectImage = (Button) findViewById(R.id.selectImageButton); // ð
        buttonCalculateAndSendCoT = (Button) findViewById(R.id.calculateButton); // ð

        setButtonReady(buttonSelectImage, true);
        setButtonReady(buttonCalculateAndSendCoT, false);

        isImageLoaded = false;
        isDEMLoaded = false;

        scrollView = (ScrollView) findViewById(R.id.scrollView2);

        textView = (TextView)findViewById(R.id.textView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textViewTargetCoord = (TextView)findViewById(R.id.textViewTargetCoord);
        textViewTargetCoord.setMovementMethod(LinkMovementMethod.getInstance());

        iView = (MarkableImageView) findViewById(R.id.imageView);
        athenaApp = (AthenaApp) getApplication();
        // try to get our version out of app/build.gradle
        // versionName field
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
            Log.d(TAG, "Got version " + versionName);
        }
        catch (Exception e) {
            versionName = "unknown";
        }

        clearText();

        dangerousAutelAwarenessCount = athenaApp.getInt("dangerousAutelAwarenessCount");
        dangerousMissingCameraIntrinsicsCount = athenaApp.getInt("dangerousMissingCameraIntrinsicsCount");
        noDemApiKeyPresentDialogCount = athenaApp.getInt("noDemApiKeyPresentDialogCount");

        CharSequence textRestore = athenaApp.getCharSequence("textview");
        if (textRestore != null) {
            textView.setText(textRestore);
        }
        CharSequence textViewTargetCoordRestore = athenaApp.getCharSequence("textViewTargetCoord");
        if (textViewTargetCoordRestore != null) {
            textViewTargetCoord.setText(textViewTargetCoordRestore);
        }

        isTargetCoordDisplayed = athenaApp.getBoolean("isTargetCoordDisplayed");
        isImageLoaded = athenaApp.getBoolean("isImageLoaded");
        isDEMLoaded = athenaApp.getBoolean("isDEMLoaded");

        String storedDEMUriString = athenaApp.getString("demUri");
        if (storedDEMUriString != null && !storedDEMUriString.isEmpty()) {
            Log.d(TAG, "recovered demUri: " + storedDEMUriString);
        }

        // get our prefs that we have saved
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();

        // initialize MetadataExtractor and load droneModels.json
        new MetadataExtractor(this);
        // Restore the user's custom droneModels.json drone camera intrinsics database (if so configured)
        String storedDroneModelsJsonUriString = sharedPreferences.getString("droneModelsJsonUri", null);
        DroneParametersFromJSON droneModelsParser = MetadataExtractor.parameterProvider;
        if (storedDroneModelsJsonUriString != null && !storedDroneModelsJsonUriString.isBlank()) {
            try {
                droneModelsParser.loadJSONFromUri(Uri.parse(storedDroneModelsJsonUriString));
            } catch (IOException | JSONException e) {
                Log.e(TAG,"ERROR: attempted to restore an invalid DroneModels.json from persistent settings!");
                e.printStackTrace();
                // Use the droneModels.json file bundled with the app instead if user's file is invalid
                droneModelsParser.loadJSONFromAsset();
            }
        }

        if (isDEMLoaded) {
            if (athenaApp != null && athenaApp.getDEMParser() != null) { // load DEM from App singleton instance in mem
                theParser = athenaApp.getDEMParser();
                theTGetter = new TargetGetter(theParser);
                setButtonReady(buttonSelectImage, true);
            } else if (storedDEMUriString != null && !storedDEMUriString.isEmpty()) { // fallback, load DEM from disk (slower)
                Log.d(TAG, "loading demUri: " + storedDEMUriString);
                demUri = Uri.parse(storedDEMUriString);
                prefsEditor.putString("lastDEM", null); // clear lastDEM just in case it is invalid to prevent crash loop
                prefsEditor.apply(); // make the change persistent
                demSelected(demUri); // If successful, this will update lastDEM again
            } else { // this shouldn't ever happen, but just to be safe...
                isDEMLoaded = false;
                setButtonReady(buttonSelectImage, false);
                setButtonReady(buttonCalculateAndSendCoT, false);
            }
          // Get DEM used last time the application was launched
        } else if (sharedPreferences.getString("lastDEM", null) != null && !sharedPreferences.getString("lastDEM", "").isEmpty()) {
            String lastDEM = sharedPreferences.getString("lastDEM", "");
            Log.d(TAG, "loading last used demUri: " + lastDEM);
            demUri = Uri.parse(lastDEM);
            demSelected(demUri);
        }

        String storedUriString = athenaApp.getString("imageUri");
        if (storedUriString != null) {
            imageUri = Uri.parse(storedUriString);
            if (imageUri != null && imageUri.getPath() != null) Log.d(TAG, imageUri.getPath());
            AssetFileDescriptor fileDescriptor = null;
            try {
                fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(imageUri , "r");
                if (fileDescriptor != null) fileDescriptor.close();
            } catch(IOException e) {
                imageUri = null;
                isImageLoaded = false;
                Log.e(TAG, "ERROR while trying to reload image: " + e.getMessage());
            }

            if (imageUri != null && imageUri.getPath() != null) {
                imageSelected(imageUri);
            }
        }

//        set_selection_x(athenaApp.get_selection_x());
//        set_selection_y(athenaApp.get_selection_y());

        if (isImageLoaded) {
            if (AthenaApp.get_selection_x() != -1 && AthenaApp.get_selection_y() != -1) {
                iView.restoreMarker(AthenaApp.get_selection_x(), AthenaApp.get_selection_y());
            } else{
                iView.reset();
            }
        }

        restorePrefs(); // restore the outputMode from persistent settings
        if (athenaApp.needsToCalculateForNewSelection && isDEMLoaded && isImageLoaded) {
            calculateImage(iView, athenaApp.shouldISendCoT);
            athenaApp.needsToCalculateForNewSelection = false;
        }

        // load DEM cache for later reference
        athenaApp.demCache = new DemCache(getApplicationContext());
        Log.d(TAG,"DemCache: total storage "+athenaApp.demCache.totalStorage());
        Log.d(TAG,"DemCache: count "+athenaApp.demCache.count());

        String userChoseOfflineStr = getString(R.string.mainactivity_missing_dem_api_key_user_chose_offline);

        if (noDemApiKeyPresentDialogCount < 1 && getDemApiKey().trim().isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.missing_dem_api_key_dialog_text);
            builder.setPositiveButton(R.string.missing_dem_api_key_positive_take_me_there_now, (DialogInterface.OnClickListener) (dialog, which) -> {
                Log.d(TAG, "MainActivity: user navigating to ManageDroneModelsAndAPIKeyActivity to input an API Key");
                noDemApiKeyPresentDialogCount++;
                Intent intent = new Intent(getApplicationContext(), ManageDroneModelsAndAPIKeyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            });
            builder.setNegativeButton(R.string.missing_dem_api_key_negative, (DialogInterface.OnClickListener) (dialog, which) -> {
                noDemApiKeyPresentDialogCount++;
                Toast.makeText(this,userChoseOfflineStr,Toast.LENGTH_LONG).show();
                appendText(userChoseOfflineStr + "\n");
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    } // onCreate


    // save the current state of this activity to the athenaApp Singleton object
    @Override
    protected void saveStateToSingleton() {
        athenaApp.putInt("dangerousAutelAwarenessCount", dangerousAutelAwarenessCount);
        athenaApp.putInt("dangerousMissingCameraIntrinsicsCount", dangerousMissingCameraIntrinsicsCount);
        athenaApp.putInt("noDemApiKeyPresentDialogCount", noDemApiKeyPresentDialogCount);
        if (textView != null) {
            athenaApp.putCharSequence("textview", textView.getText());
        }
        if (textViewTargetCoord != null) {
            athenaApp.putCharSequence("textViewTargetCoord", textViewTargetCoord.getText());
        }
        athenaApp.putBoolean("isTargetCoordDisplayed", isTargetCoordDisplayed);
        athenaApp.putBoolean("isImageLoaded", isImageLoaded);
        athenaApp.putBoolean("isDEMLoaded", isDEMLoaded);
        if (imageUri != null) {
            athenaApp.putString("imageUri", imageUri.toString());
        }

        if (demUri != null) {
            Log.d(TAG, "saved demUri: " + demUri.toString());
            athenaApp.putString("demUri", demUri.toString());

            athenaApp.setDEMParser(theParser);
        }
    }

    public void setButtonReady(Button aButton, boolean isItReady) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float enabled = 1.0f;
                float disabled = 0.5f;

                if (isItReady) {
                    aButton.setAlpha(enabled);
                    aButton.setClickable(true);
                } else {
                    aButton.setAlpha(disabled);
                    aButton.setClickable(false);
                }
            }
        });
    }

    // back from image selection dialog; handle it
    private void imageSelected(Uri uri)
    {
        File appCacheDir = new File(getCacheDir(), "images");
        if (!appCacheDir.exists()) {
            appCacheDir.mkdirs();
        }

        ContentResolver cr = getContentResolver();
        InputStream is;

        // Android 10/11, we can't access this file directly
        // We will copy the file into app's own package cache
        String fileName = getFileName(uri);
        File fileInCache = new File(appCacheDir, fileName);
        if (!isCacheUri(uri)) {
            try {
                try {
                    is = cr.openInputStream(uri);
                    OutputStream outputStream = new FileOutputStream(fileInCache);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    is.close();
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "FileNotFound imageSelected(): " + e.getMessage());
                    throw e;
                } catch (IOException e) {
                    Log.e(TAG, "IOException imageSelected():" + e.getMessage());
                    throw e;
                } catch (NullPointerException e) {
                    Log.e(TAG, "NullPointerException imageSelected():" + e.getMessage());
                    throw e;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            uri = Uri.fromFile(fileInCache); // use the uri of the copy in the cache directory
        }

        // reset marker and selection if new image is being loaded
        if (imageUri != null && !uri.equals(imageUri)) {
            clearText(); // clear attributes textView
            isTargetCoordDisplayed = false;
            restorePrefs(); // reset textViewTargetCoord to output mode descriptor

            isImageLoaded = false;
            iView.reset();// reset the marker to the center and reset pan and zoom values
            AthenaApp.set_selection_x(-1);
            AthenaApp.set_selection_y(-1);
        }
        imageUri = uri;

        //appendText("imageSelected: uri is "+uri+"\n");
        //appendText(uri.toString()+"\n");

        //Log.d(TAG,"imageSelected: uri is "+uri);
        //aPath = getPathFromURI(uri);
        //Log.d(TAG,"imageSelected: path is "+aPath);

        AssetFileDescriptor fileDescriptor;
        try {
            fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(uri , "r");
            if (fileDescriptor != null) fileDescriptor.close();
        } catch(IOException e) {
            imageUri = null;
            return;
        }

        iView.setImageURI(uri);

//        appendLog("Selected image "+imageUri+"\n");
        appendText(getString(R.string.image_selected_msg) + "\n");

        // Force the aspect ratio to be same as original image
        constrainViewAspectRatio();

        isImageLoaded = true;

        ExifInterface exif;
        Uri matchingDemURI;
        try {
            is = cr.openInputStream(imageUri);
            exif = new ExifInterface(is);
            double[] values = MetadataExtractor.getMetadataValues(exif);
            double lat = values[0];
            double lon = values[1];
            DemCache.DemCacheEntry dce = athenaApp.demCache.searchCacheEntry(lat, lon);
            if (dce != null) {
                matchingDemURI = dce.fileUri;
                Log.d(TAG, "matchingDemURI is: " + matchingDemURI.getPath());
                Log.d(TAG, "demUri is: " + ((demUri == null) ? "null" : demUri.getPath()));
                if (!matchingDemURI.equals(demUri) && demUri != null) {
                    if (dce.contains(lat, lon)) {
                        appendText(getString(R.string.main_activity_found_dem_in_cache_starting_load) + "\n");
                        demSelected(matchingDemURI);
                    } else {
                        appendText(getString(R.string.main_activity_no_dem_found_for_your_image) + "\n");
                        displayNewDemDownloadChoice(lat, lon);
                    }
                }
            } else {
                appendText(getString(R.string.main_activity_no_dem_found_for_your_image) + "\n");
                displayNewDemDownloadChoice(lat, lon);
            }

            is.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            appendText(getString(R.string.metadata_parse_error_msg) + "\n");
            e.printStackTrace();
        }

        if (isDEMLoaded) {
            setButtonReady(buttonCalculateAndSendCoT, true);
        }

    }

    private void demSelected(Uri uri) {
        setButtonReady(buttonCalculateAndSendCoT, false);

        incrementAndShowProgressBar();

        Handler myHandler = new Handler();

        // Load GeoTIFF in a new thread, this is a long-running task
        new Thread(() -> {
            Exception e = loadDEMnewThread(uri);
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (e == null) {
                        String prefix = theParser.isDTED ? "DTED DEM " : "GeoTIFF DEM ";
                        String successOutput = prefix;
                        successOutput += getString(R.string.dem_loaded_size_is_msg) + " " + theParser.getNumCols() + "x" + theParser.getNumRows() + " " + getString(R.string.mainactivity_dem_pixelunit_squares) + "\n";
                        appendText(successOutput);
                        printDEMBounds();
                        if (isImageLoaded) {
                            appendText(getString(R.string.dem_loading_finished_user_interaction_prompt) + "\n");
                        }
                        isDEMLoaded = true;
                        setButtonReady(buttonSelectImage, true);
                        if (isImageLoaded) {
                            setButtonReady(buttonCalculateAndSendCoT, true);
                        }
                        decrementProgressBar();
                    } else {
                        appendText(e.getMessage());
                        decrementProgressBar();
                    }
                }
            });
        }).start();
    }

    private Exception loadDEMnewThread(Uri uri) {
        File appCacheDir = new File(getCacheDir(), "DEMs");
        if (!appCacheDir.exists()) {
            appCacheDir.mkdirs();
        }

        // Android 10/11, we can't access this file directly
        // We will copy the file into app's own package cache
        String fileName = getFileName(uri);
        File fileInCache = new File(appCacheDir, fileName);
        if (!isCacheUri(uri)) {
            try {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    OutputStream outputStream = new FileOutputStream(fileInCache);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "FileNotFound demSelected()");
                    throw e;
                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                } finally {
//                    setButtonReady(buttonSelectDEM, true);
                }
            } catch (Exception e) {
                return e;
            }
        }

        demUri = Uri.fromFile(fileInCache);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();

        try {
            DEMParser parser = new DEMParser(fileInCache);
            theParser = parser;
            theTGetter = new TargetGetter(parser);
            prefsEditor.putString("lastDEM", demUri.toString()); // store the uri for fileInCache for next time the app is launched
            prefsEditor.apply(); // make change persistent
            return null;
        } catch (IllegalArgumentException e) {
            String failureOutput = getString(R.string.dem_load_error_generic_msg) + " " + e.getMessage();
            e.printStackTrace();
            return new Exception(failureOutput + "\n");
        } catch (TiffException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    decrementProgressBar();
                    Toast.makeText(MainActivity.this, R.string.wrong_filetype_toast_error_msg, Toast.LENGTH_LONG).show();
                }
            });
            String failureOutput = getString(R.string.dem_load_error_tiffexception_msg);
            e.printStackTrace();
            return new Exception(failureOutput + "\n");
        } finally {
//            setButtonReady(buttonSelectDEM, true);
            if (isDEMLoaded && isImageLoaded) {
                setButtonReady(buttonCalculateAndSendCoT, true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, getString(R.string.permissions_toast_success_msg), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.permissions_toast_error_msg), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG,"onDestroy started");
        super.onDestroy();
    }

    // overloaded, called by button press
    public void calculateImage(View view) {
        calculateImage(view, true);
    }

    public void calculateImage(View view, boolean shouldISendCoT)
    {
        Drawable aDrawable;
        ExifInterface exif;
        String attribs = "";

        clearText();
        textViewTargetCoord.setText("");

        appendText(getString(R.string.calculating_target_msg) + "\n");
//        appendLog("Going to start calculation\n");

        if (imageUri == null) {
//            appendLog("ERROR: Cannot calculate \uD83D\uDEAB\uD83E\uDDEE; no image \uD83D\uDEAB\uD83D\uDDBC selected\n");
            appendText(getString(R.string.no_image_selected_error_msg));
            return;
        }

        LinkedHashMap<String, String> openAthenaCalculationInfo = new LinkedHashMap<String,String>();
        // load image into object
        try {
            ContentResolver cr = getContentResolver();
            InputStream is = cr.openInputStream(imageUri);
            aDrawable = iView.getDrawable();
            exif = new ExifInterface(is);

            double[] values = MetadataExtractor.getMetadataValues(exif);
            double y = values[0];
            double x  = values[1];
            // MetadataExctractor.getMetadataValues auto converts vertical datum to WGS84 ellipsoidal
            double z = values[2];
            double azimuth = values[3];
            // pitch angle in OpenAthena convention, positive degrees downwards from horizon
            double theta = values[4];
            double roll = values[5];

            Log.i(TAG, "parsed xmpMeta\n");

            if (IS_EXTENDED_COT_MODE_ACTIVE) {
                openAthenaCalculationInfo.put("droneLatitude", roundDouble(y));
                openAthenaCalculationInfo.put("droneLongitude", roundDouble(x));
                openAthenaCalculationInfo.put("droneElevationHAE", roundDouble(z));
                openAthenaCalculationInfo.put("cameraRollAngleDeg", roundDouble(roll));
                openAthenaCalculationInfo.put("cameraSlantAngleDeg", roundDouble(theta));
            }

            appendText(getString(R.string.opened_exif_for_image_msg));
            attribs += MetadataExtractor.getTagString(ExifInterface.TAG_DATETIME, exif);
            attribs += MetadataExtractor.getTagString(ExifInterface.TAG_MAKE, exif);
            attribs += MetadataExtractor.getTagString(ExifInterface.TAG_MODEL, exif);
            attribs += getString(R.string.isCameraModelRecognized) + " " + (MetadataExtractor.isDroneModelRecognized(exif) ? getString(R.string.yes) : getString(R.string.no)) + "\n";
            attribs += getString(R.string.lens_type) + " " + (MetadataExtractor.getLensType(exif)) + "\n";

            String make = exif.getAttribute(ExifInterface.TAG_MAKE);
            if (make == null) make = "";
            String model = exif.getAttribute(ExifInterface.TAG_MODEL);
            if (model == null) model = "";
            if (IS_EXTENDED_COT_MODE_ACTIVE) {
                openAthenaCalculationInfo.put("make", make.toLowerCase());
                openAthenaCalculationInfo.put("model", model.toUpperCase());
                openAthenaCalculationInfo.put("isCameraModelRecognized", Boolean.toString(MetadataExtractor.isDroneModelRecognized(exif)));
                openAthenaCalculationInfo.put("lensType", MetadataExtractor.getLensType(exif));
            }

            // Iterate through camera distortion parameters from droneModels.json for drone camera
            LinkedHashMap<String, Double> paramMap = MetadataExtractor.getDistortionParameters(exif);
            if (paramMap != null && !paramMap.isEmpty()) {
                attribs += getString(R.string.distortion_parameters) + "\n";
                for (Map.Entry<String, Double> entry : paramMap.entrySet()) {
                    attribs += "    " + entry.getKey() + ": " + entry.getValue() + "\n";
                    if (IS_EXTENDED_COT_MODE_ACTIVE) openAthenaCalculationInfo.put(entry.getKey(), roundDouble(entry.getValue()));
                }
            }
            attribs += MetadataExtractor.getTagString(ExifInterface.TAG_FOCAL_LENGTH, exif);
            attribs += MetadataExtractor.getTagString(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, exif);
            attribs += MetadataExtractor.getTagString(ExifInterface.TAG_DIGITAL_ZOOM_RATIO, exif);
            attribs += MetadataExtractor.getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
            attribs += MetadataExtractor.getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
            // yikes
            if (IS_EXTENDED_COT_MODE_ACTIVE) {
                openAthenaCalculationInfo.put("focalLength", roundDouble(MetadataExtractor.rationalToFloat(exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH))));
                openAthenaCalculationInfo.put("digitalZoomRatio", exif.getAttribute(ExifInterface.TAG_DIGITAL_ZOOM_RATIO));
                openAthenaCalculationInfo.put("imageWidth", exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
                openAthenaCalculationInfo.put("imageLength", exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
            }

            double[] intrinsics = MetadataExtractor.getIntrinsicMatrixFromExif(exif);
            attribs += getString(R.string.focal_length_label) + " " + Math.round(intrinsics[0]) + "\n";
//            attribs += "fy: " + intrinsics[4] + "\n";
//            attribs += "cx: " + intrinsics[2] + "\n";
//            attribs += "cy: " + intrinsics[5] + "\n";
            attribs += getString(R.string.roll_label) + " " + roll + "°\n";
            if (IS_EXTENDED_COT_MODE_ACTIVE) {
                openAthenaCalculationInfo.put("f_x", roundDouble(intrinsics[0]));
                openAthenaCalculationInfo.put("f_y", roundDouble(intrinsics[4]));
//            openAthenaCalculationInfo.put("c_x", roundDouble(intrinsics[2]));
//            openAthenaCalculationInfo.put("c_y", roundDouble(intrinsics[5]));
            }

            double azimuthOffsetUserCorrection = athenaApp.getDouble("userOffset");
            if (azimuthOffsetUserCorrection != 0.0d) {
                attribs += getString(R.string.manual_correction_label) + " " + roundDouble(azimuthOffsetUserCorrection) + "°" + "\n";
            }
            azimuth += azimuthOffsetUserCorrection;

            if (IS_EXTENDED_COT_MODE_ACTIVE) {
                openAthenaCalculationInfo.put("azimuthOffsetUserCorrection", roundDouble(azimuthOffsetUserCorrection));
                openAthenaCalculationInfo.put("imageSelectedProportionX", roundDouble(AthenaApp.get_proportion_selection_x()));
                openAthenaCalculationInfo.put("imageSelectedProportionY", roundDouble(AthenaApp.get_proportion_selection_y()));
            }

            double[] relativeRay;
            try {
                if (AthenaApp.get_selection_x() < 0 || AthenaApp.get_selection_y() < 0) {
                    throw new NoSuchFieldException("no point was selected");
                } else {
                    relativeRay = MetadataExtractor.getRayAnglesFromImgPixel(AthenaApp.get_selection_x(), AthenaApp.get_selection_y(), roll, exif);
                }
            } catch (Exception e) {
                relativeRay = new double[] {0.0d, 0.0d};
                iView.theMarker = null;
                iView.invalidate();
                Log.i(TAG, "No point was selected in image. Using principal point (center) for calculation.");
            }

            double azimuthOffsetSelectedPoint = relativeRay[0];
            double thetaOffsetSelectedPoint = relativeRay[1];

            attribs += getString(R.string.azimuth_offset_label) + " " + Math.round(azimuthOffsetSelectedPoint) + "°\n";
            attribs += getString(R.string.pitch_offset_label) + " " + -1 * Math.round(thetaOffsetSelectedPoint) + "°\n";

            if (IS_EXTENDED_COT_MODE_ACTIVE) {
                openAthenaCalculationInfo.put("yawOffsetDegSelectedPoint", roundDouble(azimuthOffsetSelectedPoint));
                openAthenaCalculationInfo.put("pitchOffsetDegSelectedPoint", roundDouble(-1.0d * thetaOffsetSelectedPoint));
            }

            azimuth += azimuthOffsetSelectedPoint;
            theta += thetaOffsetSelectedPoint;

            if (!outputModeIsSlavic()) {
                attribs += getString(R.string.latitude_label_long) + " "+ roundDouble(y) + "°\n";
                attribs += getString(R.string.longitude_label_long) + " " + roundDouble(x) + "°\n";
                attribs += getString(R.string.altitude_label_long) + " " + Math.round(z * (isUnitFoot() ? AthenaApp.FEET_PER_METER : 1.0d)) + " " + (isUnitFoot() ? "ft.":"m") + "\n";
            } else {
                attribs += getString(R.string.latitude_wgs84_label_long) + " " + roundDouble(y) + "°\n";
                attribs += getString(R.string.longitude_wgs84_label_long) + " " + roundDouble(x) + "°\n";
                attribs += getString(R.string.altiude_wgs84_label_long) + " " + Math.round(z * (isUnitFoot() ? AthenaApp.FEET_PER_METER : 1.0d)) + " " + (isUnitFoot() ? "ft.":"m") + "\n";
            }

            try {
                double terrainAltitude = theParser.getAltFromLatLon(y, x);
                attribs += getString(R.string.terrain_altitude) + " " + Math.round(terrainAltitude * (isUnitFoot() ? AthenaApp.FEET_PER_METER : 1.0d)) + " " + (isUnitFoot() ? "ft.":"m") + "\n";
            } catch (RequestedValueOOBException | CorruptTerrainException e){
                attribs += getString(R.string.dem_load_error_generic_msg);
            }

            attribs += getString(R.string.attribute_text_drone_azimuth) + " " + Math.round(degNormalize(azimuth)) + "°\n";
            attribs += getString(R.string.attribute_text_drone_camera_pitch) + " " + -1 * Math.round(theta) + "°\n";
            appendText(attribs);
            attribs = "";
            double[] result;
            double distance;
            double latitude;
            double longitude;
            double altitudeDouble;
            long altitude;

            double latCK42;
            double lonCK42;
            long altCK42;

            long GK_northing;
            long GK_easting;

            double predictedCE;
            CursorOnTargetSender.TLE_Categories TLE_Cat;

            if (theTGetter != null) {
                try {
                    result = theTGetter.resolveTarget(y, x, z, azimuth, theta);
                    distance = result[0];
                    latitude = result[1];
                    longitude = result[2];
                    altitudeDouble = result[3];

                    if (IS_EXTENDED_COT_MODE_ACTIVE) openAthenaCalculationInfo.put("slantRange", roundDouble(distance));

                    latCK42 = CoordTranslator.toCK42Lat(latitude, longitude, altitudeDouble);
                    lonCK42 = CoordTranslator.toCK42Lon(latitude, longitude, altitudeDouble);
                    // Note: This altitude calculation assumes the SK42 and WGS84 ellipsoid have the exact same center
                    //     This is not totally correct, but in practice is close enough to the actual value
                    //     See: https://gis.stackexchange.com/a/88499
                    altCK42 = Math.round(altitudeDouble - CoordTranslator.fromCK42Alt(latCK42, lonCK42, 0.0d));

                    long[] GK_conversion_results = CoordTranslator.fromCK42toCK42_GK(latCK42, lonCK42);
                    GK_northing = GK_conversion_results[0];
                    GK_easting = GK_conversion_results[1];

                    altitude = Math.round(result[3]);
                    if (!outputModeIsSlavic()) {
                        // Imperial distance units are optionally available for outputs in western coordinate systems
                        attribs += getString(R.string.target_found_at_msg) + ": " + roundDouble(latitude) + "," + roundDouble(longitude) + "\n" + getString(R.string.altitude_label_short) + " " + Math.round(altitude * (isUnitFoot() ? AthenaApp.FEET_PER_METER : 1.0d)) + " " + (isUnitFoot() ? "ft.":"m") + "\n";
                    } else {
                        // Imperial distance units are disabled for CK42 output modes
                        attribs += getString(R.string.target_found_at_msg) + " (WGS84): " + roundDouble(latitude) + "," + roundDouble(longitude) + " " + getString(R.string.altitude_label_short) + " " + altitude + " " + "m" + "\n";
                        attribs += getString(R.string.target_found_at_msg) + " (CK-42): " + roundDouble(latCK42) + "," + roundDouble(lonCK42) + " Alt (hae): " + altCK42 + " " + "m" + "\n";
                    }
                    attribs += getString(R.string.drone_dist_to_target_msg) + " " + Math.round(distance * (isUnitFoot() ? AthenaApp.FEET_PER_METER : 1.0d)) + " " + (isUnitFoot() ? "ft.":"m") + "\n";
                    predictedCE = CursorOnTargetSender.calculateCircularError(theta);
                    attribs += getString(R.string.target_predicted_ce) + " " + Math.round((predictedCE * (isUnitFoot() ? AthenaApp.FEET_PER_METER : 1.0d))*10.0)/10.0 + " " + (isUnitFoot() ? "ft.":"m") + "\n";
                    TLE_Cat = CursorOnTargetSender.errorCategoryFromCE(predictedCE);
                    attribs += getString(R.string.target_location_error_category) + " " + TLE_Cat.name() + "\n";
                    if (shouldISendCoT) {
                        attribs += getString(R.string.mainactivity_label_cursor_on_target_message_sent_with_uid) + " " + CursorOnTargetSender.buildUIDString(this) + "\n";
                    }
                    if (!outputModeIsSlavic()) { // to avoid confusion with WGS84, no Maps link is provided when outputModeIsSlavic()

                        attribs += "<a href=\"geo:" + roundDouble(latitude) + "," + roundDouble(longitude);
                        // https://en.wikipedia.org/wiki/Geo_URI_scheme#Uncertainty
                        attribs += ";u=" + roundDouble(predictedCE);
                        // Google Maps requires a ?q= tag to actually display a pin for the indicated location
                        // https://en.wikipedia.org/wiki/Geo_URI_scheme#Unofficial_extensions
                        attribs += "?q=" + roundDouble(latitude) + "," + roundDouble(longitude) + "\">";
                        attribs += roundDouble(latitude) + "," + roundDouble(longitude);
                        // https://en.wikipedia.org/wiki/Geo_URI_scheme#Uncertainty
                        attribs += "</a>\n\n";
                    }
                } catch (RequestedValueOOBException e) {
                    if (e.isAltitudeDataBad) {
                        Log.e(TAG, e.getMessage());
                        attribs += getString(R.string.bad_altitude_data_error_msg) + "\n";
                        appendText(attribs);
                        return;
                    } else {
                        double droneToOobDistance = Math.round(haversine(x,y,e.OOBLon,e.OOBLat,z) * (isUnitFoot() ? AthenaApp.FEET_PER_METER : 1.0d));
                        Log.e(TAG, "ERROR: resolveTarget ran OOB at (WGS84): " + roundDouble(e.OOBLat) + ", " + roundDouble(e.OOBLon));
                        if (!outputModeIsSlavic()) {
                            attribs += getString(R.string.resolveTarget_oob_error_msg) + ": " + roundDouble(e.OOBLat) + ", " + roundDouble(e.OOBLon) + "\n";
                            attribs += getString(R.string.distance_label) + " " + droneToOobDistance + (isUnitFoot() ? "ft.":"m")  + "\n";
                        } else {
                            attribs += getString(R.string.resolveTarget_oob_error_msg) + " (CK-42):" +roundDouble(CoordTranslator.toCK42Lat(e.OOBLat, e.OOBLon, z)) + ", " + roundDouble(CoordTranslator.toCK42Lon(e.OOBLat, e.OOBLon, z)) + "\n";
                            // NOTE: OpenAthena forces meters distance unit when in CK42 output mode
                            attribs += getString(R.string.distance_label) + " " + droneToOobDistance + (isUnitFoot() ? "ft.":"m") + "\n";
                        }
                        attribs += getString(R.string.geotiff_coverage_reminder) + "\n";
                        attribs += getString(R.string.geotiff_coverage_precedent_message) + "\n";
                        appendText(attribs);
                        printDEMBounds();

                        // new DEM diameter is meters distance from drone location to out of bounds location
                        //     diameter is multiplied by 1.5 for margin of safety
                        double newDEMDiameter = haversine(x,y,e.OOBLon,e.OOBLat,z) * 1.5d;
                        if (newDEMDiameter > AthenaApp.DEM_DOWNLOAD_RETRY_MAX_METERS_DIAMETER) {
                            appendText(getString(R.string.error_mainactivity_calculation_oob) + " " + getString(R.string.prompt_use_blah) + getString(R.string.action_demcache) + getString(R.string.to_import_an_offline_dem_file_manually) +  "\n");
                        } else {
                            if (newDEMDiameter < AthenaApp.DEM_DOWNLOAD_DEFAULT_METERS_DIAMETER) {
                                newDEMDiameter = AthenaApp.DEM_DOWNLOAD_DEFAULT_METERS_DIAMETER;
                            }
                            appendText( getString(R.string.mainactivity_status_downloading_a_new_dem_with_increased_coverage_area) + "..." + "\n" + getString(R.string.mainactivity_warning_dem_oob_retry) + "\n");
                            appendText(getString(R.string.mainactivity_label_new_dem_center_coordinates) + " " + roundDouble(e.OOBLat) + "," + roundDouble(e.OOBLon) + " " + getString(R.string.mainactivity_new_dem_diameter_label) + " " + Math.round(newDEMDiameter * (isUnitFoot() ? AthenaApp.FEET_PER_METER : 1.0d)) + (isUnitFoot() ? "ft.":"m") + "\n");
                            displayNewDemDownloadChoice((e.OOBLat + y) / 2.0d, (e.OOBLon + x) / 2.0d, newDEMDiameter);
                        }

                        return;
                    }
                }
            } else {
                attribs += getString(R.string.geotiff_load_reminder_msg);
                appendText(attribs);
                return;
            }
            attribs = attribs.replaceAll("(\r\n|\n)", "<br>"); // replace newline with HTML equivalent
            textView.append(Html.fromHtml(attribs, 0, null, null));
            String targetCoordString;
            if (!outputModeIsSlavic()) {
                // open link portion of href tag
                targetCoordString = "<a href=\"geo:";
                targetCoordString += roundDouble(latitude) + "," + roundDouble(longitude); // just use normal WGS84 for URI, regardless of current outputMode
                // https://en.wikipedia.org/wiki/Geo_URI_scheme#Uncertainty
                targetCoordString += ";u=" + roundDouble(predictedCE);
                // Google Maps requires a ?q= tag to actually display a pin for the indicated location
                // https://en.wikipedia.org/wiki/Geo_URI_scheme#Unofficial_extensions
                targetCoordString += "?q=" + roundDouble(latitude) + "," + roundDouble(longitude);
                targetCoordString += "\">"; // close link portion of href tag

                // start building display text portion of href tag
                targetCoordString += CoordTranslator.toSelectedOutputMode(latitude,longitude,outputMode);
                targetCoordString += "</a> "; // end href tag

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    // add a newline between coordinate and elevation if device is in landscape
                    targetCoordString += "<br>";
                }

                // convert from WGS84 height above ellipsoid to EGM96 above mean sea level (much more commonly used)
                double mslAlt = altitudeDouble + offsetAdapter.getEGM96OffsetAtLatLon(latitude, longitude);
                // convert from meters to feet if user setting indicates to do so
                mslAlt *= (isUnitFoot() ? AthenaApp.FEET_PER_METER : 1.0d);
                // round to nearest whole number
                long altEGM96 = Math.round(mslAlt);
                targetCoordString += getString(R.string.altitude_label_short) + " " + altEGM96 + " " + (isUnitFoot() ? "ft.":"m") + " ";

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    // add a newline between elevation and TLE category if device is in Portrait
                    targetCoordString += "<br>";
                }

                // Choose color Green, Yellow, Red for TLE 1, 2, 3
                // For TLE 4+, htmlColorFromTLE_Category will be empty, leaving font at default color
                targetCoordString += " " + "<font color=\"" + CursorOnTargetSender.htmlColorFromTLE_Category(TLE_Cat) + "\">";
                // Add Target Location Error Category (e.g. TLE_1, TLE_2, TLE_3, etc.) to output
                targetCoordString += TLE_Cat.name();
                // end colored text
                targetCoordString += "</font>";
            } else /* outputModeIsSlavic */ { // to avoid confusion with WGS84, no Maps link is provided when outputModeIsSlavic()
                if (outputMode == outputModes.CK42Geodetic) {
                    targetCoordString = "(CK-42) " + roundDouble(latCK42) + ", " + roundDouble(lonCK42);
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        targetCoordString += "<br>";
                    } else {
                        targetCoordString += " ";
                    }
                    targetCoordString += "Alt: " + altCK42 + "m";
                } else if (outputMode == outputModes.CK42GaussKrüger) {
                    String northing_string = CoordTranslator.makeGKHumanReadable(GK_northing);
                    String easting_string = CoordTranslator.makeGKHumanReadable(GK_easting);
                    // Note that for CK-42, height above ellipsoid is used rather than above mean sea level
                    targetCoordString = "(CK-42) [Gauss-Krüger] " + "<br>" + getString(R.string.gk_northing_text) + " " + northing_string + "<br>" + getString(R.string.gk_easting_text) + " " + easting_string + "<br>" + getString(R.string.altitude_label_short) + " " + altCK42 + "m\n";
                } else {
                    throw new RuntimeException("Program entered an inoperable state due to outputMode"); // this shouldn't ever happen
                }
            }
            textViewTargetCoord.setText(Html.fromHtml(targetCoordString, 0, null, null));
            isTargetCoordDisplayed = true;
            if (!MetadataExtractor.isDroneModelRecognized(exif)) {
                displayMissingCameraIntrinsicsAlert();
            }
            // close file
            is.close();
            //
            // send CoT message to udp://239.2.3.1:6969
            //     e.g. for use with ATAK app
            if (shouldISendCoT) {
                // NOTE that the CoT spec requires WGS84 hae, not EGM96 above mean sea level
                CursorOnTargetSender.sendCoT(this, latitude, longitude, altitudeDouble, theta, exif.getAttribute(ExifInterface.TAG_DATETIME), openAthenaCalculationInfo);
            }
        } catch (XMPException e) {
            Log.e(TAG, e.getMessage());
            appendText(getString(R.string.metadata_parse_error_msg) + "\n");
            e.printStackTrace();
        } catch (MissingDataException e) {
            Log.e(TAG, e.getMessage());
            appendText(e.getMessage() + "\n");
            e.getStackTrace();
        } catch (CorruptTerrainException cte) {
            Log.e(TAG, cte.getMessage());
            printDEMBounds();
        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
            appendText(getString(R.string.metadata_parse_error_msg)+"\n\n");
            e.printStackTrace();
        }
    } // button click

    protected void postResults(double lat, double lon, String resultStr) {
        postResults(resultStr);
        DemCache.DemCacheEntry dce = athenaApp.demCache.searchCacheEntry(lat, lon);
        if (dce != null) {
            appendText(getString(R.string.main_activity_status_starting_auto_load_for_downloaded_dem_file) + "\n");
            demSelected(dce.fileUri);
        }
    }
    @Override
    protected void postResults(String resultStr) {
        appendText(resultStr + "\n");
        athenaApp.demCache.refreshCache();
    }

    @Override
    protected void downloadNewDEM(double lat, double lon, double meters_diameter) {
        DemDownloader aDownloader = new DemDownloader(getApplicationContext(),lat,lon,meters_diameter);
        aDownloader.asyncDownload(new Consumer<String>() {
            @Override
            public void accept(String s) {
                Log.d(TAG,"NewDemActivity download returned "+s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        decrementProgressBar();
                        postResults(lat, lon, s);
                        Toast t = Toast.makeText(MainActivity.this,s,Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER,0,0);
                        t.show();
                    }
                });
            }
        });
    }

    private void printDEMBounds() {
        String attribs = "";
        if (!outputModeIsSlavic()) {
            attribs += roundDouble(theParser.getMinLat()) + " ≤ " + getString(R.string.latitude_label_short) + " ≤ " + roundDouble(theParser.getMaxLat()) + "\n";
            attribs += roundDouble(theParser.getMinLon()) + " ≤ " + getString(R.string.longitude_label_short) + " ≤ " + roundDouble(theParser.getMaxLon()) + "\n\n";
        } else {
            try {
                // Believe me, I don't like this either....
                attribs += roundDouble(CoordTranslator.toCK42Lat(theParser.getMinLat(), theParser.getMinLon(), theParser.getAltFromLatLon(theParser.getMinLat(), theParser.getMinLon()))) + " ≤ " + getString(R.string.latitude_label_short) + " " + "(CK-42)" + " ≤ " + roundDouble(CoordTranslator.toCK42Lat(theParser.getMaxLat(), theParser.getMaxLon(), theParser.getAltFromLatLon(theParser.getMaxLat(), theParser.getMaxLon()))) + "\n";
                attribs += roundDouble(CoordTranslator.toCK42Lon(theParser.getMinLat(), theParser.getMinLon(), theParser.getAltFromLatLon(theParser.getMinLat(), theParser.getMinLon()))) + " ≤ " + getString(R.string.longitude_label_short) + " " + "(CK-42)" + " ≤ " + roundDouble(CoordTranslator.toCK42Lon(theParser.getMaxLat(), theParser.getMaxLon(), theParser.getAltFromLatLon(theParser.getMaxLat(), theParser.getMaxLon()))) + "\n\n";
            } catch (RequestedValueOOBException e_OOB) { // This shouldn't happen, may be possible though if GeoTIFF file is very small
                // revert to WGS84 if CK-42 conversion has failed
                attribs += getString(R.string.wgs84_ck42_conversion_fail_warning) + "\n";
                attribs += roundDouble(theParser.getMinLat()) + " ≤ " + getString(R.string.latitude_label_short) + " ≤ " + roundDouble(theParser.getMaxLat()) + "\n";
                attribs += roundDouble(theParser.getMinLon()) + " ≤ " + getString(R.string.longitude_label_short) + " ≤ " + roundDouble(theParser.getMaxLon()) + "\n\n";
            } catch (CorruptTerrainException cte) {
                attribs += getString(R.string.error_mainactivity_dted_elevation_model_file_is_corrupt_and_unusable);
            }
        }
        appendText(attribs);
    }

    public void displayAutelAlert() {
        if (dangerousAutelAwarenessCount < 1) { // suppress warning if already encountered by user in this session
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.autel_accuracy_warning_msg);
            builder.setPositiveButton(R.string.i_understand_this_risk, (DialogInterface.OnClickListener) (dialog, which) -> {
                dangerousAutelAwarenessCount += 1;
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    // Overloaded function call
    public void displayNewDemDownloadChoice(double lat, double lon) {
        displayNewDemDownloadChoice(lat,lon,AthenaApp.DEM_DOWNLOAD_DEFAULT_METERS_DIAMETER);
    }

    public void displayNewDemDownloadChoice(double lat, double lon, double diameter) {
        if (showProgressBarSemaphore > 0) {
            // Do not display a new download prompt if a DEM download or load operation is still in progress
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(getString(R.string.dialog_mainactivity_would_you_like_to_download_a_new_dem)+ CoordTranslator.toSelectedOutputMode(lat,lon,outputMode) + " ?");
        builder.setPositiveButton(getString(R.string.yes), (DialogInterface.OnClickListener) (dialog, which) -> {
            incrementAndShowProgressBar();
            downloadNewDEM(lat, lon, diameter);
        });
        builder.setNegativeButton(getString(R.string.no), (DialogInterface.OnClickListener) (dialog, which) -> {
            appendText(getString(R.string.mainactivity_status_user_declined_dem_download) + "\n");
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void displayMissingCameraIntrinsicsAlert() {
        if (dangerousMissingCameraIntrinsicsCount < 1 || !MetadataExtractor.parameterProvider.isDroneArrayValid()) { // suppress warning if already encountered by user in this session
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                             // TODO move this to values/strings.xml
            builder.setMessage("⚠\uFE0F " + getString(R.string.missing_camera_intrinsics_warning_message) + " ⚠\uFE0F");
            builder.setPositiveButton(R.string.i_understand_this_risk, (DialogInterface.OnClickListener) (dialog, which) -> {
                dangerousMissingCameraIntrinsicsCount += 1;
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void copyTargetCoordText(View view) {
        if (isTargetCoordDisplayed) {
            String text = textViewTargetCoord.getText().toString();
            Log.d(TAG, "clipboard text: " + text);
            text = text.replaceAll("<[^>]*>", ""); // remove HTML link tag(s)
            // don't remove newline characters for copy/paste text if output mode is CK42 GK gridref
            if (outputMode != outputModes.CK42GaussKrüger) {
                text = text.replaceAll("<br>", ""); // remove HTML <br> newlines
                text = text.replaceAll("(\r\n|\n)", ""); // remove string literal newlines
            }
            // Copy the text to the clipboard
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Text", text);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, getString(R.string.text_copied_to_clipboard_msg), Toast.LENGTH_SHORT).show();
        }
    }

    // select image button clicked; launch chooser and get result
    // in callback
    public void selectImage(View view)
    {
        Log.d(TAG,"selectImageClick started");
        Log.d(TAG,"READ_EXTERNAL_STORAGE: " + Integer.toString(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)));

        requestExternStorage();

        mGetContent.launch("image/*");

    }

//    public void selectDEM(View view)
//    {
//        Log.d(TAG,"selectDEM started");
////        appendLog("Going to start selecting GeoTIFF\n");
//
//        Log.d(TAG,"READ_EXTERNAL_STORAGE: " + Integer.toString(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)));
//
//        requestExternStorage();
//
//        mGetDEM.launch("*/*");
//
//    }

    private void requestExternStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.READ_MEDIA_IMAGES}, requestNo);
            }
            requestNo++;
        } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                Log.d(TAG, "Attempting to Obtain unobtained permission READ_EXTERNAL_STORAGE");
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, requestNo);
                requestNo++;
            }
        }
    }

    private static String roundDouble(double d) {
        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
        decimalSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.######", decimalSymbols);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(d);
    }

    private void appendText(final String aStr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(aStr);

                if(isImageLoaded) {
                    // Additional code to scroll to the bottom
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            // Scroll to the bottom of the ScrollView
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
            }
        });
    } // appendText to textView but do so on UI thread


    // reset the text field
    private void clearText()
    {
        runOnUiThread(new Runnable() {
           @Override
           public void run() {
               String placeholderText = getString(R.string.openathena_for_android) +  " " + getString(R.string.version_word) + " " + versionName + "\n\n";
               placeholderText += getString(R.string.step_1_load_drone_image) + " \n";
               placeholderText += getString(R.string.step_2_press_calculate) + " \n";
               placeholderText += getString(R.string.step_3_obtain_target) + " \uD83C\uDFAF\n\n";
               textView.setText(placeholderText);
           }
        });

    }

}
