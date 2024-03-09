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
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import androidx.core.util.Consumer;

public class DemDownloader
{
    // add your API key to local.properties in root of the android project
    // for example:
    // OPENTOPOGRAPHY_API_KEY=hexnumbers
    private String OPENTOPOGRAPHY_API_KEY = BuildConfig.OPENTOPOGRAPHY_API_KEY;
    public static String TAG = DemDownloader.class.getSimpleName();
    private static final String URL_STR = "https://portal.opentopography.org/API/globaldem?";
    private static final String DEM_TYPE_STR = "SRTMGL1";
    private int responseCode;
    private int responseBytes;
    private double s, w, n, e; // Bounding box coordinates
    private String filenameSuffix = ".tiff";
    private String outputFormatStr = "GTiff";
    private Context context;

    public DemDownloader(Context appContext, double lat, double lon, double length) {
        context = appContext;
        double[] boundingBox = getBoundingBox(lat, lon, length);
        n = boundingBox[0];
        s = boundingBox[1];
        e = boundingBox[2];
        w = boundingBox[3];

        Log.d(TAG,"DemDownloader: "+n+","+s+","+e+","+w);
    }

    // Blocking download of a DEM from OpenTopography
    public boolean syncDownload() throws IOException {
        String requestURLStr = URL_STR +
                "demtype=" + DEM_TYPE_STR +
                "&south=" + s +
                "&north=" + n +
                "&west=" + w +
                "&east=" + e +
                "&outputFormat=" + outputFormatStr +
                "&API_Key=" + OPENTOPOGRAPHY_API_KEY;
        boolean b = false;

        URL url = new URL(requestURLStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            Log.d(TAG,"DemDownloader: request failed, error code "+responseCode);
            return false;
        }

        // read and write out the data to file
        String filename = "DEM_LatLon_"+s+"_"+w+"_"+n+"_"+e+filenameSuffix;

        try {
            InputStream inputStream = connection.getInputStream();
            FileOutputStream outputStream = context.openFileOutput(filename,Context.MODE_PRIVATE);

            byte[] buffer = new byte[4096];
            int bytesRead = 0;
            int totalBytes = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer,0,bytesRead);
                totalBytes += bytesRead;
            }
            outputStream.close();
            Log.d(TAG,"DemDownloader: wrote "+totalBytes+" bytes to "+filename);
            b = true;
        }
        catch (Exception e) {
            Log.d(TAG,"DemDownloader: failed to write "+filename+" : "+e);
            b = false;
        }

        connection.disconnect();

        return true;

    } // syncDownload

    // down a DEM async or in background
    // callback will indicate success or error
    // pass an object that implements a callback metho
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
                            consumer.accept("Download succeeded");
                        }
                        else {
                            consumer.accept("Download failed");
                        }
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
        final double metersInDegreeLatitude = 111320; // Approximate meters in one degree of latitude

        // Calculate deltas
        double deltaLat = (length / 2) / metersInDegreeLatitude;
        double deltaLon = (length / 2) / (metersInDegreeLatitude * Math.cos(Math.toRadians(centerLat)));

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
