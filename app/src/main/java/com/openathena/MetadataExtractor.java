package com.openathena;

import android.content.Context;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import com.adobe.xmp.XMPError;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MetadataExtractor {
    private static final String TAG = "MetadataExtractor";
    private static MainActivity parent;

    private static HashMap<String, HashMap> mfnMap = new HashMap<String, HashMap>();

    protected MetadataExtractor(MainActivity caller) {
        super();
        parent = caller;
        genCCDMap();
    }

    /**
     * Generates a nested map which can be indexed into by make, then model
     * The double[2] stored in the map represents the width and height, in mm, of a pixel
     * on the device's specific ccd/cmos sensor.
     *
     * This is used for intrinsics calculation, and allows the focal length to be converted from mm to pixel units
     * <p>
     *     See also:
     *     https://towardsdatascience.com/camera-intrinsic-matrix-with-example-in-python-d79bf2478c12
     *     https://www.djzphoto.com/blog/2018/12/5/dji-drone-quick-specs-amp-comparison-page
     *     https://support.skydio.com/hc/en-us/articles/5338292379803-Comparing-Skydio-2-and-Skydio-2-
     *     https://commonlands.com/blogs/technical/cmos-sensor-size
     *
     * </p>
     */
    private static void genCCDMap() {
        HashMap<String, double[]> djiMap = new HashMap<String, double[]>();

        // DJI Mavic 2 Zoom
        //     ccd_width(mm) / width_pixels(pixels) = pixel_width(mm/pixel) ...
        djiMap.put("FC2204", new double[]{6.26d/4000.0d, 4.7d/3000.0d, 4000.0d, 3000.0d});

        mfnMap.put("DJI", djiMap);

        HashMap<String, double[]> skydioMap = new HashMap<String, double[]>();

        // Skydio 2 and 2+
        //     ccd_width(mm) / width_pixels(pixels) = pixel_width(mm/pixel) ...
        skydioMap.put("2", new double[]{6.26d/4056.0d, 4.7d/3040.0d, 4056.0d, 3040.0d});
        skydioMap.put("2+", skydioMap.get("2"));

        mfnMap.put("SKYDIO", skydioMap);
    }

    public static double[] getMetadataValues(ExifInterface exif) throws XMPException, MissingDataException {
        if (exif == null) {
            Log.e(TAG, "ERROR: getMetadataValues failed, ExifInterface was null");
            throw new IllegalArgumentException("ERROR: getMetadataValues failed, exif was null");
        }
        String make = exif.getAttribute(ExifInterface.TAG_MAKE);
        String model = exif.getAttribute(ExifInterface.TAG_MODEL);
        if (make == null || make.equals("")) {
            return null;
        }
        make = make.toUpperCase();
        model = model.toUpperCase();
        switch(make) {
            case "DJI":
                return handleDJI(exif);
            //break;
            case "SKYDIO":
                return handleSKYDIO(exif);
            //break;
            case "AUTEL ROBOTICS":
                parent.displayAutelAlert();
                return handleAUTEL(exif);
            //break;
            case "PARROT":
                if (model.contains("ANAFI")) {
                    return handlePARROT(exif);
                } else {
                    Log.e(TAG, "ERROR: Parrot model " + model + " not usable at this time");
                    throw new XMPException(parent.getString(R.string.parrot_model_prefix_error_msg) + model + parent.getString(R.string.not_usable_at_this_time_error_msg), XMPError.BADVALUE);
                }
                //break;
            default:
                Log.e(TAG, parent.getString(R.string.make_prefix_error_msg) + " " + make + " " + parent.getString(R.string.not_usable_at_this_time_error_msg));
                throw new XMPException(parent.getString(R.string.make_prefix_error_msg) + " " + make + " " + parent.getString(R.string.not_usable_at_this_time_error_msg), XMPError.BADXMP);
        }
    }

    public static double[] handleDJI(ExifInterface exif) throws XMPException, MissingDataException{
        String xmp_str = exif.getAttribute(ExifInterface.TAG_XMP);
        if (xmp_str == null) {
            throw new MissingDataException(parent.getString(R.string.xmp_missing_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        } if (xmp_str.trim().equals("")) {
            throw new MissingDataException(parent.getString(R.string.xmp_empty_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        }
        Log.i(TAG, "xmp_str for Make DJI: " + xmp_str);
        XMPMeta xmpMeta = XMPMetaFactory.parseFromString(xmp_str.trim());

        String schemaNS = "http://www.dji.com/drone-dji/1.0/";
        String latitude = xmpMeta.getPropertyString(schemaNS, "GpsLatitude");
        double y;
        if (latitude != null) {
            y = Double.parseDouble(latitude);
        } else {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_latitude_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.LATITUDE);
        }
        String longitude = xmpMeta.getPropertyString(schemaNS, "GpsLongitude");
        if (longitude == null || longitude.equals("")) {
            // handle a typo "GpsLongtitude" that occurs in certain versions of Autel drone firmware (which use drone-dji metadata format)
            longitude = xmpMeta.getPropertyString(schemaNS, "GpsLong" + "t" + "itude");
            if (longitude == null || longitude.equals("")) {
                throw new MissingDataException(parent.getString(R.string.missing_data_exception_longitude_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.LATITUDE);
            }
        }
        double x = Double.parseDouble(longitude);

        double z;
        String altitude = xmpMeta.getPropertyString(schemaNS, "AbsoluteAltitude");
        if (altitude != null) {
            z = Double.parseDouble(altitude);
        } else {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_altitude_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALTITUDE);
        }

        double azimuth;
        String gimbalYawDegree = xmpMeta.getPropertyString(schemaNS, "GimbalYawDegree");
        if (gimbalYawDegree != null) {
            azimuth = Double.parseDouble(gimbalYawDegree);
            azimuth = azimuth % 360.0d;
        } else {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_azimuth_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.AZIMUTH);
        }

        double theta;
        String gimbalPitchDegree = xmpMeta.getPropertyString(schemaNS, "GimbalPitchDegree");
        if (gimbalPitchDegree != null) {
            theta = Math.abs(Double.parseDouble(gimbalPitchDegree));
        } else {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_theta_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.THETA);
        }

        // safety check: if metadata azimuth and theta are zero, it's extremely likely the metadata is invalid
        if (Math.abs(Double.compare(azimuth, 0.0d)) <= 0.001d && Math.abs(Double.compare(theta, 0.0d)) <= 0.001d) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_altitude_and_theta_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.THETA);
        }

        double[] outArr = new double[]{y, x, z, azimuth, theta};
        return outArr;
    }

    public static double[] handleSKYDIO(ExifInterface exif) throws XMPException, MissingDataException {
        String xmp_str = exif.getAttribute(ExifInterface.TAG_XMP);
        if (xmp_str == null) {
            throw new MissingDataException(parent.getString(R.string.xmp_missing_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        } if (xmp_str.trim().equals("")) {
            throw new MissingDataException(parent.getString(R.string.xmp_empty_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        }
        Log.i(TAG, "xmp_str for Make SKYDIO: " + xmp_str);
        XMPMeta xmpMeta = XMPMetaFactory.parseFromString(xmp_str.trim());
        String schemaNS = "https://www.skydio.com/drone-skydio/1.0/";

        double y; double x; double z; double azimuth; double theta;

        try {
            y = Double.parseDouble(xmpMeta.getPropertyString(schemaNS, "Latitude"));
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_latitude_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.LATITUDE);
        }

        try {
            x = Double.parseDouble(xmpMeta.getPropertyString(schemaNS, "Longitude"));
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_longitude_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.LONGITUDE);
        }

        try {
            z = Double.parseDouble(xmpMeta.getPropertyString(schemaNS, "AbsoluteAltitude"));
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_altitude_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALTITUDE);
        }

        try {
            azimuth = Double.parseDouble(xmpMeta.getStructField(schemaNS, "CameraOrientationNED", schemaNS, "Yaw").getValue());
            azimuth = azimuth % 360.0d;
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_altitude_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.AZIMUTH);
        }

        try {
            theta = Double.parseDouble(xmpMeta.getStructField(schemaNS, "CameraOrientationNED", schemaNS, "Pitch").getValue());
            theta = Math.abs(theta);
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_theta_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.THETA);
        }

        double[] outArr = new double[]{y, x, z, azimuth, theta};
        return outArr;
    }

    public static double[] handleAUTEL(ExifInterface exif) throws XMPException, MissingDataException{
        String xmp_str = exif.getAttribute(ExifInterface.TAG_XMP);
        if (xmp_str == null) {
            throw new MissingDataException(parent.getString(R.string.xmp_missing_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        } if (xmp_str.trim().equals("")) {
            throw new MissingDataException(parent.getString(R.string.xmp_empty_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        }
        Log.i(TAG, "xmp_str for Make AUTEL: " + xmp_str);
        XMPMeta xmpMeta = XMPMetaFactory.parseFromString(xmp_str.trim());

        boolean isNewMetadataFormat;
        int aboutIndex = xmp_str.indexOf("rdf:about=");
        String rdf_about = xmp_str.substring(aboutIndex + 10, aboutIndex + 24); // not perfect, should be fine though
        Log.d(TAG, "rdf_about: " + rdf_about);

        if (!rdf_about.toLowerCase().contains("autel")) {
            isNewMetadataFormat = true;
        } else {
            isNewMetadataFormat = false;
        }

        double y;
        double x;
        double z;
        double azimuth;
        double theta;

        if (isNewMetadataFormat) {
            // Newer metadata uses the same format and schemaNS as DJI
            return handleDJI(exif);
        } else {
            Float[] yxz = exifGetYXZ(exif);
            y = yxz[0];
            x = yxz[1];
            z = yxz[2];

            String schemaNS = "http://pix4d.com/camera/1.0";

            try {
                azimuth = Double.parseDouble(xmpMeta.getPropertyString(schemaNS, "Yaw"));
                azimuth = azimuth % 360.0d;
            } catch (NumberFormatException nfe) {
                throw new MissingDataException(parent.getString(R.string.missing_data_exception_azimuth_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.AZIMUTH);
            }

            try {
                theta = Double.parseDouble(xmpMeta.getPropertyString(schemaNS, "Pitch"));
            } catch (NumberFormatException nfe) {
                throw new MissingDataException(parent.getString(R.string.missing_data_exception_theta_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.THETA);
            }
            // AUTEL old firmware Camera pitch 0 is down, 90 is forwards towards horizon
            // so, we use the complement of the angle instead
            // see: https://support.pix4d.com/hc/en-us/articles/202558969-Yaw-Pitch-Roll-and-Omega-Phi-Kappa-angles
            theta = 90.0d - theta;
            double[] outArr = new double[]{y, x, z, azimuth, theta};
            return outArr;
        }
    }

    public static double[] handlePARROT(ExifInterface exif) throws XMPException, MissingDataException{
        double y;
        double x;
        double z;
        double azimuth;
        double theta;

        Float[] yxz = exifGetYXZ(exif);
        y = yxz[0];
        x = yxz[1];
        z = yxz[2];

        String xmp_str = exif.getAttribute(ExifInterface.TAG_XMP);
        if (xmp_str == null) {
            throw new MissingDataException(parent.getString(R.string.xmp_missing_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        } if (xmp_str.trim().equals("")) {
            throw new MissingDataException(parent.getString(R.string.xmp_empty_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        }
        Log.i(TAG, "xmp_str for Make PARROT: " + xmp_str);
        XMPMeta xmpMeta = XMPMetaFactory.parseFromString(xmp_str.trim());

        String schemaNS = "http://www.parrot.com/drone-parrot/1.0/";

        try {
            azimuth = Double.parseDouble(xmpMeta.getPropertyString(schemaNS, "CameraYawDegree"));
            azimuth = azimuth % 360.0d;
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_azimuth_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.AZIMUTH);
        }

        try {
            theta = Math.abs(Double.parseDouble(xmpMeta.getPropertyString(schemaNS, "CameraPitchDegree")));
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_theta_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.THETA);
        }

        double[] outArr = new double[]{y, x, z, azimuth, theta};
        return outArr;
    }

    // http://android-er.blogspot.com/2009/12/read-exif-information-in-jpeg-file.html
    public static String getTagString(String tag, ExifInterface exif)
    {
        return(tag + " : " + exif.getAttribute(tag) + "\n");
    }

    public static Float[] exifGetYXZ(ExifInterface exif) throws MissingDataException
    {
        String latDir = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String latRaw = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        if (latDir == null || latRaw == null) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_latitude_error_msg), MissingDataException.dataSources.EXIF, MissingDataException.missingValues.LATITUDE);
        }
        String lonDir = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        String lonRaw = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        if (lonDir == null || lonRaw == null) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_longitude_error_msg), MissingDataException.dataSources.EXIF, MissingDataException.missingValues.LATITUDE);
        }

        String alt = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
        if (alt == null) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_altitude_error_msg), MissingDataException.dataSources.EXIF, MissingDataException.missingValues.ALTITUDE);
        }

        latDir = latDir.toUpperCase();
        String[] latArr = latRaw.split(",", 3);
        lonDir = lonDir.toUpperCase();
        String[] lonArr = lonRaw.split(",", 3);

        float y = 0.0f;
        y += rationalToFloat(latArr[0]);
        y += rationalToFloat(latArr[1]) / 60.0f;
        y += rationalToFloat(latArr[2]) / 3600.0f;
        if (latDir.equals("S"))
        {
            y = y * -1.0f;
        }

        float x = 0.0f;
        x += rationalToFloat(lonArr[0]);
        x += rationalToFloat(lonArr[1]) / 60.0f;
        x += rationalToFloat(lonArr[2]) / 3600.0f;
        if (lonDir.equals("W"))
        {
            x = x * -1.0f;
        }

        float z = rationalToFloat(alt);

        Float[] arrOut = {y, x, z};
        return(arrOut);
    }

    public static double[] getIntrinsicMatrixFromExif(ExifInterface exif) throws Exception {
        double[] intrinsicMatrix = new double[9];
        String make = exif.getAttribute(ExifInterface.TAG_MAKE);
        String model = exif.getAttribute(ExifInterface.TAG_MODEL);
        make = make.toUpperCase();
        model = model.toUpperCase();

        HashMap<String, double[]> mfn = mfnMap.get(make);
        if (mfn != null) {
            double[] pixelDimensions = mfn.get(model);
            if (pixelDimensions != null) {
                Log.i(TAG, "found pixel dimensions (mm) from table lookup: " + pixelDimensions[0] + ", " + pixelDimensions[1]);
                return getIntrinsicMatrixFromKnownCCD(exif, pixelDimensions);
            } else {
                return getIntrinsicMatrixFromExif35mm(exif);
            }
        } else {
            return getIntrinsicMatrixFromExif35mm(exif);
        }
    }

    protected static double[] getIntrinsicMatrixFromKnownCCD(ExifInterface exif, double[] pixelDimensions) throws Exception {
        if (exif == null) {
            throw new IllegalArgumentException("Failed to get intrinsics, ExifInterface was null!");
        }
        if (pixelDimensions == null) {
            Log.e(TAG, "Failed to calculate intrinsics, ccdDimensions was null!");
            Log.e(TAG, "Warning: reverting to calc from 35mm mode");
            return getIntrinsicMatrixFromExif35mm(exif);
        }
        if (pixelDimensions.length < 2) {
            Log.e(TAG, "Failed to calculate intrinsics, ccdDimensions was invalid!");
            Log.e(TAG, "Warning: reverting to calc from 35mm mode");
            return getIntrinsicMatrixFromExif35mm(exif);
        }

        String focalRational = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
        if (focalRational == null || focalRational.equals("")) {
            Log.e(TAG, "Failed to calculate intrinsics, focal length was missing or invalid!");
            Log.e(TAG, "Warning: reverting to calc from 35mm mode");
            return getIntrinsicMatrixFromExif35mm(exif);
        }

        double focalLength = rationalToFloat(focalRational);
        if (focalLength == -1.0d || focalLength == 0.0d) {
            throw new Exception("focal length could not be determined");
        }

        String digitalZoomRational = exif.getAttribute(ExifInterface.TAG_DIGITAL_ZOOM_RATIO);
        if (digitalZoomRational != null && !digitalZoomRational.equals("")) {
            float digitalZoomRatio = rationalToFloat(exif.getAttribute(ExifInterface.TAG_DIGITAL_ZOOM_RATIO));
            if (Math.abs(digitalZoomRatio) > 0.000f && Math.abs(digitalZoomRatio - 1.0f) > 0.000f) {
                throw new Exception("digital zoom detected. Not supported in this version");
            }
        }

        double mmWidthPerPixel = pixelDimensions[0];
        double mmHeightPerPixel = pixelDimensions[1];
        double pixelAspectRatio = mmWidthPerPixel / mmHeightPerPixel;
        double ccdPixelWidth = pixelDimensions[2];
        double ccdPixelHeight = pixelDimensions[3];

        double imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
        double imageHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1); // Image Height
        if (imageWidth <= 0.0d || imageHeight <= 0.0d) {
            throw new Exception("could not determine width and height of image!");
        }

        double scaleRatio = imageWidth / ccdPixelWidth;

        double alpha_x = focalLength / mmWidthPerPixel; // focal length in pixel units
        alpha_x = alpha_x * scaleRatio; // scale down if image is scaled down
        double alpha_y = alpha_x / pixelAspectRatio; // focal length equivalent in pixel units, for the homogenous y axis in the image frame
        alpha_y = alpha_y * scaleRatio; // use the x-axis scale ratio because y axis is often cropped (and not EXIF tagged appropriately) and x axis usually isn't

        double[] intrinsicMatrix = new double[9];
        intrinsicMatrix[0] = alpha_x;
        intrinsicMatrix[1] = 0.0d; // gamma, the skew coefficient between the x and the y axis, and is often 0.
        intrinsicMatrix[2] = imageWidth / 2.0d; // cx
        intrinsicMatrix[3] = 0.0d;
        intrinsicMatrix[4] = alpha_y;
        intrinsicMatrix[5] = imageHeight / 2.0d; // cy
        intrinsicMatrix[6] = 0.0d;
        intrinsicMatrix[7] = 0.0d;
        intrinsicMatrix[8] = 1.0d;

        return intrinsicMatrix;
    }

    protected static double[] getIntrinsicMatrixFromExif35mm(ExifInterface exif) throws Exception{
        if (exif == null) {
            throw new IllegalArgumentException("Failed to get intrinsics, ExifInterface was null!");
        }

        double[] intrinsicMatrix = new double[9];

        double focalLength35mmEquiv = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, -1.0d);

        if (focalLength35mmEquiv == -1.0d || focalLength35mmEquiv == 0.0d) {
            throw new Exception("focal length could not be determined");
        }

        String digitalZoomRational = exif.getAttribute(ExifInterface.TAG_DIGITAL_ZOOM_RATIO);
        if (digitalZoomRational != null && !digitalZoomRational.equals("")) {
            float digitalZoomRatio = rationalToFloat(exif.getAttribute(ExifInterface.TAG_DIGITAL_ZOOM_RATIO));
            if (Math.abs(digitalZoomRatio) > 0.000f && Math.abs(digitalZoomRatio - 1.0f) > 0.000f) {
                throw new Exception("digital zoom detected. Not supported in this version");
            }
        }

        double imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
        double imageHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0); // Image Height
        if (imageWidth <= 0.0d || imageHeight <= 0.0d) {
            throw new Exception("could not determine width and height of image!");
        }

        // calculate aspect ratio
