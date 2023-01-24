package com.openathena;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

public class CursorOnTargetSender {

    private static long eventuid = 0;

    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    CursorOnTargetSender() {
        df.setTimeZone(tz);
    }

    public void sendCoT(double lat, double lon, double hae, double theta, String exif_datetime) {
        Date now = new Date();
        String nowAsISO = df.format(now);
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.MINUTE, 5);
        Date fiveMinsFromNow = cal.getTime();
        String fiveMinutesFromNowISO = df.format(fiveMinsFromNow);
        String imageISO = df.format(convert(exif_datetime));
        double linearError = 15.0d / 3.0d; // optimistic estimation of 1 sigma accuracy of altitude
        double circularError = (1.0d / Math.tan(theta)) * linearError; // optimistic estimation of 1 sigma accuracy based on angle of camera depression theta
        String le = Double.toString(linearError);
        String ce = Double.toString(circularError);
        String uidString = "OpenAthena-" + getDeviceHostnameHash() + "-" + Long.toString(eventuid);
        
        String xmlString = buildCoT(uidString, imageISO, nowAsISO, fiveMinutesFromNowISO, Double.toString(lat), Double.toString(lon), ce, Double.toString(hae), le);
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

    public String getDeviceHostnameHash() {
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

    public String buildCoT(String uid, String imageISO, String nowAsISO, String fiveMinutesFromNowISO, String lat, String lon, String ce, String hae, String le) {
        String xml = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            //root elements
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("event");
            rootElement.setAttribute("version", "2.0");
            rootElement.setAttribute("uid", uid);
            rootElement.setAttribute("type", "a-h-A-M-F-U-M");
            rootElement.setAttribute("time", imageISO);
            rootElement.setAttribute("start", nowAsISO);
            rootElement.setAttribute("stale", fiveMinutesFromNowISO);
            doc.appendChild(rootElement);

            Element detail = doc.createElement("detail");
            rootElement.appendChild(detail);

            Element point = doc.createElement("point");
            point.setAttribute("lat", lat);
            point.setAttribute("lon", lon);
            point.setAttribute("ce", ce);
            point.setAttribute("hae", hae);
            point.setAttribute("le", le);
            rootElement.appendChild(point);

            // Transform the document to an XML string
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            xml = writer.toString();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }

        return xml;
    }

}
