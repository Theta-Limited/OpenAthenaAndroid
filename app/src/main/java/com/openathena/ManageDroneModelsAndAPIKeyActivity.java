package com.openathena;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.net.Uri;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ManageDroneModelsAndAPIKeyActivity extends AthenaActivity{

//    private Handler mainHandler = new Handler(Looper.getMainLooper());

    // Member variables for TextViews
    private TextView titleTextView1;
    private TextView textViewDemApiKeyStatus;
    private TextView textViewUserInformationWhatIsApiKeyFor;
    private TextView textViewLinkObtainApiKey;
    private TextView titleTextView2;
    private TextView textViewDroneModelsJsonStatus;
    private TextView textViewDroneModelsLastUpdated;
    private TextView textViewDroneModelsNumEntries;
    private TextView droneModelsAndApiKeyResults;

    // Member variable for EditText
    private EditText apiKeyEditText;

    // Member variables for Buttons
    private Button applyNewDemApiKeyButton;
    private Button resetApiKeyButton;
    private Button loadNewDroneModelsJsonButton;
    private Button resetDroneModelsButton;

    private ActivityResultLauncher<String> mGetContentDroneModels;

//    // Member variable for Views (separator lines)
//    private View viewSep1;
//    private View viewSep2;

    protected enum ApiKeyStatus {
        VALID,
        UNKNOWN,
        INVALID
    }

    protected ApiKeyStatus apiKeyStatus;
    protected boolean isDroneModelsDatabaseValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_drone_models_and_api_key);

        // Initialize TextViews
        titleTextView1 = findViewById(R.id.titleTextView1);
        textViewDemApiKeyStatus = findViewById(R.id.textView_dem_api_key_status);
        textViewUserInformationWhatIsApiKeyFor = findViewById(R.id.textView_user_information_what_is_api_key_for);
        textViewLinkObtainApiKey = findViewById(R.id.textView_link_obtain_api_key);
        titleTextView2 = findViewById(R.id.titleTextView2);
        textViewDroneModelsJsonStatus = findViewById(R.id.textView_dronemodels_json_status);
        textViewDroneModelsLastUpdated = findViewById(R.id.textView_dronemodels_last_updated);
        textViewDroneModelsNumEntries = findViewById(R.id.textView_dronemodels_num_entries);
        droneModelsAndApiKeyResults = findViewById(R.id.drone_models_and_api_key_results);

        // Initialize EditText
        apiKeyEditText = findViewById(R.id.api_key_edittext);

        // Initialize Buttons
        applyNewDemApiKeyButton = findViewById(R.id.apply_new_dem_API_key_button);
        resetApiKeyButton = findViewById(R.id.reset_API_key_button);
        loadNewDroneModelsJsonButton = findViewById(R.id.load_new_dronemodels_json_button);
        resetDroneModelsButton = findViewById(R.id.reset_dronemodels_button);

