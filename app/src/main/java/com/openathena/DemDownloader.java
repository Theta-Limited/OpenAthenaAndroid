// DemDownloader.java
// Bobby Krupczak and ChatGPT
// rdk@theta.limited
//
// download a digital elevation model/map
// from OpenTopography.org and write out
// to a tiff file;
// API Key is needed but don't check that key into public
// Git repo

package com.openathena;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import androidx.core.util.Consumer;

public class DemDownloader
{
    // Make sure to add your OpenTopography API key to local.properties in root of the android project!
    // for example:
    // OPENTOPOGRAPHY_API_KEY=nlhhp3yd9ud54tr3eem4akqv49wcb23i
    // that is not an actual valid key btw. You will need to obtain your own from:
    // https://opentopography.org/blog/introducing-api-keys-access-opentopography-global-datasets
    public static String TAG = DemDownloader.class.getSimpleName();
    protected static final String URL_STR = "https://portal.opentopography.org/API/globaldem?";
    // Use COP30 everywhere due to better results accuracy (observed in empirical testing) than SRTM
    // https://github.com/Theta-Limited/OpenAthenaAndroid/issues/187
    protected static String demTypeStr = "COP30";
    private int responseCode;
    private int responseBytes;
    private double s, w, n, e; // Bounding box coordinates
    protected static final String filenameSuffix = ".tiff";
    public String filename;
    private static final String OUTPUT_FORMAT_STRING = "GTiff";
    private Context context;

    protected File demDir;

    public DemDownloader(Context appContext, double lat, double lon, double length) {
        context = appContext;
        if (context == null) {
            throw new IllegalArgumentException("ERROR: tried to initialize DemCache object with a null Context!");
        }
        demDir = new File(context.getCacheDir(), "DEMs");
        if (!demDir.exists()) {
            demDir.mkdirs();
        }

        double[] boundingBox = getBoundingBox(lat, lon, length);
        n = boundingBox[0];
        s = boundingBox[1];
        e = boundingBox[2];
        w = boundingBox[3];

        filename = "DEM_LatLon_"+s+"_"+w+"_"+n+"_"+e+filenameSuffix;

        Log.d(TAG,"DemDownloader: "+n+","+s+","+e+","+w);
    }

    protected String getDemApiKey() {
        if (context == null) return "";
        String apiKey = "";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        apiKey = sharedPreferences.getString("OPENTOPOGRAPHY_API_KEY", "");
        if (apiKey.isEmpty()) {
            apiKey = BuildConfig.OPENTOPOGRAPHY_API_KEY;
        }
        return apiKey;
    }

    // Method to check the validity of the API Key
    public boolean isApiKeyValid() throws IOException {
        // Minimal bounding box at a location with no data
        String testUrl = URL_STR +
                "demtype=" + demTypeStr +
                "&south=0" +
                "&north=0.01" +
                "&west=0" +
                "&east=0.01" +
                "&outputFormat=" + OUTPUT_FORMAT_STRING +
                "&API_Key=" + getDemApiKey();

        URL url = new URL(testUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        responseCode = connection.getResponseCode();
        connection.disconnect();

        // Check for valid responses indicating a valid API key
        return (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_BAD_REQUEST);
    }

    public boolean isHttpResponseCodeOk() {
        return responseCode == HttpURLConnection.HTTP_OK;
    }

    // Blocking download of a DEM from OpenTopography
    public boolean syncDownload() throws IOException {
//        String demTypeStr;
//        if (n <= 60.0d && s > -56.0d) {
//            // SRTM GL1 v3, to be used for locations on Earth within coverage area of 60.0° N to 56.0° S
//            // https://portal.opentopography.org/datasetMetadata?otCollectionID=OT.042013.4326.1
//            demTypeStr = "SRTMGL1";
//        } else {
//            // Copernicus GLO-30 generated with X-band SAR data from the TanDEM-X
//            // to be used for extreme latitudes (above 60.0° N or below 56.0° S) only
//            // https://ilrs.gsfc.nasa.gov/missions/satellite_missions/current_missions/tand_general.html
//            // https://portal.opentopography.org/datasetMetadata?otCollectionID=OT.032021.4326.1
//            demTypeStr = "COP30";
//        }


        String requestURLStr = URL_STR +
                "demtype=" + demTypeStr +
                "&south=" + s +
                "&north=" + n +
                "&west=" + w +
                "&east=" + e +
                "&outputFormat=" + OUTPUT_FORMAT_STRING +
                "&API_Key=" + getDemApiKey();
        boolean isDownloadSuccessful = false;

        URL url = new URL(requestURLStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            Log.d(TAG,"DemDownloader: request failed, error code "+responseCode);
            Log.d(TAG, "requestURLStr was: " + requestURLStr);
            return false;
        }

        // read and write out the data to file

        try {
            InputStream inputStream = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(new File(demDir,filename));

            byte[] buffer = new byte[4096];
            int bytesRead = 0;
            int totalBytes = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer,0,bytesRead);
                totalBytes += bytesRead;
            }
            outputStream.close();
            Log.d(TAG,"DemDownloader: wrote "+totalBytes+" bytes to "+filename);
            isDownloadSuccessful = true;
        }
        catch (Exception e) {
            Log.d(TAG,"DemDownloader: failed to write "+filename+" : "+e);
            isDownloadSuccessful = false;
        }

