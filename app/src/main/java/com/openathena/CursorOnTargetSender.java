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

import android.util.Log;
import android.util.Xml;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class CursorOnTargetSender {

    private static long eventuid = 0;

    static TimeZone tz = TimeZone.getTimeZone("UTC");
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    public static void sendCoT(double lat, double lon, double hae, double theta, String exif_datetime) {
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
        double circularError = Math.round((1.0d / Math.tan(theta)) * linearError); // optimistic estimation of 1 sigma accuracy based on angle of camera depression theta

        String le = Double.toString(linearError);
        String ce = Double.toString(circularError);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String uidString = "OpenAthena-" + getDeviceHostnameHash() + "-" + Long.toString(eventuid);
//                String xmlString = buildCoT(uidString, imageISO, nowAsISO, fiveMinutesFromNowISO, Double.toString(lat), Double.toString(lon), ce, Double.toString(Math.round(hae)), le);
                String xmlString = buildCoT("7", nowAsISO, nowAsISO, fiveMinutesFromNowISO, "0.0", "0.0", ce, Double.toString(Math.round(hae)), le);

                deliverUDP(xmlString);
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
            root.setAttribute("type", "a-u-G");
            root.setAttribute("time", imageISO);
            root.setAttribute("start", nowAsISO);
            root.setAttribute("stale", fiveMinutesFromNowISO);
            doc.appendChild(root);

            Element detail = doc.createElement("detail");
            root.appendChild(detail);

            Element status = doc.createElement("status");
            status.setAttribute("readiness", "true");
            detail.appendChild(status);

//            Element remarks = doc.createElement("remarks");
//            remarks.setAttribute("how", "Generated by OpenAthena for Android");
//            detail.appendChild(remarks);

            Element point = doc.createElement("point");
            point.setAttribute("lat", lat);
            point.setAttribute("lon", lon);
            point.setAttribute("ce", ce);
            point.setAttribute("hae", hae);
            point.setAttribute("le", le);
            root.appendChild(point);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("standalone", "yes");
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            Log.d("CursorOnTargetSender", writer.toString());
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
                    InetAddress address = InetAddress.getByName("127.0.0.1");
                    int port = 6969;

                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), address, port);
                    socket.send(packet);
                    socket.close();
                    eventuid++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
