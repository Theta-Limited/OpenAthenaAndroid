// OpenAthena for Android
// Bobby Krupczak, rdk@theta.limited Matthew Krupczak, matthew@theta.limited, et. al

package com.openathena;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class PrefsActivity extends AthenaActivity {

    public static String TAG = PrefsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);

        outputModeRadioGroup = (RadioGroup) findViewById(R.id.outputModeRadioGroup);
        measurementUnitRadioGroup = (RadioGroup) findViewById(R.id.measurementUnitRadioGroup);
        compassCorrectionSeekBar = findViewById(R.id.compassCorrectionSeekBar);
        compassCorrectionValue = findViewById(R.id.compassCorrectionValue);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Context parent = this;

        // Listener which changes outputMode when a radio button is selected
        outputModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButtonWGS84) {
                    if (outputMode != outputModes.WGS84) {
                        setOutputMode(outputModes.WGS84);
                    }
                } else if (checkedId == R.id.radioButtonMGRS1m) {
                    if (outputMode != outputModes.MGRS1m) {
                        setOutputMode(outputModes.MGRS1m);
                    }
                } else if (checkedId == R.id.radioButtonMGRS10m) {
                    if (outputMode != outputModes.MGRS10m) {
                        setOutputMode(outputModes.MGRS10m);
                    }
                } else if (checkedId == R.id.radioButtonMGRS100m) {
                    if (outputMode != outputModes.MGRS100m) {
                        setOutputMode(outputModes.MGRS100m);
                    }
                } else if (checkedId == R.id.radioButtonCK42Geodetic) {
                    if (outputMode != outputModes.CK42Geodetic) {
                        setOutputMode(outputModes.CK42Geodetic);
                    }
                } else if (checkedId == R.id.radioButtonCK42GaussKrüger) {
                    if (outputMode != outputModes.CK42GaussKrüger) {
                        setOutputMode(outputModes.CK42GaussKrüger);
                    }
                } else {
                    setOutputMode(-1); // should never happen
                }
            }
        });

        measurementUnitRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckChanged(RadioGroup group, int checkedId) {
              if (checkedId == R.id.radioButtonMETER) {
                  if (measurementUnit != measurementUnits.METER) {
                      setMeasurementUnit(measurementUnits.METER);
                  }
              } else if (checkedId == R.id.radioButtonFOOT) {
                  if (measurementUnit != measurementUnits.FOOT) {
                      if (outputModeIsSlavic() ) {
                          Toast.makeText(parent, "This option not compatible with CK42 Output Mode(s)", Toast.LENGTH_SHORT).show();
                          // TODO do this visually instead by disabling radioButtonFoot when a CK42 mode is active
                      } else {
                          setMeasurementUnit(measurementUnits.FOOT);
                      }
                  }
              } else {
                  setOutputMode(-1); // should never happen
              }
            }
        });

        restorePrefs();

        compassCorrectionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateCompassCorrection(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Save the new value to SharedPreferences
                saveCurSeekBarProgress();
            }
        });

    } // end onCreate()

    protected void saveCurSeekBarProgress() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("compassCorrectionSeekBarValue", compassCorrectionSeekBar.getProgress());
        editor.apply();
    }

    protected void saveStateToSingleton() {
        return; // do nothing
    }

    @Override
    protected void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);
        if (outputMode != null) {
            saveInstanceState.putInt("outputMode", outputMode.ordinal());
        }
        if (measurementUnit != null) {
            saveInstanceState.putInt("measurementUnit", measurementUnit.ordinal());
        }
    }

    public void calculateImage(View view) {
        return; // not used in this activity
    }

    public void calculateImage(View view, boolean shouldISendCoT) {
        return; // not used in this activity
    }

//    public void prefsSave(View view)
//    {
//        appendLog("Saving preferences/settings\n");
//
//    }

    public void prefsReset(View view)
    {
        appendLog("Resetting settings \uD83D\uDD04\n");
        setOutputMode(outputModes.WGS84);
        setCompassCorrectionSeekBar(100);
        saveCurSeekBarProgress();
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