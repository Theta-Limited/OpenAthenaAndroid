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
import java.util.Locale;
import java.util.TimeZone;

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

    private static long eventuid = 0;
    private static Context mContext;

    static TimeZone tz = TimeZone.getTimeZone("UTC");
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static void sendCoT(Context invoker, double lat, double lon, double hae, double theta, String exif_datetime) {
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (sharedPreferences != null) {
            eventuid = sharedPreferences.getLong("eventuid", 0);
        }

        df.setTimeZone(tz);
        Date now = new Date();
        String nowAsISO = df.format(now);
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.MINUTE, 5);
        Date fiveMinsFromNow = cal.getTime();
        String fiveMinutesFromNowISO = df.format(fiveMinsFromNow);
        String imageISO = df.format(convert(exif_datetime));
        double linearError = 15.0d / 3.0d; // optimistic estimation of 1 sigma accuracy of altitude
        double circularError = 1.0d / Math.tan(Math.toRadians(theta)) * linearError; // optimistic estimation of 1 sigma accuracy based on angle of camera depression theta

        String le = Double.toString(linearError);
        String ce = Double.toString(circularError);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String uidString = "OpenAthena-" + getDeviceHostnameHash().substring(0,8) + "-" + Long.toString(eventuid);
//                String xmlString = buildCoT(uidString, imageISO, nowAsISO, fiveMinutesFromNowISO, Double.toString(lat), Double.toString(lon), ce, Double.toString(Math.round(hae)), le);
                String xmlString = buildCoT(uidString, imageISO, nowAsISO, fiveMinutesFromNowISO, Double.toString(lat), Double.toString(lon), ce, Double.toString(hae), le);
//                String dumxml = "<event uid=\"41414141\" type=\"a-u-G\" how=\"h-c\" start=\"2023-01-24T22:16:53Z\" time=\"2023-01-24T22:16:53Z\" stale=\"2023-01-25T22:06:53Z\"><point le=\"0\" ce=\"0\" hae=\"0\" lon=\"0\" lat=\"0\"/></event>";
//                dumxml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + dumxml;
                Log.d("CursorOnTargetSender", xmlString);
                deliverUDP(xmlString); // increments uid upon success
            }
        }).start();



    }

    /**
     * Converts an ExifInterface time and date tag into a Joda time format
     *
     * @param exif_tag_datetime
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

    public static String getDeviceHostnameHash() {
        InetAddress addr;
        String hash;
        try {
            addr = InetAddress.getLocalHost();
            String hostname = addr.getHostName();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(hostname.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            hash = sb.toString();
        } catch (UnknownHostException e) {
            hash = "unknown";
        } catch (NoSuchAlgorithmException e) {
            hash = "unknown";
        }
        return hash;
    }

    public static String buildCoT(String uid, String imageISO, String nowAsISO, String fiveMinutesFromNowISO, String lat, String lon, String ce, String hae, String le) {
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
            precisionlocation.setAttribute("altsrc", "DTED0");
            precisionlocation.setAttribute("geopointsrc", "GPS");
            detail.appendChild(precisionlocation);

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

    private static void saveUid() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putLong("eventuid", eventuid);
        prefsEditor.apply();
    }
}
