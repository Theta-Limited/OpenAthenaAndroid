// AboutActivity.java
// OpenAthena for Android
// Bobby Krupczak, Matthew Krupczak, et. al

package com.openathena;

import android.content.res.Configuration;
import android.os.Bundle;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;

public class AboutActivity extends AthenaActivity {

    public static String TAG = AboutActivity.class.getSimpleName();
    ImageView athenaIcon;
    TextView aboutText;
    String versionName;

    /** CoT DEBUG mode Easter-egg settings */
    private static final int REQUIRED_TAPS      = 5;      // how many taps
    private static final long TAP_WINDOW_MS     = 3_000;  // max time span for them (3 s)
    private int   tapCount          = 0;
    private long  firstTapTimestamp = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG,"onCreate started");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        athenaIcon = (ImageView) findViewById(R.id.aboutImageView);
        athenaIcon.setOnClickListener(v -> {
            long now = System.currentTimeMillis();

            // Reset counter if first tap was too long ago
            if (now - firstTapTimestamp > TAP_WINDOW_MS) {
                tapCount = 0;
                firstTapTimestamp = now;
            }

            tapCount++;

            if (tapCount == 1) {
                firstTapTimestamp = now;   // mark start of the burst
            }

            if (tapCount >= REQUIRED_TAPS) {
                AthenaActivity.toggleExtendedCotMode();
                boolean on = AthenaActivity.is_extended_cot_mode_active;

                // 2-s toast for final confirmation
                Toast.makeText(
                        this,
                        on ? "Extended CoT mode enabled!" : "Extended CoT mode disabled.",
                        Toast.LENGTH_SHORT).show();

                tapCount = 0;
                firstTapTimestamp = 0L;
            } else {
                int remaining = REQUIRED_TAPS - tapCount;

                if (remaining < Math.max(REQUIRED_TAPS - 2, 2)) {
                    Snackbar foo = Snackbar.make(
                            v,                                            // attach to tapped ImageView
                            remaining + " more tap" + (remaining == 1 ? "" : "s"),
                            400                                           // duration in ms
                    );
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        foo.setAnchorView(R.id.aboutScrollView);
                    } else {
                        foo.setAnchorView(R.id.aboutImageView);
                    }
                    foo.show();
                }
            }
        });

        aboutText = (TextView)findViewById(R.id.aboutText);
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());

        // try to get our version out of app/build.gradle
        // versionName field
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
            Log.d(TAG, "Got version " + versionName);
        }
        catch (Exception e) {
            versionName = "unknown";
        }

        // set About text
        aboutText.setText(Html.fromHtml( getString(R.string.app_name) + " " + getString(R.string.version_word) + " " + versionName+"<br>"
                        + "© 2025 Theta Informatics LLC<br>"
                        + "<a href=\"https://www.gnu.org/licenses/agpl-3.0.en.html\">AGPL-3.0</a> " + getString(R.string.software_license) + ",<br>"
                        + getString(R.string.some_rights_reserved) + "<br>" + "<br>"
                        + "Distribution Statement A: Approved for public release: distribution is unlimited" + "<br>"
                        + "<br> <a href=\"https://openathena.com/\">OpenAthena.com</a> <br>"
                        + getString(R.string.AboutSnippet) + "<br>"
                        + getString(R.string.CalibrationReminderSnippet) + "<br><br>"
                        + getString(R.string.tle_disclaimer) + "<br>"
                        + "<br><a href=\"https://github.com/mkrupczak3/OpenAthenaAndroid\">"
                        + getString(R.string.operation_manual) + "</a><br>"
                        + "<br><a href=\"https://github.com/Theta-Limited/OpenAthenaAndroid/blob/master/TROUBLESHOOTING.md\">"
                        + getString(R.string.troubleshooting_manual) + "</a><br>"
                        + "<br><a href=\"https://github.com/mkrupczak3/OpenAthenaAndroid\">"
                        + getString(R.string.CallToActionSnippet) + "<br>"
                        + getString(R.string.email_address_for_technical_support_prompt_about) + "<br>"
                        + "<a href=\"mailto:support@theta.limited?subject=Support Request, OpenAthena for Android " + versionName + "\">support@theta.limited</a>" + "<br>"
                        + "<br>" + getString(R.string.nato_vertical_datum_notice) + "<br>"
                        + getString(R.string.warsaw_vertical_datum_notice) + "<br>"
                        + getString(R.string.AuthorGitHubSnippet)
                        + getString(R.string.about_privacy_policy_label)
                        + " <a href=\"https://theta.limited/privacy-policy\">theta.limited/privacy-policy</a><br>"
                        + getString(R.string.about_software_libraries_used) + "<br>"
                        + "<a href=\"https://github.com/ngageoint/tiff-java\">ngageoint/tiff-java</a> MIT license<br>"
                        + "<a href=\"https://github.com/ngageoint/mgrs-android/\">ngageoint/mgrs-android</a> MIT license<br>"
                        + "<a href=\"https://github.com/veraPDF/veraPDF-xmp\">veraPDF/veraPDF-xmp</a> Adobe Community license<br>"
                        + "<a href=\"https://github.com/matthiaszimmermann/EGM96\">matthiaszimmermann/EGM96</a> MIT license<br>"
                        + "<a href=\"https://github.com/agilesrc/dem4j\">agilesrc/dem4j</a> Apache-2.0 license<br>"
                        + "<a href=\"https://github.com/ThreeTen/threetenbp\">ThreeTen/threetenbp</a> BSD 3-Clause<br>"
                        + "<a href=\"https://github.com/apache/commons-lang\">Apache Commons Lang</a> Apache License<br>"
                        + "<a href=\"https://commons.apache.org/proper/commons-math/\">Apache Commons Math</a> Apache License<br>"
                        + "<a href=\"https://github.com/apache/commons-io\">Apache Commons IO</a> Apache License<br><br>"
                        + getString(R.string.about_opentopography_api_description)
                        + "<br><a href=\"https://opentopography.org/privacypolicy\">https://opentopography.org/privacypolicy</a><br><br>"
                        + getString(R.string.about_about_opentopography)
                        + "<br><br>"
                        + getString(R.string.icon_credit)
                ,0,null, null)
        );

    } // onCreate()

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


    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG,"onPause");

        //appendText("onPause\n");
        super.onPause();

    } // onPause()

    @Override
    protected void onDestroy()
    {
        Log.d(TAG,"onDestroy started");

        // close logfile
        //appendText("onDestroy\n");
        // do whatever here
        super.onDestroy();

    } // onDestroy()

    private void appendText(final String aStr)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                aboutText.append(aStr);
            }
        });

    } // appendText to textView but do so on UI thread

//    private void appendLog(String str)
//    {
//        FileOutputStream fos;
//        PrintWriter pw;
//
//        Log.d(TAG,"appendLogLocal started");
//
//        try {
//            fos = openFileOutput(MainActivity.LOG_NAME, Context.MODE_PRIVATE|Context.MODE_APPEND);
//            pw = new PrintWriter(fos);
//            pw.print(str);
//            pw.close();
//            fos.close();
//            Log.d(TAG,"appendLogLocal: wrote to logfile");
//
//        } catch (Exception e) {
//            Log.d(TAG,"appendLogLocal: failed to write log:"+e.getMessage());
//        }
//
//    } // appendLog()
}
