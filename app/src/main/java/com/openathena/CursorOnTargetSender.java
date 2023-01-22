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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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

    public void sendCoT(double lat, double lon, double hae, String exif_datetime) {
        String nowAsISO = df.format(new Date());
    }

    /**
     * Converts an ExifInterface time and date tag into a Joda time format
     *
     * @param EXIF_TAG_DATETIME
     * @return null in case of failure, the date object otherwise
     */
    public static LocalDateTime convert(String EXIF_TAG_DATETIME){
        // EXIF tag contains no time zone data, assume it is same as local time
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault()); // default locale of device at application start

//        try {
//            return new LocalDateTime( simpleDateFormat.parse( EXIF_TAG_DATETIME ) );
//        } catch (ParseException e) {
//            return null;
//        }
        return null;
    }

}
