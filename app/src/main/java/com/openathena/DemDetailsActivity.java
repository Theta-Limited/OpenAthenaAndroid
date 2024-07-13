// NewElevationMapActivity.java
// Bobby Krupczak, Matthew Krupczak et al
// rdk@theta.limited
//
// Activity to display details about an elevation map

package com.openathena;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class DemDetailsActivity extends AthenaActivity
{
    public static String TAG = DemDetailsActivity.class.getSimpleName();
    private TextView demInfo;
    private ActivityResultLauncher<String> copyDemLauncher;
    String exportFilename = "";
    DemCache.DemCacheEntry dEntry;

    static TimeZone local_tz;
    @SuppressLint("SimpleDateFormat")
    static DateFormat df_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    protected File demDir;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elevation_map_detail);
        demInfo = (TextView)findViewById(R.id.elevationMapDetailText);
        demInfo.setMovementMethod(LinkMovementMethod.getInstance());

        demDir = new File(getCacheDir(), "DEMs");

        local_tz = TimeZone.getDefault();
        df_ISO8601.setTimeZone(local_tz);

        // create the activity launcher here;
        // Storage Access Framework(SAF) does not require us to request permission
        // to access
        copyDemLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument(), uri -> {
            if (uri != null && !exportFilename.isEmpty()) {
                // copy the file to uri; since we have a File and
                // dialog gives us a URI, we need to open/read/write exported file
                // the try with resources statement automatically closes the streams
                // when it finishes
                try (InputStream inputStream = Files.newInputStream(new File(demDir, exportFilename).toPath());
                     OutputStream outStream = getContentResolver().openOutputStream(uri)) {

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outStream.write(buf, 0, len);
                    }
                    Log.d(TAG,"DemDetail: finished copying file to uri");
                    Toast.makeText(this, R.string.toast_demdetailsactivity_elevation_map_successfully_exported, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.d(TAG,"DemDetail: exception while exporting file "+e);
                    Toast.makeText(this, R.string.toast_demdetailsactivity_error_exporting_elevation_map, Toast.LENGTH_SHORT).show();
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
        exportFilename = dEntry.filename;
        String basename = exportFilename.substring(0,exportFilename.lastIndexOf("."));

        htmlString += "<b>"+dEntry.filename + "</b><br>";
        htmlString += "Modified: " + df_ISO8601.format(dEntry.modDate) + "<br>";

        // example DEM filename: DEM_LatLon_29.502361_-95.960694_30.172639_-94.881528
        String[] filename_pieces = basename.split("_");
        for (String aString : filename_pieces) {
            if (filename_pieces.length != 6 || aString.isEmpty()) {
                htmlString += getString(R.string.error_demdetailsactivity_invalid_filename) +" " + dEntry.filename + "<br>";
                htmlString += getString(R.string.error2_dem_details_activity_invalid_filename);
                demInfo.setText(Html.fromHtml(htmlString,0,null,null));
                return;
            }
        }

        double minLat, maxLat, minLon, maxLon;
        try {
            minLat = Double.parseDouble(filename_pieces[2]);
            minLon = Double.parseDouble(filename_pieces[3]);
            maxLat = Double.parseDouble(filename_pieces[4]);
            maxLon = Double.parseDouble(filename_pieces[5]);
        } catch (NumberFormatException nfe) {
            htmlString += getString(R.string.error_demdetailsactivity_invalid_filename) + " " + exportFilename + "<br>";
            htmlString += getString(R.string.error2_dem_details_activity_invalid_filename);
            demInfo.setText(Html.fromHtml(htmlString,0,null,null));
            return;
        }

        htmlString += "<br>";

        // text formatting tag for uniform character width
        String teletypeTextFlag = "<tt>"; // this tag is deprecated in HTML5, too bad we use it anyways
        // "<pre>" // proper tag for current rev HTML, does not seem to work with Android

        if(outputModeIsMGRS() || outputMode == outputModes.UTM || outputMode == outputModes.CK42GaussKrüger) { // For Grid-based output modes
            htmlString += "DEM lat/lon bounds (" + getCurrentOutputModeName() + "):" + "<br>";
            htmlString += teletypeTextFlag;
            htmlString += "NW: " + CoordTranslator.toSelectedOutputMode(maxLat,minLon,outputMode) + "<br>";
            htmlString += "NE: " + CoordTranslator.toSelectedOutputMode(maxLat,maxLon,outputMode) + "<br>";
            htmlString += "SW: " + CoordTranslator.toSelectedOutputMode(minLat,minLon,outputMode) + "<br>";
            htmlString += "SE: " + CoordTranslator.toSelectedOutputMode(minLat,maxLon,outputMode) + "<br>";
            // close pre tag
        } else { // For Geodetic output modes (WGS84 or CK42)
            htmlString += "DEM lat/lon bounds (" + getCurrentOutputModeName().split(" ")[0] + "):" + "<br>";
            htmlString += teletypeTextFlag;
            if (outputMode == outputModes.CK42Geodetic) {
                // this conversion isn't entirely accurate w/o correct altitude value, but is good enough for this UI
                minLat = CoordTranslator.toCK42Lat(minLat,minLon,0.0d);
                minLon = CoordTranslator.toCK42Lon(minLat,minLon,0.0d);
                maxLat = CoordTranslator.toCK42Lat(maxLat,maxLon,0.0d);
                maxLon = CoordTranslator.toCK42Lon(maxLat,maxLon,0.0d);
            }
            htmlString += "N: " + truncateDouble(maxLat, 6) + "<br>";
            htmlString += "E: " + truncateDouble(maxLon, 6) + "<br>";
            htmlString += "S: " + truncateDouble(minLat, 6) + "<br>";
            htmlString += "W: " + truncateDouble(minLon, 6) + "<br>";
        }
        htmlString += "</tt>";


        htmlString += "<br>";

        htmlString += "size: " + athenaApp.demCache.getAreaSizeString(dEntry,isUnitFoot()) + "<br>";
        String coordStr = truncateDouble(dEntry.cLat, 6)+","+truncateDouble(dEntry.cLon, 6);

        // Google Maps requires a ?q= tag to actually display a pin for the indicated location
        // https://en.wikipedia.org/wiki/Geo_URI_scheme#Unofficial_extensions
        String urlStr = "geo:"+coordStr+"?q="+coordStr;
        String lineStr = "center: <a href=\""+urlStr+"\">"+coordStr+"</a><br>";
        Log.d(TAG,"DemDetail: center link line is "+lineStr);
        htmlString += lineStr;
        htmlString += "size: "+dEntry.bytes/1024+" KB<br>";

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
        int itemID = item.getItemId();

        DemListAdapter adapter = new DemListAdapter(this,athenaApp.demCache);
        // Handle item selection
        if (itemID == R.id.action_delete_dem) {
            Log.d(TAG, "DemDetail: going to delete a DEM " + exportFilename);
            // Create an AlertDialog for confirmation
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_elevation_map_dialog_title)
                    .setIcon(R.drawable.athena48)
                    .setMessage(R.string.are_you_sure_delete_map)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {

                        // Delete the item from your data set
                        Log.d(TAG,"DemDetailsActivity: deleting item");
                        adapter.removeItem(athenaApp.demCache.selectedItem);
                        athenaApp.demCache.selectedItem = -1;
                        Intent i;
                        i = new Intent(getApplicationContext(), DemCacheListActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(i);
                        // Destroy this activity after DEM is deleted so we don't accidentally return to this activity and get a NullPointerException
                        this.finish();
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                        // User cancelled the deletion, refresh the item to show it again
                        Log.d(TAG,"DemDetailsActivity: not deleting item");
                        //new Handler(Looper.getMainLooper()).postDelayed(() -> adapter.notifyItemChanged(position), 300);
                    })
                    .show();
        } else if (itemID == R.id.action_export_dem) {
            Log.d(TAG, "DemDetail: going to export a DEM " + exportFilename);
            copyDemLauncher.launch(exportFilename);
            return true;
        } else if (itemID == R.id.action_share_dem) {
            Log.d(TAG, "DemDetail: going to share a DEM " + exportFilename);
            shareDemFile(new File(demDir, exportFilename));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareDemFile(File file) {
        try {
            Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant temporary read permission to the recipient
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.setType("application/octet-stream");
            startActivity(Intent.createChooser(shareIntent, "Share DEM"));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "The selected file can't be shared: " + file.toString());
            Toast.makeText(this, R.string.error_demdetailsactivity_failed_to_share_your_selected_dem_file, Toast.LENGTH_SHORT).show();
        }
    }

    public void calculateImage(View view) { return; } // not used in this activity
    public void calculateImage(View view, boolean shouldISendCoT) { return; } // not used in this activity
    protected void saveStateToSingleton() { return; } // do nothing

    private double truncateDouble(double val, int precision) {
        double scale = Math.pow(10, precision);
        return Math.round(val * scale) / scale;
    }

} // DemDetailsActivity