//        // Initialize Views
//        viewSep1 = findViewById(R.id.view_sep_1);
//        viewSep2 = findViewById(R.id.view_sep_2);

        textViewLinkObtainApiKey.setText(Html.fromHtml(getString(R.string.href_obtain_an_api_key_here), Html.FROM_HTML_MODE_COMPACT));
        textViewLinkObtainApiKey.setMovementMethod(LinkMovementMethod.getInstance());

        // Initialize the ActivityResultLauncher
        mGetContentDroneModels = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Handle the returned Uri (path to the selected file)
                        jsonFileSelected(uri);
                    }
                });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();

        String storedJsonUriString = sharedPreferences.getString("droneModelsJsonUri",null);
        if (storedJsonUriString != null) {
            droneModelsJsonUri = Uri.parse(storedJsonUriString);
        } else {
            //TODO stuff here
        }

        // Hide the Description of why a DEM API Key is needed by default (will be un-hidden if Key is later found to be invalid)
        hideAPIKeyPurposeDescription();

        testAPIKeyAndSetApiKeyStatus();
        testDroneModelsAndSetDroneModelsStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        testAPIKeyAndSetApiKeyStatus();
    }

    protected void testAPIKeyAndSetApiKeyStatus() {
        // demDownloader to be used for API Key validity check
        // lat lon parmaters in input are ignored
        DemDownloader demDownloader = new DemDownloader(this, 0.0, 0.0, 10);


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (demDownloader.isApiKeyValid()) {
                        apiKeyStatus = ApiKeyStatus.VALID;
                    } else {
                        apiKeyStatus = ApiKeyStatus.INVALID;
                    }
                } catch (IOException e) {
                    // IOException indicates no internet connection available
                    apiKeyStatus = ApiKeyStatus.UNKNOWN;
                }
                updateTextViewDemApiKeyStatus();

            }
        }).start();
    }

    protected void updateTextViewDemApiKeyStatus() {
        String newText = getString(R.string.dem_api_key_status) + " ";
        if (apiKeyStatus == ApiKeyStatus.VALID) {
            newText += " ✅" + " (" + getString(R.string.status_valid) + ")";
        } else if (apiKeyStatus == ApiKeyStatus.UNKNOWN) {
            newText += " ❓" + " (" + getString(R.string.status_unknown) + ")";
            showAPIKeyPurposeDescription();
        } else {
            newText += " ❌" + " (" + getString(R.string.status_invalid) + ")";
            showAPIKeyPurposeDescription();
        }
        final String outText = newText;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewDemApiKeyStatus.setText(outText);
            }
        });
    }

    protected void showAPIKeyPurposeDescription() {
        textViewUserInformationWhatIsApiKeyFor.setVisibility(View.VISIBLE);
        textViewLinkObtainApiKey.setVisibility(View.VISIBLE);
    }

    protected void hideAPIKeyPurposeDescription() {
        textViewUserInformationWhatIsApiKeyFor.setVisibility(View.GONE);
        textViewLinkObtainApiKey.setVisibility(View.GONE);
    }

    protected void testDroneModelsAndSetDroneModelsStatus() {
        DroneParametersFromJSON droneModelsParser = new DroneParametersFromJSON(getApplicationContext());
        isDroneModelsDatabaseValid = droneModelsParser.isDroneArrayValid();
        updateTextViewDroneModelsJsonStatus();
        textViewDroneModelsLastUpdated.setText(getString(R.string.dronemodels_last_updated) + " " + droneModelsParser.getLastUpdatedField());
        textViewDroneModelsNumEntries.setText(getString(R.string.dronemodels_number_of_entries) + " " + droneModelsParser.getDroneArrayLength());
    }

    protected void updateTextViewDroneModelsJsonStatus () {
        if (isDroneModelsDatabaseValid) {
            textViewDroneModelsJsonStatus.setText(getString(R.string.local_database_status) + " ✅" + " (" + getString(R.string.status_valid) + ")");
        } else {
            textViewDroneModelsJsonStatus.setText(getString(R.string.local_database_status) + " ❌" + " (" + getString(R.string.status_invalid) + ")");
        }
    }

    // Logic for handling Apply New DEM API Key button click
    public void handleApplyNewDemApiKey(View view) {
        boolean wasNewApiKeyAppliedSuccesfully = putDemApiKey(apiKeyEditText.getText().toString());
        if (!wasNewApiKeyAppliedSuccesfully) {
            String errStr = getString(R.string.error_dem_api_key_text_not_valid);
            Toast.makeText(this,errStr,Toast.LENGTH_LONG).show();
            droneModelsAndApiKeyResults.setText(errStr);
            return;
        }

        String successStr = getString(R.string.new_api_key_applied);
        droneModelsAndApiKeyResults.setText(successStr);
        Toast.makeText(this,successStr,Toast.LENGTH_SHORT).show();

        // test the user's new API key and update thew status indicator
        testAPIKeyAndSetApiKeyStatus();
    }

    // Logic for handling Rest API Key button click
    public void handleResetDemApiKey(View view) {
        // NOTE: if OPENTOPOGRAPHY_API_KEY is missing from build local.properties, this will default to an empty String!
        resetDemApiKey();

        apiKeyEditText.setText("");
        //apiKeyEditText.setHint(getString(R.string.api_key_gibberish));

        String successStr = "API Key Reset to Default";
        droneModelsAndApiKeyResults.setText(successStr);
        Toast.makeText(this,successStr,Toast.LENGTH_SHORT).show();

        testAPIKeyAndSetApiKeyStatus();
    }

    // Logic for handling Load New droneModels.json button click
    public void handleLoadNewDroneModelsJson(View view) {
        mGetContentDroneModels.launch("application/json");
    }

    public void handleResetDroneModels(View view) {
        DroneParametersFromJSON droneModelsParser = new DroneParametersFromJSON(getApplicationContext());
        droneModelsParser.loadJSONFromAsset();
        String successStr = "droneModels JSON reset to default";
        droneModelsAndApiKeyResults.setText(successStr);
        Toast.makeText(this,successStr,Toast.LENGTH_SHORT).show();

        testDroneModelsAndSetDroneModelsStatus();
    }

    private void jsonFileSelected(Uri uri) {
        if (uri == null) {
            return;
        }

        File appCacheDroneModelsDir = new File(getCacheDir(), "droneModels");
        if (!appCacheDroneModelsDir.exists()) {
            appCacheDroneModelsDir.mkdirs();
        }

        ContentResolver cr = getContentResolver();
        InputStream is;

        // Android 10/11, we can't access this file directly
        // We will copy the file into app's own package cache
        String fileName = getFileName(uri);
        File fileInCache = new File(appCacheDroneModelsDir, fileName);
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
                    Log.e(TAG, "FileNotFound jsonFileSelected(): " + e.getMessage());
                    throw e;
                } catch (IOException e) {
                    Log.e(TAG, "IOException jsonFileSelected():" + e.getMessage());
                    throw e;
                } catch (NullPointerException e) {
                    Log.e(TAG, "NullPointerException jsonFileSelected():" + e.getMessage());
                    throw e;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            uri = Uri.fromFile(fileInCache); // use the uri of the copy in the cache directory
        }

        DroneParametersFromJSON droneModelsParser = new DroneParametersFromJSON(getApplicationContext());
        try {
            droneModelsParser.loadJSONFromUri(uri);
        } catch (IOException | JSONException e) {
            String offendingObjectMakeModel = null;
            if (e instanceof  DetailedJSONException) {
                Log.e(TAG, "Encountered a DetailedJSONException");
                offendingObjectMakeModel = ((DetailedJSONException) e).getOffendingObjectMakeModel();
            }

            if (offendingObjectMakeModel == null) {
                Log.e(TAG, "DetailedJSONException offendingObjectMakeModel is null");
                offendingObjectMakeModel = "";
            } else {
                Log.e(TAG, "DetailedJSONException offendingObjectMakeModel is " + offendingObjectMakeModel);
                offendingObjectMakeModel = " " + getString(R.string.in_drone_model) + " " + offendingObjectMakeModel;
            }

            droneModelsAndApiKeyResults.setText(getString(R.string.error_nondescript)+": " + e.getMessage() + offendingObjectMakeModel);
            Toast.makeText(this,getString(R.string.error_nondescript) + ": " + "droneModels JSON was invalid!",Toast.LENGTH_LONG).show();
            testDroneModelsAndSetDroneModelsStatus();
            return;
        }

        droneModelsJsonUri = uri;
        testDroneModelsAndSetDroneModelsStatus();

        droneModelsAndApiKeyResults.setText("DroneModels updated successfully");

        // save path of new droneModels.json to persistent settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString("droneModelsJsonUri", droneModelsJsonUri.toString());
    } // jsonFileSelected

    @Override
    public void calculateImage(View view) {
        assert(true);
    }

    @Override
    public void calculateImage(View view, boolean shouldISendCoT) {
        assert(true);
    }

    @Override
    protected void saveStateToSingleton() {
        //athenaApp.putString("droneModelsJsonUri", droneModelsJsonUri.toString());
    }
}