//        double aspectRatio = imageWidth / imageHeight; // This will be WRONG if the image is auto-cropped, which is commonly done to make a 16:9 picture from a 4:3 sensor
        double aspectRatio = 4.0d/3.0d; // This will be WRONG if the sensor is not 4:3! e.g: APS-C or Full-frame 3:2

        // Calculate the intrinsic matrix elements
        double alpha_x = imageWidth * focalLength35mmEquiv / 36.0d; // focal length, in pixel units
        intrinsicMatrix[0] = alpha_x;

        intrinsicMatrix[1] = 0.0f; // gamma, the skew coefficient between the x and the y axis, and is often 0.

        double alpha_y = alpha_x / aspectRatio;

        intrinsicMatrix[4] = alpha_y;

        // principal point
        intrinsicMatrix[2] = imageWidth / 2.0d; // cx
        intrinsicMatrix[3] = 0.0d;
        intrinsicMatrix[5] = imageHeight / 2.0d; // cy
        intrinsicMatrix[6] = 0.0d;
        intrinsicMatrix[7] = 0.0d;
        intrinsicMatrix[8] = 1.0d;

        return intrinsicMatrix;
    }

    public static double[] getRayAnglesFromImgPixel(int x, int y, ExifInterface exifInterface) throws Exception {
        double[] intrinsics = getIntrinsicMatrixFromExif(exifInterface); // may throw Exception

        double fx = intrinsics[0];
        double fy = intrinsics[4];
        double cx = intrinsics[2];
        double cy = intrinsics[5];

        // calculate ray angles
        double pixelX = x - cx;
        double pixelY = y - cy;

        double azimuth = Math.atan2(pixelX, fx);
        double elevation = Math.atan2(pixelY, fy);

        azimuth = Math.toDegrees(azimuth);
        elevation = Math.toDegrees(elevation);

        Log.d(TAG, "Pixel (" + x + ", " + y + ") -> Ray (" + azimuth + ", " + elevation + ")");
        return new double[] {azimuth, elevation};
    }

    public static float rationalToFloat(String str)
    {
        if (str == null) {
            return 0.0f;
        }
        String[] split = str.split("/", 2);
        float numerator = Float.parseFloat(split[0]);
        float denominator;
        if (split.length > 1) {
            denominator = Float.parseFloat(split[1]);
        } else {
            denominator = 1.0f;
        }
        return numerator / denominator;
    }
}
