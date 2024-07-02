// DemCache.java
// Bobby Krupczak, Matthew Krupczak et al
// rdk@theta.limited

// manage digital elevation module cache
// files are downloaded, imported, exported, etc.
// filenames take format:
//   DEM_LatLon_LowerLeft_UpperRight.tiff
//   DEM_LatLon_s_w_n_e.tiff

// translated from Swift with help of ChatGPT4

package com.openathena;

import java.util.Date;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class DemCache {

    public static String TAG = DemCache.class.getSimpleName();

    public class DemCacheEntry {
        String filename;
        Uri fileUri;
        double n; // north lat
        double e; // east lon
        double s; // south lat
        double w; // west lon
        double l; // len or width/height of box in meters
        double cLat; // center lat
        double cLon; // center lon
        Date createDate;
        Date modDate;
        long bytes;

        public DemCacheEntry(String filename, Uri fileUri, double n, double s, double e, double w,
                             double l, double cLat, double cLon, Date createDate, Date modDate,
                             long bytes) {

            this.filename = filename;
            this.fileUri = fileUri;
            this.n = n;
            this.s = s;
            this.e = e;
            this.w = w;
            this.l = l;
            this.cLat = cLat;
            this.cLon = cLon;
            this.createDate = createDate;
            this.modDate = modDate;
            this.bytes = bytes;

        } // DemCacheEntry constructor

        public boolean contains(double lat, double lon) {
            return (lat <= n && lat >= s && lon >= w && lon <= e);
        }

    } // class DemCacheEntry

    // DemCache instance variables
    public long totalBytes = 0;
    public List<DemCacheEntry> cache;
    public Context context;
    public int selectedItem = -1;

    public DemCache(Context context)
    {
        // read/scan app document/storage directory for .tiff files
        // DEM_LatLon_s_w_n_e.tiff
        this.context = context;

        Log.d(TAG,"DemCache: starting");

        refreshCache();

        // launch a downloader for testing XXX
        // remove after testing
        // need to test this DEM for correctness

        //DemDownloader aDownloader = new DemDownloader(context, 33.708768, -84.440384, 10000.0);
        //
        //aDownloader.asyncDownload(new Consumer<String>() {
        //   @Override
        //    public void accept(String s) {
        //       Log.d(TAG,"DemCache: downloader returned "+s);
        //  }
        //});

    } // DemCache() constructor

    // refresh the cache after say new downloads or imports XXX

    public void refreshCache()
    {

        File appDir = context.getFilesDir();

        // reset the array list
        cache = new ArrayList<DemCacheEntry>();
        selectedItem = -1;

        // list all .tiff files in the main app dir
        File[] files = appDir.listFiles((dir,name) -> name.toLowerCase().endsWith(".tiff"));
        if (files != null) {

            Log.d(TAG,"DemCache: found "+files.length+" files to look at");

            for (File file: files) {

                try {
                    // strip off .tiff
                    String filename = file.getName().substring(0,file.getName().lastIndexOf("."));
                    Log.d(TAG,"DemCache: examining file "+filename);
                    Log.d(TAG,"DemCache: file exists is "+file.exists());

                    // make sure file starts with DEM_LatLon; will be 6 pieces
                    // stat the file first to get attributes
                    // stat the file to get createDate and modDate XXX
                    Date modDate = new Date(file.lastModified());
                    long fileSize = file.length();
                    // android gives us a NoSuchFileException when we try to get
                    // the file create time; so, skip
                    // BasicFileAttributes attrs = Files.readAttributes(Paths.get(file.getName()),BasicFileAttributes.class);
                    // Date createDate = new Date(attrs.creationTime().toMillis());
                    Date createDate = new Date();

                    Log.d(TAG,"DemCache: successfully got file attributes");

                    if (filename.startsWith("DEM_LatLon_")) {
                        String[] pieces = filename.split("_");
                        if (pieces.length == 6) {
                            Double s,w,n,e,clat,clon,l;
                            s = Double.parseDouble(pieces[2]);
                            w = Double.parseDouble(pieces[3]);
                            n = Double.parseDouble(pieces[4]);
                            e = Double.parseDouble(pieces[5]);

                            double v[] = getCenterAndLength(s,w,n,e);
                            clat = v[0];
                            clon = v[1];
                            l = v[2];

                            Uri fileUri = Uri.fromFile(file);
                            DemCacheEntry aDem = new DemCacheEntry(filename,fileUri,n,s,e,w,l,clat,clon,createDate,modDate,fileSize);
                            cache.add(aDem);
                            totalBytes += aDem.bytes;

                            Log.d(TAG,"DemCache: successfully parse filename and created cache entry");
                        } // pieces

                    } // DEM_

                } catch (Exception e) {
                    // ignore exception
                    Log.d(TAG,"DemCache: exception examing/loading DEM file "+e);
                }

            } // foreach file

        } // files
        else {
            Log.d(TAG,"DemCache: no files to scan");
        }
    }

    public int count() { return cache.size(); }

    public long totalStorage()  { return totalBytes; }

    public void removeCacheEntry(int index) {
        if (index >= 0 && index < cache.size()) {
            DemCacheEntry removed = cache.remove(index);
            totalBytes -= removed.bytes;
            // delete the file
            Log.d(TAG,"DemCache: deleting "+removed.filename);
            String aFilename = removed.filename+".tiff";

            File file = new File(context.getFilesDir(),aFilename);
            boolean ret = file.delete();

            if (!ret) {
                Log.d(TAG,"DemCache: failed to delete file");
            }
            else {
                Log.d(TAG,"DemCache: deleted file successfully");
            }
        }
    }

    // given a filename (w/o tiff), find it and set the selectedItem param
    // return the selected item number
    public int setSelectedItem(String filename)
    {
        selectedItem = -1;

        for (DemCacheEntry entry : cache) {
            if (entry.filename.equals(filename)) {
                selectedItem = cache.indexOf(entry);
            }
        }

        return selectedItem;
    }

    public String searchCacheFilename(double lat, double lon) {
        DemCacheEntry entry = searchCacheEntry(lat, lon);
        return entry != null ? entry.filename : "";
    }

    // search cache to find an entry with most coverage around the point or null if
    // no matching entry
    public DemCacheEntry searchCacheEntry(double lat, double lon) {
        double maxCoverage = -1;  // We want to maximize this value
        DemCacheEntry bestEntry = null;

        for (DemCacheEntry entry : cache) {
            if (entry.contains(lat,lon)) {
                // Calculate coverage as the minimum distance to the boundary from the search position
                double northCoverage = Math.abs(entry.n - lat);
                double southCoverage = Math.abs(lat - entry.s);
                double eastCoverage = Math.abs(entry.e - lon);
                double westCoverage = Math.abs(lon - entry.w);

                // The minimum of these distances tells us how "well" the point is covered in the smallest dimension
                double coverage = Math.min(Math.min(northCoverage, southCoverage), Math.min(eastCoverage, westCoverage));

                if (coverage > maxCoverage) {
                    maxCoverage = coverage;
                    bestEntry = entry;
                }
            }
        }
        return bestEntry;
    }

    // get center and diameter from bounding box
    // unfortunately, haversine() function reverses order of lat,lon to be lon,lat -- ug!
//    private double[] getCenterAndLength(double s, double w, double n, double e)
//    {
//        // get distance between s,w and n,w
//        double d12 = TargetGetter.haversine(w,s,w,n,0);
//
//        // get distance between s,w and s,e
//        double d14 = TargetGetter.haversine(w,s,e,s,0);
//
//        // get distance between n,w and n,e
//        double d23 = TargetGetter.haversine(w,n,e,n,0);
//
//        // get distance between n,e and s,e
//        double d34 = TargetGetter.haversine(e,n,e,s,0);
//
//        // average the 4 distances; l represents width and height of
//        // bounding box between four corners
//        double l = (d12 + d14 + d23 + d34) / 4.0;
//
//        // calculate distance to center which is arcLen;
//        // divide by 6371*1000 as thats the radius in in meters of earth
//        double h = Math.sqrt( 2*(l/2.0)*(l/2.0) ) / (6371*1000);
//
//        // now translate coordinate from s,w bearing 45.0 arcLen: h
//        double vals[] = DemCache.translateCoordinate(s,w,45.0,h);
//
//        return new double[]{vals[0], vals[1], l};
//
//    } // getCenterAndLength

    private double[] getCenterAndLength(double s, double w, double n, double e)
    {
        double EARTH_RADIUS = TargetGetter.radius_at_lat_lon(n+s / 2.0d, 0.0d);
//        double METERS_PER_DEGREE_LATITUDE = 111320; // Approximate meters per degree

        // Center coordinates
        double centerLat = (n + s) / 2;
        double centerLon = (e + w) / 2;

        // Height in meters (difference in latitude)
//        double height = Math.abs(n - s) * METERS_PER_DEGREE_LATITUDE;
        double height = TargetGetter.haversine(0.0d,n,0.0d,s,0.0d);

        // Width in meters (difference in longitude), considering the average latitude for more accuracy
        double averageLatitude = Math.abs(n + s) / 2;
//        double width = Math.abs(e - w) * METERS_PER_DEGREE_LATITUDE * Math.cos(Math.toRadians(averageLatitude));
        double width = TargetGetter.haversine(w,s,e,s,0.0d);

        double l = width*height;

        return new double[]{centerLat, centerLon, l};
    }

} // class DemCache
