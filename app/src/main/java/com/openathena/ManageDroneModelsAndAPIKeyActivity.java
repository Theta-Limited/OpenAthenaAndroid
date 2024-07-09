package com.openathena;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

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
    private EditText lookupLatLonText;

    // Member variables for Buttons
    private Button applyNewDemApiKeyButton;
    private Button resetApiKeyButton;
    private Button loadNewDroneModelsJsonButton;
    private Button resetDroneModelsButton;

    protected ApiKeyStatus apiKeyStatus;

//    // Member variable for Views (separator lines)
//    private View viewSep1;
//    private View viewSep2;

    protected enum ApiKeyStatus {
        VALID,
        UNKNOWN,
        INVALID
    }

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
        lookupLatLonText = findViewById(R.id.lookup_latlon_text);

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

        tesAPIKeyAndSetApiKeyStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tesAPIKeyAndSetApiKeyStatus();
    }

    protected void tesAPIKeyAndSetApiKeyStatus() {
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

//        try {
//            if (demDownloader.isApiKeyValid()) {
//                apiKeyStatus = ApiKeyStatus.VALID;
//            } else {
//                apiKeyStatus = ApiKeyStatus.INVALID;
//            }
//        } catch (IOException ioe) {
//            // IOException indicates no internet connection available
//            apiKeyStatus = ApiKeyStatus.UNKNOWN;
//        }
//        updateTextViewDemApiKeyStatus();
    }

    protected void updateTextViewDemApiKeyStatus() {
        String newText = getString(R.string.dem_api_key_status) + " ";
        if (apiKeyStatus == ApiKeyStatus.VALID) {
            newText += "✅" + " (" + getString(R.string.status_valid) + ")";
        } else if (apiKeyStatus == ApiKeyStatus.UNKNOWN) {
            newText += "❓" + " (" + getString(R.string.status_unknown) + ")";
        } else {
            newText += "❌" + " (" + getString(R.string.status_invalid) + ")";
        }
        final String outText = newText;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewDemApiKeyStatus.setText(outText);
            }
        });
    }

    // Logic for handling Apply New DEM API Key button click
    public void handleApplyNewDemApiKey(View view) {
        boolean wasNewApiKeyAppliedSuccesfully = putDemApiKey(lookupLatLonText.getText().toString());
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
        tesAPIKeyAndSetApiKeyStatus();
    }

    // Logic for handling Rest API Key button click
    public void handleResetDemApiKey(View view) {
        // NOTE: if OPENTOPOGRAPHY_API_KEY is missing from build local.properties, this will default to an empty String!
        resetDemApiKey();

        tesAPIKeyAndSetApiKeyStatus();
    }

    public void handleLoadNewDroneModelsJson(View view) {
        // Logic for handling Load New droneModels.json button click
    }

    public void handleResetDroneModels(View view) {
        // Logic for handling Reset Preferences button click
    }

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
        assert(true);
    }
}
