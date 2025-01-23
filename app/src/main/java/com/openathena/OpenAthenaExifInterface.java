package com.openathena;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class OpenAthenaExifInterface extends ExifInterface {
    private static final String TAG = "OpenAthenaExifInterface";

    public OpenAthenaExifInterface(@NonNull File file) throws IOException {
        super(file);
    }
    public OpenAthenaExifInterface(@NonNull String filename) throws IOException {
        super(filename);
    }
    public OpenAthenaExifInterface(@NonNull FileDescriptor fileDescriptor) throws IOException {
        super(fileDescriptor);
    }
    public OpenAthenaExifInterface(@NonNull InputStream inputStream) throws IOException {
        super(inputStream);
    }
    public OpenAthenaExifInterface(@NonNull InputStream inputStream, @ExifStreamType int streamType) throws IOException {
        super(inputStream,streamType);
    }

    @Nullable
    @Override
    /**
     * Overrides ExifInterface getAttribute. If desired attribute is Exif make and the tag is just "Camera" grab the value from XMP tiff:Make tag instead
     * <p>
     *     This is a workaround for certain Autel Robotics drones which ncorrectly set their EXIF make to just "Camera" even though the XMP tiff:Make correctly indicates "Autel Robotics"
     * </p>
     * @param tag the name of the tag.
     * @return super.getAttribute(tag) if tag != ExifInterface.TAG_MAKE. Otherwise, return XMP make tag instead of EXIF if EXIF make is "Camera"
     */
    public String getAttribute(@NonNull String tag) {
        if (tag != ExifInterface.TAG_MAKE) {
            return super.getAttribute(tag);
        }
        String exif_make = super.getAttribute(tag);
        if (exif_make == null) return null;
        if (exif_make.trim().equalsIgnoreCase("camera")) {
            String xmp_str = this.getAttribute(ExifInterface.TAG_XMP);
            if (xmp_str == null || xmp_str.trim().isEmpty()) return null;
            //Log.d(TAG, "xmp_str: " + xmp_str);
            //Log.d(TAG, "EXIF make was: " + make);
            if (!xmp_str.trim().isEmpty()) {
                XMPMeta xmpMeta = null;
                try {
                    xmpMeta = XMPMetaFactory.parseFromString(xmp_str.trim());
                    String schemaNS = "http://ns.adobe.com/tiff/1.0/";
                    String xmp_make = xmpMeta.getPropertyString(schemaNS,"Make");
                    return xmp_make;
                } catch (XMPException e) {
                    return null;
                }
            }
        }

        return exif_make;
    }
}