        connection.disconnect();

        return isDownloadSuccessful;

    } // syncDownload



    // down a DEM async or in background
    // callback will indicate success or error
    // pass an object that implements a callback method
    // onCallback() method

    public void asyncDownload(Consumer<String> consumer)
    {
        Thread aThread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                boolean b;

                // call the sync download from within our thread
                //
                try {
                    b = syncDownload();
                    if (consumer != null) {
                        if (b == true) {
                            consumer.accept(context.getString(R.string.demdownloader_download_succeeded));
                        }
                        else {
                            consumer.accept(context.getString(R.string.dem_downloader_download_failed_invalid_key));
                        }
                    }
                }
                catch (java.net.UnknownHostException uhe) {
                    if (consumer != null) {
                        consumer.accept(context.getString(R.string.error_demdownloader_could_not_connect)+"\n " + context.getString(R.string.error_demdownloader_internet_reminder) + "\n\n" + "(" + context.getString(R.string.prompt_use_blah) + context.getString(R.string.action_demcache) + context.getString(R.string.to_import_an_offline_dem_file_manually) + "\n");
                    }
                }
                catch (java.net.SocketException se) {
                    if (consumer != null) {
                        consumer.accept(context.getString(R.string.error_demdownloader_connectus_interruptus));
                    }
                }
                catch (Exception e) {
                    if (consumer != null) {
                        consumer.accept(e.toString());
                    }
                }
            } // run
        });

        aThread.start();
        Log.d(TAG,"DemDownloader: async download starting");

    } // asyncDownload

    // Method to calculate the bounding box
//    private double[] getBoundingBox(double centerLat, double centerLon, double length) {
//        double d = Math.sqrt(2.0) * (length / 2.0);
//        double[] sw = calculateCoordinate(centerLat, centerLon, 225.0, d);
//        double[] ne = calculateCoordinate(centerLat, centerLon, 45.0, d);
//
//        return new double[]{truncateDouble(sw[0], 6), truncateDouble(sw[1], 6),
//                            truncateDouble(ne[0], 6), truncateDouble(ne[1], 6)};
//    }
//
//    // Helper method to calculate new coordinate; ChatGPT
//    private double[] calculateCoordinate(double lat, double lon, double bearing, double distance) {
//        double radius = 6371e3; // Earth's radius in meters
//        double angularDistance = distance / radius;
//
//        double latRad = Math.toRadians(lat);
//        double lonRad = Math.toRadians(lon);
//        bearing = Math.toRadians(bearing);
//
//        double newLat = Math.asin(Math.sin(latRad) * Math.cos(angularDistance) +
//                                  Math.cos(latRad) * Math.sin(angularDistance) * Math.cos(bearing));
//        double newLon = lonRad + Math.atan2(Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(latRad),
//                                            Math.cos(angularDistance) - Math.sin(latRad) * Math.sin(newLat));
//
//        return new double[]{Math.toDegrees(newLat), Math.toDegrees(newLon)};
//    }

    // calculate bounding box; return [n,s,e,w]
    private double[] getBoundingBox(double centerLat, double centerLon, double length)
    {
        double deltaLat = TargetGetter.inverse_haversine(centerLat, centerLon, length / 2.0d, 0.0d,0.0d)[0] - centerLat;
//        Log.d(TAG, "getBoundingBox deltaLat: " + deltaLat);
        double deltaLon = TargetGetter.inverse_haversine(centerLat, centerLon, length / 2.0d, Math.PI/2.0d, 0.0d)[1] - centerLon;
//        Log.d(TAG, "getBoundingBox deltaLon" + deltaLon);

        // Calculate bounding box
        double north = centerLat + deltaLat;
        double south = centerLat - deltaLat;
        double east = centerLon + deltaLon;
        double west = centerLon - deltaLon;

        return new double[]{truncateDouble(north,6), truncateDouble(south,6), truncateDouble(east,6), truncateDouble(west,6)};
    }

    public static double truncateDouble(double val, int precision) {
        double scale = Math.pow(10, precision);
        return Math.round(val * scale) / scale;
    }

} // DemDownloader
