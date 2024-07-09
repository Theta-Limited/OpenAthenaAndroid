package com.openathena;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ManageDroneModelsAndAPIKeyActivity extends AthenaActivity{

    // Member variables for TextViews
    private TextView titleTextView1;
    private TextView textViewDemApiKeyStatus;
    private TextView textViewUserInformationWhatIsApiKeyFor;
    private TextView textViewLinkObtainApiKey;
    private TextView titleTextView2;
    private TextView textViewDroneModelsJsonStatus;
    private TextView textViewDroneModelsLastUpdated;
    private TextView textViewDroneModelsNumEntries;
    private TextView newDemResults;

    // Member variable for EditText
    private EditText lookupLatLonText;

    // Member variables for Buttons
    private Button applyNewDemApiKeyButton;
    private Button loadNewDroneModelsJsonButton;
    private Button resetPrefsButton;

//    // Member variable for Views (separator lines)
//    private View viewSep1;
//    private View viewSep2;

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
        newDemResults = findViewById(R.id.new_dem_results);

        // Initialize EditText
        lookupLatLonText = findViewById(R.id.lookup_latlon_text);

        // Initialize Buttons
        applyNewDemApiKeyButton = findViewById(R.id.apply_new_dem_API_key_button);
        loadNewDroneModelsJsonButton = findViewById(R.id.load_new_dronemodels_json_button);
        resetPrefsButton = findViewById(R.id.reset_prefs_button);

//        // Initialize Views
//        viewSep1 = findViewById(R.id.view_sep_1);
//        viewSep2 = findViewById(R.id.view_sep_2);

        textViewLinkObtainApiKey.setText(Html.fromHtml(getString(R.string.href_obtain_an_api_key_here), Html.FROM_HTML_MODE_COMPACT));
        textViewLinkObtainApiKey.setMovementMethod(LinkMovementMethod.getInstance());

    }

    public void handleApplyNewDemApiKey(View view) {
        // Logic for handling Apply New DEM API Key button click
    }

    public void handleLoadNewDroneModelsJson(View view) {
        // Logic for handling Load New droneModels.json button click
    }

    public void handleResetPrefs(View view) {
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
