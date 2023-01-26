// PrefsActivity.java
// OpenAthena for Android
// Bobby Krupczak, rdk@krupczak.org Matthew Krupczak, mwk@krupczak.org, et. al

package com.openathena;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import java.io.FileOutputStream;
import java.io.PrintWriter;

// handle permissions for accessing photos and
// log file (local storage) XXX


public class PrefsActivity extends AthenaActivity {

    public static String TAG = PrefsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);

        radioGroup = (RadioGroup) findViewById(R.id.outputModeRadioGroup);
        // Listener which changes outputMode when a radio button is selected
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButtonWGS84:
                        if (outputMode != outputModes.WGS84) {
                            setOutputMode(outputModes.WGS84);
                        }
                        break;
                    case R.id.radioButtonMGRS1m:
                        if (outputMode != outputModes.MGRS1m) {
                            setOutputMode(outputModes.MGRS1m);
                        }
                        break;
                    case R.id.radioButtonMGRS10m:
                        if (outputMode != outputModes.MGRS10m) {
                            setOutputMode(outputModes.MGRS10m);
                        }
                        break;
                    case R.id.radioButtonMGRS100m:
                        if (outputMode != outputModes.MGRS100m) {
                            setOutputMode(outputModes.MGRS100m);
                        }
                        break;
                    case R.id.radioButtonCK42Geodetic:
                        if (outputMode != outputModes.CK42Geodetic) {
                            setOutputMode(outputModes.CK42Geodetic);
                        }
                        break;
                    case R.id.radioButtonCK42GaussKrüger:
                        if (outputMode != outputModes.CK42GaussKrüger) {
                            setOutputMode(outputModes.CK42GaussKrüger);
                        }
                        break;
                    default:
                        setOutputMode(-1); // should never happen
                }
            }
        });

        restorePrefOutputMode();
    } // end onCreate()

    @Override
    protected void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);
        if (outputMode != null) {
            saveInstanceState.putInt("outputMode", outputMode.ordinal());
        }
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

        // don't do anything if they select prefs

        if (id == R.id.action_calculate) {
            // jump to main activity
            // its already created
            intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_about) {
            intent = new Intent(getApplicationContext(),AboutActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }

//        if (id == R.id.action_log) {
//            intent = new Intent(getApplicationContext(),ActivityLog.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            startActivity(intent);
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume started");
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG,"onPause started");

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

//    public void prefsSave(View view)
//    {
//        appendLog("Saving preferences/settings\n");
//
//    }

    public void prefsReset(View view)
    {
        appendLog("Resetting settings \uD83D\uDD04\n");
        setOutputMode(outputModes.WGS84);
    }

    private void appendLog(String str)
    {
        FileOutputStream fos;
        PrintWriter pw;

        Log.d(TAG,"appendLogLocal started");

        try {
            fos = openFileOutput(MainActivity.LOG_NAME, Context.MODE_PRIVATE|Context.MODE_APPEND);
            pw = new PrintWriter(fos);
            pw.print(str);
            pw.close();
            fos.close();
            Log.d(TAG,"appendLogLocal: wrote to logfile");

        } catch (Exception e) {
            Log.d(TAG,"appendLogLocal: failed to write log:"+e.getMessage());
        }

    } // appendLog()

}