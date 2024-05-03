// NewElevationMapActivity.java
// Bobby Krupczak, Matthew Krupczak et al
// rdk@theta.limited
//
// Activity to display details about an elevation map

package com.openathena;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.util.Consumer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.openathena.databinding.ActivityAboutBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class ElevationMapDetailsActivity extends AthenaActivity
{
    public static String TAG = ElevationMapDetailsActivity.class.getSimpleName();
    private TextView demInfo;
    private ActivityResultLauncher<String> copyDemLauncher;
    String exportFilename = "";
    DemCache.DemCacheEntry dEntry;

    static TimeZone local_tz = TimeZone.getDefault();
    static DateFormat df_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elevation_map_detail);
        demInfo = (TextView)findViewById(R.id.elevationMapDetailText);
        demInfo.setMovementMethod(LinkMovementMethod.getInstance());

        df_ISO8601.setTimeZone(local_tz);

        // create the activity launcher here;
        // Storage Access Framework(SAF) does not require us to request permission
        // to access
        copyDemLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument(), uri -> {
            if (uri != null && !exportFilename.equals("")) {
                // copy the file to uri; since we have a File and
                // dialog gives us a URI, we need to open/read/write exported file
                // the try with resources statement automatically closes the streams
                // when it finishes
                try (InputStream inputStream = new FileInputStream(new java.io.File(getFilesDir(), exportFilename));
                     OutputStream outStream = getContentResolver().openOutputStream(uri)) {

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outStream.write(buf, 0, len);
                    }
                    Log.d(TAG,"DemDetail: finished copying file to uri");
                    Toast.makeText(this, "Elevation map successfully exported", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.d(TAG,"DemDetail: exception while exporting file "+e);
                    Toast.makeText(this, "Error exporting elevation map", Toast.LENGTH_SHORT).show();
                }
            }
        });
    } // onCreate

    @Override
    protected void onResume()
    {
        super.onResume();

        // fill in details of the elevation map XXX
        String htmlString = "";
        dEntry = athenaApp.demCache.cache.get(athenaApp.demCache.selectedItem);
        exportFilename = dEntry.filename + ".tiff";

        htmlString += "<b>"+dEntry.filename + "</b><br>";
        htmlString += "Modified: " + df_ISO8601.format(dEntry.modDate) + "<br>";

        // load the DEM into object
        DEMParser aParser;
        File file = new File(getApplicationContext().getFilesDir(),dEntry.filename+".tiff");
        aParser = new DEMParser(file);

        htmlString += "n: " + truncateDouble(aParser.getMaxLat(),6)+"<br>";
        htmlString += "s: " + truncateDouble(aParser.getMinLat(),6)+"<br>";
        htmlString += "e: " + truncateDouble(aParser.getMaxLon(),6)+"<br>";
        htmlString += "w: " + truncateDouble(aParser.getMinLon(),6)+"<br>";
        htmlString += "length: " + truncateDouble(dEntry.l,0)+"<br>";
        String coordStr = truncateDouble(dEntry.cLat, 6)+","+truncateDouble(dEntry.cLon, 6);
        // make sure layout xml has clickable=true in for the textview widget
        // and make sure to add LinkMovementMethod too
        String urlStr = "https://www.google.com/maps/search/?api=1&t=k&query="+coordStr;
        String lineStr = "center: <a href=\""+urlStr+"\">"+coordStr+"</a><br>";
        Log.d(TAG,"DemDetail: center link line is "+lineStr);
        htmlString += lineStr;
        htmlString += "size: "+dEntry.bytes/1024+" KB<br>";
        htmlString += "loaded ok: true<br>";

        Log.d(TAG,"DemDetail: url is "+urlStr);

        demInfo.setText(Html.fromHtml(htmlString,0,null,null));

    } // onRsume

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.dem_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent intent;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_export_dem:
                Log.d(TAG,"DemDetail: going to export a DEM "+exportFilename);
                copyDemLauncher.launch(exportFilename);
                return true;
            default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void calculateImage(View view) { return; } // not used in this activity
    public void calculateImage(View view, boolean shouldISendCoT) { return; } // not used in this activity
    protected void saveStateToSingleton() { return; } // do nothing

    private double truncateDouble(double val, int precision) {
        double scale = Math.pow(10, precision);
        return Math.round(val * scale) / scale;
    }

} // ElevationMapDetailsActivity
