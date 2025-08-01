package com.openathena;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class CursorOnTargetSender {

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

    private static long eventuid = 0;
    private static Context mContext;

    // Estimate of max linear (vertical) error for any SRTM elevation value
    // from https://doi.org/10.1016/j.asej.2017.01.007
    private static final double LINEAR_ERROR = 5.9d;

    // Approximately the minimum possible horizontal error from a typical GPS unit
    private static final double MINIMUM_ERROR = 5.0d;

    static TimeZone tz = TimeZone.getTimeZone("UTC");
    @SuppressLint("SimpleDateFormat")
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Sends a Cursor on Target message for a target calculated by OpenAthena
     * @param invoker Context of the caller activity
     * @param lat Latitude of target
     * @param lon Longitude of target
     * @param hae Elevation of target (in WGS84 height above ellipsoid vertical datum)
     * @param theta Angle of camera depression (in degrees, downwards is positive)
     * @param slant_range Distance from camera to target (in meters) calcualted by terrain-raycast
     * @param isDroneModelRecognized True if the drone's camera has calibration values in droneModels.json, false otherwise
     * @param tle_model Contains parameters of 2 factor linear model for target location error estimation
     * @param exif_datetime Exif date and time of image taken
     * @param openAthenaCalculationInfo LinkedHashMap<String,String> containing all the various calculation data from oaInfoMap to use for statistics/experimentation and debugging
     */
    public static void sendCoT(Context invoker, double lat, double lon, double hae, double theta, double slant_range, boolean isDroneModelRecognized, TLE_Model_Parameters tle_model, String exif_datetime, LinkedHashMap<String,String> openAthenaCalculationInfo) {
        if(invoker == null){
            throw new IllegalArgumentException("invoker context can not be null");
        } else if (!(invoker instanceof android.app.Activity)) {
            throw new IllegalArgumentException("invoker context must be an Activity");
        } else if (theta > 90) {
            // If camera is facing backwards, use the appropriate value for the reverse direction (the supplementary angle of theta)
            theta = 180.0d - theta;
        } else if (lat > 90 || lat < -90){
            throw new IllegalArgumentException("latitude " + lat + " degrees is invalid");
        } else if (lon > 180 || lon < -180) {
            throw new IllegalArgumentException("longitude " + lon + " degrees is invalid");
        } else if (exif_datetime == null) {
            throw new IllegalArgumentException("exif_datetime was null pointer, expected a String");
        }

        mContext = invoker;
        loadUid();

        df.setTimeZone(tz);
        Date now = new Date();
        String nowAsISO = df.format(now);
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.MINUTE, 5);
        Date fiveMinsFromNow = cal.getTime();
        String fiveMinutesFromNowISO = df.format(fiveMinsFromNow);
        String imageISO = df.format(convert(exif_datetime));

        double circularError = calculateCircularError(slant_range, isDroneModelRecognized, tle_model); // optimistic estimation of 2 sigma accuracy based on angle of camera depression theta
        String le = Double.toString(LINEAR_ERROR);
        String ce = Double.toString(circularError);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String uidString = buildUIDString(invoker);
//                String xmlString = buildCoT(uidString, imageISO, nowAsISO, fiveMinutesFromNowISO, Double.toString(lat), Double.toString(lon), ce, Double.toString(Math.round(hae)), le);
                String xmlString = buildCoT(uidString, imageISO, nowAsISO, fiveMinutesFromNowISO, Double.toString(lat), Double.toString(lon), ce, Double.toString(hae), le, openAthenaCalculationInfo);
//                String dumxml = "<event uid=\"41414141\" type=\"a-u-G\" how=\"h-c\" start=\"2023-01-24T22:16:53Z\" time=\"2023-01-24T22:16:53Z\" stale=\"2023-01-25T22:06:53Z\"><point le=\"0\" ce=\"0\" hae=\"0\" lon=\"0\" lat=\"0\"/></event>";
//                dumxml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + dumxml;
                Log.d("CursorOnTargetSender", xmlString);
                deliverUDP(xmlString); // increments uid upon success
            }
        }).start();

    }

    /**
     * Estimates the circular error for a target calculated by OpenAthena based on a one factor linear model
     * <p>
     *     Uses a one factor linear model which accounts for slant range from drone to target<br>
     *     For more info see:<br>
     *     <a href="https://github.com/Theta-Limited/OpenAthenaAndroid/pull/191">https://github.com/Theta-Limited/OpenAthenaAndroid/pull/191</a> <br>
     *     <a href="https://github.com/Theta-Limited/DroneModels?tab=readme-ov-file#target-location-error-tle-estimation-model-parameters">https://github.com/Theta-Limited/DroneModels?tab=readme-ov-file#target-location-error-tle-estimation-model-parameters</a> <br>
     *     <a href="https://github.com/Theta-Limited/OA-Accuracy-Testing/blob/6d480938d57f11fb4aab5a942e695c4d8c3559cd/Analyze-OA-CoT-and-GCP-csv-data.R#L248">https://github.com/Theta-Limited/OA-Accuracy-Testing/blob/6d480938d57f11fb4aab5a942e695c4d8c3559cd/Analyze-OA-CoT-and-GCP-csv-data.R#L248</a><br>
     *
     * </p>
     * @param slant_range Distance from camera to target (in meters) calculated by terrain-raycast
     * @param isDroneModelRecognized True if the drone's camera has calibration values in droneModels.json, false otherwise
     * @param tle_model Contains parameters of one factor linear model for target location error estimation
     * @return Estimated circular error (in meters)
     */
    public static double calculateCircularError(double slant_range, boolean isDroneModelRecognized, TLE_Model_Parameters tle_model) {
        // If the camera's intrinsic parameters (calibration values) are missing, accuracy will be significantly degraded.
        if(!isDroneModelRecognized) {
            // return an estimate with the highest category of predicted error (CAT 6)
            return 306.0d;
        }

        // params for one factor linear model, consisting of a y intercept and slope
        double tle_model_y_intercept = tle_model.tle_model_y_intercept;
        double tle_model_slant_range_coeff = tle_model.tle_model_slant_range_coeff;

        // one factor linear model for circular error estimation based on slant_range from drone to target
        // for more details, see https://github.com/Theta-Limited/DroneModels?tab=readme-ov-file#target-location-error-tle-estimation-model-parameters
        //                   and https://github.com/Theta-Limited/OA-Accuracy-Testing
        double predicted_ce = tle_model_y_intercept + slant_range * tle_model_slant_range_coeff;
        return predicted_ce;
    }

    // Target Location Error categories
    // from: https://www.bits.de/NRANEU/others/jp-doctrine/jp3_09_3%2809c%29.pdf
    // pg. V-4
    public enum TLE_Categories {
        CAT_1, // 0 to < 7 meters
        CAT_2, // 7 to < 16 meters
        CAT_3, // 16 to < 30 meters
        CAT_4, // 31 to < 91 meters
        CAT_5, // 92 to < 305 meters
        CAT_6, // > 305 meters
    }

    /**
     * Obtains target location error category based on circular error value
     * @param circular_error Estimated circular error (in meters)
     * @return Target location Error (TLE) category
     */
    public static TLE_Categories errorCategoryFromCE(double circular_error) {
        if (circular_error > 305.0) {
            return TLE_Categories.CAT_6;
        } else if (circular_error > 92.0) {
            return TLE_Categories.CAT_5;
        } else if (circular_error > 31.0) {
            return TLE_Categories.CAT_4;
        } else if (circular_error > 16.0) {
            return TLE_Categories.CAT_3;
        } else if (circular_error > 7.0) {
            return TLE_Categories.CAT_2;
        } else if (circular_error >= 0.0) {
            return TLE_Categories.CAT_1;
        } else {
            // This should never happen
            return TLE_Categories.CAT_6;
        }
    }

    /**
     * Obtains HTML color code corresponding to a given TLE category
     * @param tle_cat TLE category
     * @return HTML color code
     */
    public static String htmlColorFromTLE_Category(TLE_Categories tle_cat) {
        if (tle_cat == TLE_Categories.CAT_1) {
            // Green
            return("#00FF00");
        } else if (tle_cat == TLE_Categories.CAT_2) {
            // Yellow
            return("#FFFF00");
        } else if (tle_cat == TLE_Categories.CAT_3) {
            // Red
            return("#FF0000");
        } else {
            // regular
            return("");
        }
    }

    /**
     * Build string for Cursor on Target UID based on the device's unique ID, and a serial number for the message
     * @param invoker Context of the caller activity
     * @return String to use for the Cursor on Target UID
     */
    public static String buildUIDString(Context invoker) {
        mContext = invoker;
        loadUid();
        return UIDGenerator.buildUIDString(invoker, eventuid);
    }

    /**
     * Converts an ExifInterface time and date tag into a Joda time format
     *
     * @param exif_tag_datetime Exif time and date tag value
     * @return null in case of failure, the date object otherwise
     */
    public static Date convert(String exif_tag_datetime) {
        // EXIF tag contains no time zone data, assume it is same as local time
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault()); // default locale of device at application start
        Date outDate;
        try {
            outDate = simpleDateFormat.parse(exif_tag_datetime);
        } catch (ParseException e) {
            outDate = null;
        }
        return outDate;
    }


    /**
     * Build Cursor on Target message for a target calculated by OpenAthena (an XML-like message format)
     * @param uid String to use for the Cursor on Target UID
     * @param imageISO ISO8601 date and time of image taken
     * @param nowAsISO ISO8601 date and time of now
     * @param fiveMinutesFromNowISO ISO8601 date and time 5 minutes from now
     * @param lat Latitude of target
     * @param lon Longitude of target
     * @param ce Circular error of calculation about true target (in meters)
     * @param hae Elevation of target (in WGS84 height above ellipsoid vertical datum)
     * @param le Linear (vertical) error of calculation about true target (in meters)
     * @param oaInfoMap LinkedHashMap<String,String> containing all the various calculation data from oaInfoMap to use for statistics/experimentation and debugging
     * @return a String containing the built Cursor on Target message
     */
    public static String buildCoT(String uid, String imageISO, String nowAsISO, String fiveMinutesFromNowISO, String lat, String lon, String ce, String hae, String le, LinkedHashMap<String,String> oaInfoMap) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();


            Element root = doc.createElement("event");
            root.setAttribute("version", "2.0");
            root.setAttribute("uid", uid);
            root.setAttribute("type", "a-p-G");
            root.setAttribute("how", "h-c");
            root.setAttribute("time", imageISO);
            root.setAttribute("start", nowAsISO);
            root.setAttribute("stale", fiveMinutesFromNowISO);
            doc.appendChild(root);

            Element point = doc.createElement("point");
            point.setAttribute("lat", lat);
            point.setAttribute("lon", lon);
            point.setAttribute("ce", ce);
            point.setAttribute("hae", hae);
            point.setAttribute("le", le);
            root.appendChild(point);

            Element detail = doc.createElement("detail");
            root.appendChild(detail);

            Element precisionlocation = doc.createElement("precisionlocation");
            precisionlocation.setAttribute("altsrc", "DTED2");
            precisionlocation.setAttribute("geopointsrc", "GPS");
            detail.appendChild(precisionlocation);

            if (!oaInfoMap.isEmpty()) {
                // Log.d("CursorOnTargetSender", "oaInfoMap keySet: " + oaInfoMap.keySet().toString());
                // Log.d("CursorOnTargetSender", "oaInfoMap values: " + oaInfoMap.values().toString());
                Element openAthenaCalculationInfo = doc.createElement("openAthenaCalculationInfo");
                double theta = Math.toDegrees(Math.atan2(Double.parseDouble(le),Double.parseDouble(ce)));
                openAthenaCalculationInfo.setAttribute("raySlantAngleDeg", String.valueOf(theta));
                // add all the various calculation data from oaInfoMap to use for statistics/experimentation and debugging
                for (Map.Entry<String,String> entry : oaInfoMap.entrySet()) {
                    openAthenaCalculationInfo.setAttribute(entry.getKey(), entry.getValue());
                }
                detail.appendChild(openAthenaCalculationInfo);
            }

            Element remarks = doc.createElement("remarks");
            remarks.setTextContent("Generated by OpenAthena for Android from sUAS data");
            detail.appendChild(remarks);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("standalone", "yes");
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
//            Log.d("CursorOnTargetSender", writer.toString());
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Deliver the Cursor on Target message to all devices on the same network using UDP multicast
     * <p>
     *     The CoT message is sent to udp://239.2.3.1:6969, which causes the point ot show up on all devices on the same network running ATAK
     * </p>
     * @param xml String containing the built Cursor on Target message
     */
    private static void deliverUDP(String xml) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String message = xml;
                    InetAddress address = InetAddress.getByName("239.2.3.1");
                    int port = 6969;

                    if (mContext == null) {
                        throw new NullPointerException("CoT Sender invoker context was null");
                    }
                    WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                    WifiManager.MulticastLock lock = wifi.createMulticastLock("CoT multicast send lock");
                    lock.acquire();

                    MulticastSocket socket = new MulticastSocket();
                    DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), address, port);
                    socket.send(packet);
                    socket.close();
                    eventuid++;
                    saveUid();
                    lock.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Saves the target serial number to persistent storage so that target serial numbers remain unique
     */
    private static void saveUid() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putLong("eventuid", eventuid);
        prefsEditor.apply();
    }

    /**
     * Loads the target serial number from persistent storage
     */
    private static void loadUid() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (sharedPreferences != null) {
            eventuid = sharedPreferences.getLong("eventuid", 0);
        }
    }
}
