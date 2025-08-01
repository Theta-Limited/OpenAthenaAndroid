package com.openathena;
import static com.openathena.TargetGetter.degNormalize;

import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import com.adobe.xmp.XMPError;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

class Version implements Comparable<Version> {

    private final String version;

    public final String get() {
        return this.version;
    }

    /**
     * Constructs a new Version object
     * @param version String containing a valid semantic version number
     */
    public Version(String version) {
        if (version == null)
            throw new IllegalArgumentException("Version can not be null");
        if (!version.matches("[0-9]+(\\.[0-9]+)*"))
            throw new IllegalArgumentException("Invalid version format");
        this.version = version;
    }

    /**
     * Compares this version to another version based on semantic version number
     * @param that other version to compareTo
     * @return int 1 if this version is greater, 0 if equal, -1 if less
     */
    @Override
    public int compareTo(Version that) {
        if (that == null)
            return 1;
        String[] thisParts = this.get().split("\\.");
        String[] thatParts = that.get().split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            if (thisPart < thatPart)
                return -1;
            if (thisPart > thatPart)
                return 1;
        }
        return 0;
    }
}

public class MetadataExtractor {
    private static final String TAG = "MetadataExtractor";
    private static MainActivity parent;

    //private static HashMap<String, HashMap> mfnMaps = new HashMap<String, HashMap>();

    protected static EGMOffsetProvider offsetProvider = new EGM96OffsetAdapter();
    public static DroneParametersFromJSON parameterProvider;

    // Some older DJI models (e.g. the Mavic 2 Zoom) use EGM96 (height above mean sea level) as vertical datum instead of WGS84 (height above ellipsoid)
    // If vertical datum is EGM96, convert it to WGS84 for calculation within OpenAthena by adding the Geoid offset
    // Wild guess, will treat DJI models which came out before the Mavic 3 as EGM96
    // see: https://www.djzphoto.com/blog/dji-product-history-timeline-drones-cameras-gimbals
    // see: https://github.com/Theta-Limited/DroneModels/blob/main/droneModels.json
    //private static final Set<String> EGM96_DRONE_MODELS = new HashSet<>(Arrays.asList("FC200", "FC220", "FC230", "PHANTOM VISION FC200", "FC300X", "FC300XW", "FC300S", "FC300C", "FC330", "FC6510", "FC550", "FC550RAW", "FC550R", "FC6520", "FC6540", "ZENMUSEH20", "ZENMUSEH20T", "ZENMUSEH20N", "ZH20N", "ZH20", "ZH20T", "ZENMUSEH20W", "djiZH20W", "ZH20W", "FC6310", "FC6310S", "FC6360", "FC7203", "FC7303", "FC2103", "FC2200", "FC2204", "FC2403", "MAVIC2-ENTERPRISE-ADVANCED", "M2EA", "L1D-20C", "ZENMUSEP1", "ZP1", "ZENMUSEEXT2", "XT2", "FLIR", "XT S", "ZENMUSEZ30", "Z30", "FC1102", "FC3170", "FC3411"));

    // Despite providing an AbsoluteAltitude tag value, some older DJI drones actually only provide
    // relative altitude from their launch point. Without an absolute vertical reference,
    // there is no way to provide accurate target calculations from such drones
    // This list may be incomplete, encourage you to add additional drone models if you encounter such issue!
    private static final Set<String> DJI_MODELS_RELATIVE_ALTITUDE_ONLY_BLACKLIST = new HashSet<>(Arrays.asList("FC230", "FC330", "FC200", "PHANTOM VISION FC200", "FC7203", "FC7303", "FC3682", "FC3170", "FC3411", "FC1102", "HG310", "HG310Z"));

    private static final Version djiFirmwareVerticalDatumWasSwitched = new Version("1.5");

    // Base estimation of Target Location Error (TLE) due to GPS innaccuracy, used if no calibrated value is present for a particular drone model
    private static final double TLE_MODEL_DEFAULT_Y_INTERCEPT = 5.25d;
    // Base estimation of increase in TLE per meter of slant range, used if no calibrated value is present for a particular drone model
    private static final double TLE_MODEL_DEFAULT_SLANT_RANGE_COEFFICIENT = 0.026d;

    protected MetadataExtractor(MainActivity caller) {
        super();
        parent = caller;
        parameterProvider = new DroneParametersFromJSON(parent.getApplicationContext());
        // If the user-configured droneModels.json is invalid,
        //     load the version bundled with the application instead
        if(!parameterProvider.isDroneArrayValid()) {
            parameterProvider.loadJSONFromAsset();
        }
    }

    /**
     * Returns true if and only if drone's camera is a known model
     * @param exif exif of an image to analyze for make and model
     * @return true if the make and model is a known model
     */
    public static boolean isDroneModelRecognized(OpenAthenaExifInterface exif) {
        String make = exif.getAttribute(ExifInterface.TAG_MAKE);
        String model = exif.getAttribute(ExifInterface.TAG_MODEL);
        return (parameterProvider.getMatchingDrones(make, model).length() > 0);
    }

    /**
     * Returns true if and only if Target Location Error (TLE) estimation model parameters are available for the drone
     * @param exif exif of an image to analyze for make and model
     * @return true if and only if the drone's TLE model parameters are available
     */
    public static boolean areTLEModelValuesAvailable(OpenAthenaExifInterface exif) {
        JSONObject drone = getMatchingDrone(exif);
        try {
            double y_intercept = drone.getDouble("tle_model_y_intercept");
            double meters_error_per_meter_distance = drone.getDouble("tle_model_slant_range_coeff");
        } catch (JSONException | NullPointerException e) {
            return false;
        }
        return true;
    }

    /**
     * Returns the lensType of a given drone make/model from droneModels.json database
     * @param exif exif of an image to analyze for make and model
     * @return String lensType either "perspective" or "fisheye", or "unknown"
     */
    public static String getLensType(OpenAthenaExifInterface exif) {
        JSONObject drone = getMatchingDrone(exif);
        String lensType;
        try {
            lensType = drone.getString("lensType");
            if (!(lensType.equalsIgnoreCase("perspective") || lensType.equalsIgnoreCase("fisheye"))) {
                return "unknown";
            }
        } catch (JSONException | NullPointerException e) {
            return "unknown";
        }
        return lensType;
    }

    /**
     * Returns true if and only if the drone image is from a thermal camera
     * <p>
     *     If there is a make/model name collision between a thermal camera and a color camera,
     *     match to the correct camera based on its pixel width according to geometric distance from expected value
     * </p>
     * @param exif exif of a drone image to analyze
     * @return true if and only if the drone image is from a thermal camera
     */
    public static boolean isThermal(OpenAthenaExifInterface exif) {
        JSONObject drone = getMatchingDrone(exif);
        boolean isThermal = false;
        try {
            String jsonBoolean = drone.getString("isThermal");
            if (jsonBoolean.equalsIgnoreCase("true")) {
                isThermal = true;
            }
        } catch (JSONException | NullPointerException e) {
            assert(true); // do nothing
        }

        return isThermal;
    }

    /**
     * Returns the y-intercept (in meters) of the Target Location Error linear model for the given drone
     * @param exif exif of a drone image to analyze and match to entry in droneModels.json
     * @return double y-intercept (in meters) of the Target Location Error linear model for the given drone
     */
    public static double getTLEModelYIntercept(OpenAthenaExifInterface exif) {
        JSONObject drone = getMatchingDrone(exif);
        double y_intercept = TLE_MODEL_DEFAULT_Y_INTERCEPT;
        try {
            y_intercept = drone.getDouble("tle_model_y_intercept");
        } catch (JSONException | NullPointerException e) {
            Log.i(TAG, "No tle_model_y_intercept value found for this drone model, using default: " + TLE_MODEL_DEFAULT_Y_INTERCEPT + "m");
        }
        return y_intercept;
    }

    /**
     * Returns the slant range coefficient (in meters error per additional meter distance) of the Target Location Error linear model for the given drone
     * @param exif exif of a drone image to analyze and match to entry in droneModels.json
     * @return double slant range coefficient (in meters error per additional meter distance) of the Target Location Error linear model for the given drone
     */
    public static double getTLEModelSlantRangeCoefficient(OpenAthenaExifInterface exif) {
        JSONObject drone = getMatchingDrone(exif);
        double meters_error_per_meter_distance = TLE_MODEL_DEFAULT_SLANT_RANGE_COEFFICIENT;
        try {
            meters_error_per_meter_distance = drone.getDouble("tle_model_slant_range_coeff");
        } catch (JSONException | NullPointerException e) {
            Log.i(TAG, "No tle_model_slant_range_coeff value found for this drone model, using default: " + TLE_MODEL_DEFAULT_SLANT_RANGE_COEFFICIENT + "meters per meter");
        }
        return meters_error_per_meter_distance;
    }

    /**
     * Returns a TLE_Model_Parameters object containing the parameters of the Target Location Error linear model for the given drone
     * @param exif exif of a drone image to analyze and match to entry in droneModels.json
     * @return TLE_Model_Parameters object containing the parameters of the Target Location Error linear model for the given drone (or default values)
     */
    public static TLE_Model_Parameters getTLEModelParameters(OpenAthenaExifInterface exif) {
        double tle_model_y_intercept = getTLEModelYIntercept(exif);
        double tle_model_slant_range_coeff = getTLEModelSlantRangeCoefficient(exif);
        return new TLE_Model_Parameters(tle_model_y_intercept, tle_model_slant_range_coeff);
    }

    /**
     * Returns an ordered Map of distortion parameter names and their values for the appropriate lensType
     * @param exif exif of an image to analyze for make and model
     * @return LinkedHashMap ordered Map containing distortion parameter names and their values
     */
    public static LinkedHashMap<String, Double> getDistortionParameters(OpenAthenaExifInterface exif) {
        JSONObject drone = getMatchingDrone(exif);
        LinkedHashMap<String, Double> distortionParamMap = new LinkedHashMap<>();
        if (drone == null) return null;
        String lensType;
        try {
            lensType = drone.getString("lensType");
            if ("perspective".equalsIgnoreCase(lensType)) {
                distortionParamMap.put("k1", drone.getDouble("radialR1"));
                distortionParamMap.put("k2", drone.getDouble("radialR2"));
                distortionParamMap.put("k3", drone.getDouble("radialR3")); // value is ignored in the Brown-Conrady model
                distortionParamMap.put("p1", drone.getDouble("tangentialT1"));
                distortionParamMap.put("p2", drone.getDouble("tangentialT2"));
            } else if ("fisheye".equalsIgnoreCase(lensType)) {
                distortionParamMap.put("c", drone.getDouble("c"));
                distortionParamMap.put("d", drone.getDouble("d"));
                distortionParamMap.put("e", drone.getDouble("e"));
                distortionParamMap.put("f", drone.getDouble("f"));
                distortionParamMap.put("poly0", drone.getDouble("poly0"));
                distortionParamMap.put("poly1", drone.getDouble("poly1"));
                distortionParamMap.put("poly2", drone.getDouble("poly2"));
                distortionParamMap.put("poly3", drone.getDouble("poly3"));
                distortionParamMap.put("poly4", drone.getDouble("poly4"));
            } else {
                throw new IllegalArgumentException("Unknown lens type: " + lensType);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
        return distortionParamMap;
    }

    /**
     * Given a make and model string, returns the intrinsics of the matching drone camera from db lookup
     * @param exif exif of an image where the camera intrinsic parameters are desired
     * @return a JSONObject containing the intrinsic parameters of the particular matching camera
     * <p>
     *     Many drone models have an EXIF make/model name collision between their main color camera and their secondary thermal camera, even though each has entirely different intrinsics.
     * </p>
     */
    public static JSONObject getMatchingDrone(OpenAthenaExifInterface exif) {
        String make = exif.getAttribute(ExifInterface.TAG_MAKE);
        String model = exif.getAttribute(ExifInterface.TAG_MODEL);
        JSONArray matchingDrones = parameterProvider.getMatchingDrones(make, model);
        if (matchingDrones.length() < 1) {
            return null;
        }

        double targetWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
        if (targetWidth <= 0) {
            throw new RuntimeException("could not determine width and height of image!");
        }

        double smallestDifference = Double.MAX_VALUE;
        JSONObject closestDrone = null;

        for (int i = 0; i < matchingDrones.length(); i++) {
            try {
                JSONObject drone = matchingDrones.getJSONObject(i);
                double droneWidth = drone.getInt("widthPixels");

                double difference_ratio = droneWidth / targetWidth;
                if (difference_ratio < 1.0d) {
                    difference_ratio = 1 / difference_ratio;
                }
                if (difference_ratio < smallestDifference) {
                    closestDrone = drone;
                    smallestDifference = difference_ratio;
                }
            } catch (JSONException e) {
                return null;
            }
        }
        return closestDrone;
    }

    /**
     * Returns the sensor physical height of a single pixel of a given drone make/model from droneModels.json database
     * @param exif OpenAthenaExifInterface from metadata of an image to analyze for make and model
     * @return double height (in mm) per pixel of the camera sensor of the given make/model camera
     */
    public static double getSensorPhysicalHeight(OpenAthenaExifInterface exif) {
        JSONObject drone = getMatchingDrone(exif);
        if (drone == null) {
            return -1.0d;
        }

        try {
            double heightPerPixel = (double) rationalToFloat(drone.getString("ccdHeightMMPerPixel"));
            double heightPixels = (double) drone.getInt("heightPixels");
            return heightPerPixel * heightPixels;
        } catch (JSONException jse) {
            return -1.0d;
        }
    }

    /**
     * Returns the sensor physical width of a single pixel of a given drone make/model from droneModels.json database
     * @param exif OpenAthenaExifInterface from metadata of an image to analyze for make and model
     * @return double width (in mm) per pixel of the camera sensor of the given make/model camera
     */
    public static double getSensorPhysicalWidth(OpenAthenaExifInterface exif) {
        JSONObject drone = getMatchingDrone(exif);
        if (drone == null) {
            return -1.0d;
        }

        try {
            double widthPerPixel = (double) rationalToFloat(drone.getString("ccdWidthMMPerPixel"));
            double widthPixels = (double) drone.getInt("widthPixels");
            return widthPerPixel * widthPixels;
        } catch (JSONException jse) {
            return -1.0d;
        }
    }

    /**
     * Obtain position and orientation values from EXIF and XMP metadata of a given image
     * @param exif OpenAthenaExifInterface from the image metadata to be analyzed. Usually includes XMP metadata as well
     * @return double[6] containing latitude, longitude, elevation (in WGS84 hae), azimuth, pitch angle (theta, where downwards is positive), roll
     * @throws XMPException If expected XMP metadata values are missing
     * @throws MissingDataException If XMP or EXIF metadata are missing
     */
    public static double[] getMetadataValues(OpenAthenaExifInterface exif) throws XMPException, MissingDataException {
        if (exif == null) {
            Log.e(TAG, "ERROR: getMetadataValues failed, ExifInterface was null");
            throw new IllegalArgumentException("ERROR: getMetadataValues failed, exif was null");
        }
        String make = exif.getAttribute(ExifInterface.TAG_MAKE).toUpperCase(Locale.ENGLISH);
        String model = exif.getAttribute(ExifInterface.TAG_MODEL).toUpperCase(Locale.ENGLISH);
        if (make == null || make.equals("")) {
            return null;
        }

        switch(make) {
            case "DJI":
                return handleDJI(exif);
            //break;
            case "HASSELBLAD": // Make name for older DJI pro cameras
                return handleDJI(exif);
            case "SKYDIO":
                return handleSKYDIO(exif);
            //break;
            case "AUTEL ROBOTICS":
                parent.displayAutelAlert();
                return handleAUTEL(exif);
            //break;
            case "PARROT":
                if (model.contains("ANAFI") || model.contains("BEBOP")) {
                    return handlePARROT(exif);
                } else {
                    Log.e(TAG, "ERROR: Parrot model " + model + " not usable at this time");
                    throw new XMPException(parent.getString(R.string.parrot_model_prefix_error_msg) + model + parent.getString(R.string.not_usable_at_this_time_error_msg), XMPError.BADVALUE);
                }
                //break;
            case "TELEDYNE FLIR":
                if (model.contains("HADRON 640") || model.contains("BOSON 640")) {
                    return handleTeal(exif);
                } else {
                    Log.e(TAG, "ERROR: Teal model " + model + " not usable at this time");
                    throw new XMPException("ERROR: Teal model " + model + " not usable at this time", XMPError.BADVALUE);
                }
            default:
                Log.e(TAG, parent.getString(R.string.make_prefix_error_msg) + " " + make + " " + parent.getString(R.string.not_usable_at_this_time_error_msg));
                throw new XMPException(parent.getString(R.string.make_prefix_error_msg) + " " + make + " " + parent.getString(R.string.not_usable_at_this_time_error_msg), XMPError.BADXMP);
        }
    }

    /**
     * Obtain position and orientation values from EXIF and XMP metadata of a DJI drone image
     * @param exif OpenAthenaExifInterface from the DJI drone image metadata to be analyzed. Usually includes XMP metadata as well
     * @return double[6] containing latitude, longitude, elevation (in WGS84 hae), azimuth, pitch angle (theta, where downwards is positive), roll
     * @throws XMPException If expected XMP metadata values are missing
     * @throws MissingDataException If XMP or EXIF metadata are missing
     */
    public static double[] handleDJI(OpenAthenaExifInterface exif) throws XMPException, MissingDataException{
        String xmp_str = exif.getAttribute(ExifInterface.TAG_XMP);
        if (xmp_str == null) {
            throw new MissingDataException(parent.getString(R.string.xmp_missing_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        } if (xmp_str.trim().equals("")) {
            throw new MissingDataException(parent.getString(R.string.xmp_empty_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        }
        Log.i(TAG, "xmp_str for Make DJI: " + xmp_str);
        XMPMeta xmpMeta = XMPMetaFactory.parseFromString(xmp_str.trim());

        String longitudeTagName = "GpsLongitude";
        String schemaNS = "http://www.dji.com/drone-dji/1.0/";
        String latitude = xmpMeta.getPropertyString(schemaNS, "GpsLatitude");
        if (latitude == null) {
            // Older DJI drones have a different tag name
            latitude = xmpMeta.getPropertyString(schemaNS, "Latitude");
            longitudeTagName = "Longitude";
        }

        double y; double x; double z;
        if (latitude != null) {
            y = Double.parseDouble(latitude);

            String longitude = xmpMeta.getPropertyString(schemaNS, longitudeTagName);
            if (longitude == null || longitude.equals("")) {
                // handle a typo "GpsLongtitude" that occurs in certain versions of Autel drone firmware (which use drone-dji metadata format)
                longitude = xmpMeta.getPropertyString(schemaNS, "GpsLong" + "t" + "itude");
                if (longitude == null || longitude.equals("")) {
                    throw new MissingDataException(parent.getString(R.string.missing_data_exception_longitude_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.LATITUDE);
                }
            }
            x = Double.parseDouble(longitude);

            String altitude = xmpMeta.getPropertyString(schemaNS, "AbsoluteAltitude");
            if (altitude != null) {
                z = Double.parseDouble(altitude);
            } else {
                throw new MissingDataException(parent.getString(R.string.missing_data_exception_altitude_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALTITUDE);
            }
        } else {
            // Lat, Lon, Alt metadata not present in XMP, use EXIF as backup instead:
            Float[] yxz = exifGetYXZ(exif); // may throw MissingDataException
            y = (double) yxz[0];
            x = (double) yxz[1];
            z = (double) yxz[2];
        }

        double azimuth;
        String gimbalYawDegree = xmpMeta.getPropertyString(schemaNS, "GimbalYawDegree");
        if (gimbalYawDegree != null) {
            azimuth = Double.parseDouble(gimbalYawDegree);
            azimuth = degNormalize(azimuth);
        } else {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_azimuth_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.AZIMUTH);
        }

        double theta;
        String gimbalPitchDegree = xmpMeta.getPropertyString(schemaNS, "GimbalPitchDegree");
        if (gimbalPitchDegree != null) {
            theta = -1.0d * Double.parseDouble(gimbalPitchDegree);
        } else {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_theta_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.THETA);
        }

        double roll;
        String gimbalRollDegree = xmpMeta.getPropertyString(schemaNS, "GimbalRollDegree");
        if (gimbalRollDegree != null) {
            roll = Double.parseDouble(gimbalRollDegree);
        } else {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_roll), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ROLL);
        }

        // safety check: if metadata azimuth and theta are zero, it's extremely likely the metadata is invalid
        if (Math.abs(azimuth) <= 0.0001d && Math.abs(theta) <= 0.0001d) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_altitude_and_theta_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.THETA);
        }

        // DJI old firmware is orthometric (EGM96 AMSL) but is ellipsoidal (WGS84 hae) for newer firmware or if special RTK device is used
        String make = exif.getAttribute(ExifInterface.TAG_MAKE);
        if (make == null) make = ""; else make = make.toLowerCase(Locale.ENGLISH).trim();
        String thisModel = exif.getAttribute(ExifInterface.TAG_MODEL);
        if (thisModel == null) thisModel = ""; else thisModel = thisModel.toUpperCase(Locale.ENGLISH).trim();

        // Check against a blacklist of DJI drone models which only provide relative altitude in their AbsoluteAltitude tag
        // Despite providing an AbsoluteAltitude tag value, some older DJI drones actually only provide
        // relative altitude from their launch point. Without an absolute vertical reference,
        // there is no way to provide accurate target calculations from such drones
        if (DJI_MODELS_RELATIVE_ALTITUDE_ONLY_BLACKLIST.contains(thisModel)) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_altitude_absolute_reference), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALTITUDE_ABSOLUTE_REFERENCE);
        }

        Version thisFirmwareVersion = new Version("0");
        String firmwareString;
        try {
            firmwareString = xmpMeta.getPropertyString(schemaNS, "Version");
        } catch (XMPException e) {
            firmwareString = null;
        }
        if (firmwareString != null) {
            Log.i(TAG, "drone-dji:Version XMP tag value was: " + firmwareString);
            thisFirmwareVersion = new Version(firmwareString);
        }
        // Check if firmware version means vertical datum should be WGS84 hae instead of EGM96 amsl
        boolean isFirmwareVerticalDatumWGS84 = (thisFirmwareVersion.compareTo(djiFirmwareVerticalDatumWasSwitched) >= 0);
        Log.i(TAG, "isFirmwareVerticalDatumWGS84: " + Boolean.toString(isFirmwareVerticalDatumWGS84));

        if (!make.contains("autel") && !xmp_str.toLowerCase(Locale.ENGLISH).contains("rtkflag") && !isFirmwareVerticalDatumWGS84) {
            //Log.i(TAG, "Offset is: " +  offsetProvider.getEGM96OffsetAtLatLon(y, x));

            // Some older DJI models (e.g. the Mavic 2 Zoom) use EGM96 (height above mean sea level)
            // Newer firmware (specifically with tag djiVersion>=1.6) uses WGS84 (height above ellipsoid)
            // convert the height from EGM96 AMSL to WGS84 hae if made by dji, rtk device not present, and firmware < 1.6
            Log.i(TAG, "Converting from orthometric to ellipsoidal vertical datum for image metadata");
            // re issue #180, fix incorrect equation for applying geoid offset
            z = z + offsetProvider.getEGM96OffsetAtLatLon(y,x);
        }

        double[] outArr = new double[]{y, x, z, azimuth, theta, roll};
        return outArr;
    }

    /**
     * Obtain position and orientation values from EXIF and XMP metadata of a given Skydio drone image
     * @param exif OpenAthenaExifInterface from the Skydio drone image metadata to be analyzed. Usually includes XMP metadata as well
     * @return double[6] containing latitude, longitude, elevation (in WGS84 hae), azimuth, pitch angle (theta, where downwards is positive), roll
     * @throws XMPException If expected XMP metadata values are missing
     * @throws MissingDataException If XMP or EXIF metadata are missing
     */
    public static double[] handleSKYDIO(OpenAthenaExifInterface exif) throws XMPException, MissingDataException {
        String xmp_str = exif.getAttribute(ExifInterface.TAG_XMP);
        if (xmp_str == null) {
            throw new MissingDataException(parent.getString(R.string.xmp_missing_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        } if (xmp_str.trim().equals("")) {
            throw new MissingDataException(parent.getString(R.string.xmp_empty_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        }
        Log.i(TAG, "xmp_str for Make SKYDIO: " + xmp_str);
        XMPMeta xmpMeta = XMPMetaFactory.parseFromString(xmp_str.trim());
        String schemaNS = "https://www.skydio.com/drone-skydio/1.0/";

        double y; double x; double z; double azimuth; double theta; double roll;

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
            azimuth = degNormalize(azimuth);
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_altitude_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.AZIMUTH);
        }

        try {
            theta = Double.parseDouble(xmpMeta.getStructField(schemaNS, "CameraOrientationNED", schemaNS, "Pitch").getValue());
            theta = -1.0d * theta;
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_theta_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.THETA);
        }

        try {
            // NED, positive roll is clockwise TODO Verify this!
            roll = Double.parseDouble(xmpMeta.getStructField(schemaNS, "CameraOrientationNED", schemaNS, "Roll").getValue());
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_roll), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ROLL);
        }

        // Skydio altitude is orthometric (EGM96 AMSL), we must convert to ellipsoidal (WGS84 hae)
        // z = z - offsetProvider.getEGM96OffsetAtLatLon(y, x);
        // re issue #180, fix incorrect equation for applying geoid offset
        z = z + offsetProvider.getEGM96OffsetAtLatLon(y, x);

        double[] outArr = new double[]{y, x, z, azimuth, theta, roll};
        return outArr;
    }

    /**
     * Obtain position and orientation values from EXIF and XMP metadata of a given Autel Robotics image
     * @param exif OpenAthenaExifInterface from the Autel Robotics image metadata to be analyzed. Usually includes XMP metadata as well
     * @return double[6] containing latitude, longitude, elevation (in WGS84 hae), azimuth, pitch angle (theta, where downwards is positive), roll
     * @throws XMPException If expected XMP metadata values are missing
     * @throws MissingDataException If XMP or EXIF metadata are missing
     */
    public static double[] handleAUTEL(OpenAthenaExifInterface exif) throws XMPException, MissingDataException{
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

        if (!rdf_about.toLowerCase(Locale.ENGLISH).contains("autel")) {
            isNewMetadataFormat = true;
        } else {
            isNewMetadataFormat = false;
        }

        double y;
        double x;
        double z;
        double azimuth;
        double theta;
        double roll;

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
                azimuth = degNormalize(azimuth);
            } catch (NumberFormatException nfe) {
                throw new MissingDataException(parent.getString(R.string.missing_data_exception_azimuth_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.AZIMUTH);
            }

            try {
                theta = Double.parseDouble(xmpMeta.getPropertyString(schemaNS, "Pitch"));
            } catch (NumberFormatException nfe) {
                throw new MissingDataException(parent.getString(R.string.missing_data_exception_theta_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.THETA);
            }

            //String make = exif.getAttribute(ExifInterface.TAG_MAKE);
            //if (make == null) make = ""; else make = make.toLowerCase(Locale.ENGLISH);
            //String model = exif.getAttribute(ExifInterface.TAG_MODEL);
            //if (model == null) model = ""; else model = model.toUpperCase(Locale.ENGLISH);

            if (theta >= 0.0d) {
                // AUTEL old firmware Camera pitch 0 is down, 90 is forwards towards horizon
                // so, we use the complement of the angle instead
                // see: https://support.pix4d.com/hc/en-us/articles/202558969-Yaw-Pitch-Roll-and-Omega-Phi-Kappa-angles
                // if drone implements the above spec incorrectly and camera pitch is above horizon, the pitch value below will be incorrect
                theta = 90.0d - theta;
            } else {
                // Some Autel Drones (such as the Autel Robotics Evo Lite Enterprise 640T [XL715]) implement the PIX4D camera pitch spec incorrectly.
                // It is SUPPOSED TO BE 0 is down, 90 is forwards towards horizon
                // HOWEVER: some drones appear to use DJI-style pitch values where 0 is straight forwards and negative values for downward pitch
                // It is highly unlikely that a camera would be pointing further backwards than straight down (this is what a negative value would represent if the spec was used correctly)
                // THEREFORE: if the pitch value is negative, we will assume they implemented the spec incorrectly and just treat it as DJI-style pitch value
                theta = -1.0d * theta;
            }

            try {
                // pix4d NED, positive roll is clockwise
                roll = Double.parseDouble(xmpMeta.getPropertyString(schemaNS, "Roll"));
            } catch (NumberFormatException nfe) {
                throw new MissingDataException(parent.getString(R.string.missing_data_exception_roll), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ROLL);
            }

            double[] outArr = new double[]{y, x, z, azimuth, theta, roll};
            return outArr;
        }
    }

    /**
     * Obtain position and orientation values from EXIF and XMP metadata of a given Parrot drone image
     * @param exif OpenAthenaExifInterface from the Parrot drone image metadata to be analyzed. Usually includes XMP metadata as well
     * @return double[6] containing latitude, longitude, elevation (in WGS84 hae), azimuth, pitch angle (theta, where downwards is positive), roll
     * @throws XMPException If expected XMP metadata values are missing
     * @throws MissingDataException If XMP or EXIF metadata are missing
     */
    public static double[] handlePARROT(OpenAthenaExifInterface exif) throws XMPException, MissingDataException{
        double y;
        double x;
        double z;
        double azimuth;
        double theta;
        double roll;

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
            azimuth = degNormalize(azimuth);
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_azimuth_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.AZIMUTH);
        }

        try {
            //      convert to OpenAthena notation, where a downwards angle is positive
            theta = -1.0d * Double.parseDouble(xmpMeta.getPropertyString(schemaNS, "CameraPitchDegree"));
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_theta_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.THETA);
        }

        try {
            // positive roll is clockwise TODO Verify this!
            roll = Double.parseDouble(xmpMeta.getPropertyString(schemaNS, "CameraRollDegree"));
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_roll), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ROLL);
        }

        // From Parrot Docs, regarding EGM96 AMSL vs WGS84 hae:
        // Location altitude of where the photo was taken in meters expressed as a fraction (e.g. “4971569/65536”) On ANAFI 4K/Thermal/USA, this is the drone location with reference to the EGM96 geoid (AMSL); on ANAFI Ai with firmware < 7.4, this is the drone location with with reference to the WGS84 ellipsoid; on ANAFI Ai with firmware >= 7.4, this is the front camera location with reference to the WGS84 ellipsoid
        // https://developer.parrot.com/docs/groundsdk-tools/photo-metadata.html
        String model = exif.getAttribute(ExifInterface.TAG_MODEL).toUpperCase(Locale.ENGLISH);
        if (!model.toLowerCase(Locale.ENGLISH).contains("anafiai")) {
            // convert from EGM96 AMSL to WGS84 hae (if necessary)
            // z = z - offsetProvider.getEGM96OffsetAtLatLon(y,x);
            // re issue #180, fix incorrect equation for applying geoid offset
            z = z + offsetProvider.getEGM96OffsetAtLatLon(y,x);
        }

        double[] outArr = new double[]{y, x, z, azimuth, theta, roll};
        return outArr;
    }

    /**
     * Obtain position and orientation values from EXIF and XMP metadata of a given Teal drone image
     * @param exif OpenAthenaExifInterface from the Teal drone image metadata to be analyzed. Usually includes XMP metadata as well
     * @return double[6] containing latitude, longitude, elevation (in WGS84 hae), azimuth, pitch angle (theta, where downwards is positive), roll
     * @throws XMPException if exception occurs during parsing of XMP metadata
     * @throws MissingDataException if XMP or EXIF metadata are missing
     */
    public static double[] handleTeal(OpenAthenaExifInterface exif) throws XMPException, MissingDataException{
        double y;
        double x;
        double z;
        double azimuth;
        double theta;
        double roll;

        Float[] yxz = exifGetYXZ(exif);
        y = yxz[0];
        x = yxz[1];
        z = yxz[2];

        // Convert vertical datum from EGM96 (AMSL) to WGS84 (HAE)
        // z = z - offsetProvider.getEGM96OffsetAtLatLon(y,x);
        // re issue #180, fix incorrect equation for applying geoid offset
        z = z + offsetProvider.getEGM96OffsetAtLatLon(y,x);

        String xmp_str = exif.getAttribute(ExifInterface.TAG_XMP);
        if (xmp_str == null) {
            throw new MissingDataException(parent.getString(R.string.xmp_missing_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        } if (xmp_str.trim().equals("")) {
            throw new MissingDataException(parent.getString(R.string.xmp_empty_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ALL);
        }

        Log.i(TAG, "xmp_str for Make TELEDYNE FLIR (Teal): " + xmp_str);
        XMPMeta xmpMeta = XMPMetaFactory.parseFromString(xmp_str.trim());

        String schemaNS = "http://ns.adobe.com/exif/1.0/";

        // For Teal2, it seems that Camera yaw/pitch/roll is expressed relative to the Platform
        // So to obtain absolute yaw/pitch/roll, sum the two together
        try {
            azimuth = rationalToFloat(xmpMeta.getPropertyString(schemaNS, "PlatformYaw")) +
                      rationalToFloat(xmpMeta.getPropertyString(schemaNS, "CameraYaw"));
            azimuth = degNormalize(azimuth);
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_azimuth_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.AZIMUTH);
        }

        try {

            theta = rationalToFloat(xmpMeta.getPropertyString(schemaNS, "PlatformPitch")) +
                    rationalToFloat(xmpMeta.getPropertyString(schemaNS, "CameraPitch"));
            theta *= -1.0d; // convert to OpenAthena notation, where a downwards angle is positive
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_theta_error_msg), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.THETA);
        }

        try {
            // positive roll is clockwise TODO Verify this!
            roll = rationalToFloat(xmpMeta.getPropertyString(schemaNS, "PlatformRoll")) +
                   rationalToFloat(xmpMeta.getPropertyString(schemaNS, "CameraRoll"));
        } catch (NumberFormatException nfe) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_roll), MissingDataException.dataSources.EXIF_XMP, MissingDataException.missingValues.ROLL);
        }

        double[] outArr = new double[]{y, x, z, azimuth, theta, roll};
        return outArr;
    }


    // http://android-er.blogspot.com/2009/12/read-exif-information-in-jpeg-file.html

    /**
     * Obtain a string representation for both the key and value of a given EXIF tag
     * @param tag String representation of the EXIF tag key name
     * @param exif OpenAthenaExifInterface containing the drone image metadata to be analyzed
     * @return String representation of the EXIF tag key and value
     */
    public static String getTagString(String tag, OpenAthenaExifInterface exif)
    {
        return(tag + " : " + exif.getAttribute(tag) + "\n");
    }

    /**
     * Obtain latitude, longitude, and elevation values from EXIF metadata of a given drone image
     * <p>
     *     This is a fallback method only used where absolutely necessary; typically XMP metadata is more precise
     * </p>
     * @param exif OpenAthenaExifInterface containing the drone image metadata to be analyzed
     * @return Float[] containing latitude, longitude, and elevation
     * @throws MissingDataException If EXIF metadata are missing
     */
    public static Float[] exifGetYXZ(OpenAthenaExifInterface exif) throws MissingDataException
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

        String altDir = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
        String alt = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
        if (alt == null) {
            throw new MissingDataException(parent.getString(R.string.missing_data_exception_altitude_error_msg), MissingDataException.dataSources.EXIF, MissingDataException.missingValues.ALTITUDE);
        }

        latDir = latDir.toUpperCase(Locale.ENGLISH);
        String[] latArr = latRaw.split(",", 3);
        lonDir = lonDir.toUpperCase(Locale.ENGLISH);
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
        if (altDir != null && altDir.equals("1")) {
            z = z * -1.0f;
        }

        return(new Float[]{y, x, z});
    }

    public static double[] getIntrinsicMatrixFromExif(OpenAthenaExifInterface exif) throws Exception {
        JSONObject drone = getMatchingDrone(exif);

        if (drone != null) {
            double[] pixelDimensions = new double[4];
            pixelDimensions[0] = (double) rationalToFloat(drone.getString("ccdWidthMMPerPixel"));
            pixelDimensions[1] = (double) rationalToFloat(drone.getString("ccdHeightMMPerPixel"));
            pixelDimensions[2] = (double) drone.getInt("widthPixels");
            pixelDimensions[3] = (double) drone.getInt("heightPixels");
            Log.i(TAG, "found pixel dimensions (mm) from lookup: " + pixelDimensions[0] + ", " + pixelDimensions[1]);
            return getIntrinsicMatrixFromKnownCCD(exif, pixelDimensions);
        } else {
            Log.i(TAG, "Camera make and model not recognized. Guestimating intrinsics from exif...");
            return getIntrinsicMatrixFromExif35mm(exif);
        }
    }

    /**
     * Obtain digital zoom ratio from EXIF metadata of a given drone image
     * <p>
     *     Parrot Anafi drone is a special case which does not report its digital zoom ratio in EXIF
     *     For this particular drone model, a fallback method is used which compares the image resolution
     *     to the full resolution of the uncropped sensor to determine the effective digital zoom ratio
     * </p>
     * @param exif OpenAthenaExifInterface containing the drone image metadata to be analyzed
     * @return float representing the digital zoom ratio
     * @throws Exception if exception occurs during parsing of EXIF metadata
     */
    public static float getDigitalZoomRatio(OpenAthenaExifInterface exif) throws Exception {
        float digitalZoomRatio = 1.0f;

        String make = exif.getAttribute(ExifInterface.TAG_MAKE);
        if (make == null) make = "";
        String model = exif.getAttribute(ExifInterface.TAG_MODEL);
        if (model == null) model = "";

        JSONObject drone = getMatchingDrone(exif);

        try {
            double[] pixelDimensions = new double[4];
            if (drone != null) {
                pixelDimensions[0] = (double) rationalToFloat(drone.getString("ccdWidthMMPerPixel"));
                pixelDimensions[1] = (double) rationalToFloat(drone.getString("ccdHeightMMPerPixel"));
                pixelDimensions[2] = (double) drone.getInt("widthPixels");
                pixelDimensions[3] = (double) drone.getInt("heightPixels");
            } else {
                // placeholder values which will never be used
                pixelDimensions[0] = -1.0d;
                pixelDimensions[1] = -1.0d;
                pixelDimensions[2] = -1.0d;
                pixelDimensions[3] = -1.0d;
            }
            String digitalZoomRational = exif.getAttribute(ExifInterface.TAG_DIGITAL_ZOOM_RATIO);
            if (digitalZoomRational != null && !digitalZoomRational.equals("")) {
                digitalZoomRatio = rationalToFloat(exif.getAttribute(ExifInterface.TAG_DIGITAL_ZOOM_RATIO));
                if (digitalZoomRatio < 1.0f) {
                    digitalZoomRatio = 1.0f;
                }
            }

            if (make.equalsIgnoreCase("PARROT")) {
                if (model.equalsIgnoreCase("ANAFI") || model.equalsIgnoreCase("ANAFIUSA") || model.equalsIgnoreCase("ANAFIUA")) {
                    Log.d(TAG, "isThermal: " + isThermal(exif));
                    if (!isThermal(exif)) {
                        Log.d(TAG, "getDigitalZoomRatio special case for parrot triggered");
                        if (drone == null) {
                            throw new Exception("Could not find necessary data for Parrot Anafi");
                        }
                        double imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
                        if (imageWidth <= 0.0d) {
                            throw new Exception("Could not determine width of image");
                        }
                        // Parrot ANAFI and ANAFI USA do not report digital zoom correctly in EXIF DigitalZoomRatio tag
                        // as a workaround, we will look at the pixel width of the image
                        // and assume it is cropped if it is less than the full sensor size.
                        // ALTHOUGH: If a Parrot image has been scaled down by external software this value will be completely wrong
                        digitalZoomRatio = 5344.0f / (float) imageWidth;
                    }
                }
            }
            if (digitalZoomRatio != 1.0f) {
                Log.d(TAG, "digitalZoomRatio is: " + digitalZoomRatio);
                // Some thermal cameras perform undocumented integer upscaling on digitally zoomed thermal images
                //     (which seems to be atypical for images with digital zoom)
                //     to compensate, we ignore the digitalZoomRatio when calculating
                //     the number of pixels for width and height
                //
                // Known behavior for Autel Evo III 640T, (TODO) might affect other make/models and/or color cameras as well!
                if (isThermal(exif) && make.equalsIgnoreCase("AUTEL ROBOTICS")) {
                    Log.d(TAG, "edge case detected: image had undocumented pixel upscaling. Ignoring digitalZoomRatio");
                    digitalZoomRatio = 1.0f;
                }
            }
        } catch (JSONException jse) {
            if (make.equalsIgnoreCase("PARROT")) {
                if (model.equalsIgnoreCase("ANAFI") || model.equalsIgnoreCase("ANAFIUSA") || model.equalsIgnoreCase("ANAFIUA")) {
                    if (!isThermal(exif)) {
                        throw new Exception("Could not find necessary data for Parrot Anafi");
                    }
                }
            }
        }
        return digitalZoomRatio;
    }

    /**
     * Obtain camera intrinsics matrix from a drone camera with known calibration values in droneModels.json
     * @param exif OpenAthenaExifInterface containing the drone image metadata to be analyzed
     * @param pixelDimensions mmWidth and mmHeight of the camera CCD/CMOS sensor
     * @return double[] representing the camera intrinsics matrix
     * @throws Exception if exception occurs during parsing of EXIF metadata
     */
    protected static double[] getIntrinsicMatrixFromKnownCCD(OpenAthenaExifInterface exif, double[] pixelDimensions) throws Exception {
        JSONObject drone = getMatchingDrone(exif);

        if (exif == null) {
            throw new IllegalArgumentException("Failed to get intrinsics, ExifInterface was null!");
        }
        if (pixelDimensions == null) {
            Log.e(TAG, "Failed to calculate intrinsics, ccdDimensions was null!");
            Log.e(TAG, "Warning: reverting to calc from 35mm mode");
            return getIntrinsicMatrixFromExif35mm(exif);
        }
        if (pixelDimensions.length < 4) {
            Log.e(TAG, "Failed to calculate intrinsics, ccdDimensions was invalid!");
            Log.e(TAG, "Warning: reverting to calc from 35mm mode");
            return getIntrinsicMatrixFromExif35mm(exif);
        }

        String focalRational = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
        double focalLength = rationalToFloat(focalRational);
        // Some thermal cameras don't report their focal length in EXIF (or report them incorrectly)
        //     if their focal length is a fixed known value stored in droneModels.json,
        //     override the EXIF one with this value from the database.
        //     Otherwise: if focal length is still unknown fall back to less accurate 35mm guess-timate method
        if (drone != null && drone.has("focalLength")) {
            focalLength = drone.getDouble("focalLength");
        } else if (focalLength == -1.0d || focalLength == 0.0d) {
            Log.e(TAG, "Failed to calculate intrinsics, focal length was missing or invalid!");
            Log.e(TAG, "Warning: reverting to calc from 35mm mode");
            return getIntrinsicMatrixFromExif35mm(exif);
        }
        Log.i(TAG, "focalLength is: " + focalLength + "mm");

        double mmWidthPerPixel = pixelDimensions[0];
        double mmHeightPerPixel = pixelDimensions[1];
        double pixelAspectRatio = mmWidthPerPixel / mmHeightPerPixel;
        double ccdWidthPixels = pixelDimensions[2];
        double ccdHeightPixels = pixelDimensions[3];

        float digitalZoomRatio = getDigitalZoomRatio(exif);

        double imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
        double imageHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1); // Image Height
        if (imageWidth <= 0.0d || imageHeight <= 0.0d) {
            throw new Exception("could not determine width and height of image!");
        }

        double scaleRatio = imageWidth * digitalZoomRatio / ccdWidthPixels; // ratio current size : original size, measured on x axis

        double alpha_x = focalLength / mmWidthPerPixel; // focal length in pixel units
        alpha_x = alpha_x * scaleRatio; // scale down if image is scaled down
        double alpha_y = alpha_x / pixelAspectRatio; // focal length equivalent in pixel units, for the homogenous y axis in the image frame

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

    /**
     * Fallback method for estimating camera intrinsics matrix from a drone camera with unknown calibration values (not in droneModels.json) using the EXIF focalLength35mmEquiv tag
     * <p>
     *     This method will be far less accurate than getIntrinsicMatrixFromKnownCCD(), so a warning will be displayed to the user
     * </p>
     * @param exif OpenAthenaExifInterface containing the drone image metadata to be analyzed
     * @return double[] representing the camera intrinsics matrix, estimated by a less accurate fallback method
     * @throws Exception if exception occurs during parsing of EXIF metadata
     */
    protected static double[] getIntrinsicMatrixFromExif35mm(OpenAthenaExifInterface exif) throws Exception{
        if (exif == null) {
            throw new IllegalArgumentException("Failed to get intrinsics, ExifInterface was null!");
        }

        double[] intrinsicMatrix = new double[9];

        double focalLength35mmEquiv = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, -1.0d);

        if (focalLength35mmEquiv == -1.0d || focalLength35mmEquiv == 0.0d) {
            throw new Exception(parent.getString(R.string.error_metadata_extractor_focal_length_could_not_be_determined));
        }

        float digitalZoomRatio = getDigitalZoomRatio(exif);

        double imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
        double imageHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0); // Image Height
        if (imageWidth <= 0.0d || imageHeight <= 0.0d) {
            throw new Exception(parent.getString(R.string.error_metadataextractor_could_not_determine_width_and_height_of_image));
        }

        // calculate aspect ratio
        // note that this represents the aspect ratio of the CCD, not the image!
        double ccdAspectRatio = 4.0d/3.0d; // This will be WRONG if the sensor is not 4:3! e.g: APS-C or Full-frame 3:2

        // Calculate the intrinsic matrix elements
        double alpha_x = (imageWidth * digitalZoomRatio) * focalLength35mmEquiv / 36.0d; // focal length, in pixel units
        intrinsicMatrix[0] = alpha_x;

        intrinsicMatrix[1] = 0.0f; // gamma, the skew coefficient between the x and the y axis, and is often 0.

        double alpha_y = alpha_x / ccdAspectRatio;

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

    /**
     * Calculate the azimuth (yaw) and elevation (pitch) offset angle of a given pixel from the center of the image
     * @param x pixel x position of the pixel to calculate the angle for (measured from top left corner)
     * @param y pixel y position of the pixel to calculate the angle for (measured from top left corner)
     * @param rollAngleDeg roll angle of the drone camera in degrees
     * @param exifInterface OpenAthenaExifInterface containing the drone image metadata to be analyzed
     * @return double[] representing the yaw and pitch offset angles for the specified image pixel
     * @throws Exception if exception occurs during parsing of EXIF metadata
     */
    public static double[] getRayAnglesFromImgPixel(int x, int y, double rollAngleDeg, OpenAthenaExifInterface exifInterface) throws Exception {
        JSONObject drone = getMatchingDrone(exifInterface);
        String lensType = "";
        if (drone != null) {
            lensType = drone.getString("lensType");
        }

        double[] intrinsics = getIntrinsicMatrixFromExif(exifInterface); // may throw Exception

        double fx = intrinsics[0];
        double fy = intrinsics[4];
        double cx = intrinsics[2];
        double cy = intrinsics[5];

        // convert u,v to undistorted pixel coordinates (based on principal point and focal length)
        double xDistorted = x - cx;
        double yDistorted = y - cy;
        double xNormalized = (xDistorted) / fx;
        double yNormalized = (yDistorted) / fy;

        double xUndistorted = xDistorted;
        double yUndistorted = yDistorted;

        if ("perspective".equalsIgnoreCase(lensType)) {
            try {
                double k1 = drone.getDouble("radialR1");
                double k2 = drone.getDouble("radialR2");
                double k3 = drone.getDouble("radialR3");
                double p1 = drone.getDouble("tangentialT1");
                double p2 = drone.getDouble("tangentialT2");

                if (!(k1 == 0.0 && k2 == 0.0 && k3 == 0.0 && p1 == 0.0 && p2 == 0.0)) {
                    // "A simplification of the standard OpenCV model with the denominator coefficients and tangential coefficients omitted."
                    // https://support.skydio.com/hc/en-us/articles/4417425974683-Skydio-camera-and-metadata-overview
                    // simplified distortion correction model based on Brown-Conrady model, omitting some terms
                    //  A Flexible New Technique for Camera Calibration, 1998
                    // https://www.microsoft.com/en-us/research/wp-content/uploads/2016/02/tr98-71.pdf
                    PerspectiveDistortionCorrector pdc = new PerspectiveDistortionCorrector(k1, k2, k3, p1, p2);
                    double[] undistortedNormalized = pdc.correctDistortion(xNormalized, yNormalized);

                    xUndistorted = undistortedNormalized[0] * fx;
                    yUndistorted = undistortedNormalized[1] * fy;
                } else {
                    Log.i(TAG, "DISTORTION PARAMETERS WERE MISSING!");
                }
            } catch (JSONException jse) {
                Log.e(TAG, "failed to obtain distortion parameters, using just pinhole camera model now");
            }
        } else if ("fisheye".equalsIgnoreCase(lensType)) {
            try {
                // Fisheye distortion goes BRRRRRRRRRRRrrrrrrrrrrr
                // https://support.pix4d.com/hc/en-us/articles/202559089
                // https://www.mathworks.com/help/vision/ug/fisheye-calibration-basics.html
                double p0 = drone.getDouble("poly0");
                double p1 = drone.getDouble("poly1");
                double p2 = drone.getDouble("poly2");
                double p3 = drone.getDouble("poly3");
                double p4 = drone.getDouble("poly4");
                double c = drone.getDouble("c");
                double d = drone.getDouble("d");
                double e = drone.getDouble("e");
                double f = drone.getDouble("f");

                if (!(c == 0.0 && d == 0.0 && e == 0.0 && f == 0.0)) {
                    FisheyeDistortionCorrector fdc = new FisheyeDistortionCorrector(p0, p1, p2, p3, p4, c, d, e, f);
                    double[] undistortedNormalized = fdc.correctDistortion(xNormalized, yNormalized);
                    xUndistorted = undistortedNormalized[0] * fx;
                    yUndistorted = undistortedNormalized[1] * fy;
                } else {
                    Log.e(TAG, "DISTORTION PARAMETERS WERE MISSING!");
                }
            } catch (JSONException jse) {
                Log.e(TAG, "CRITICAL ERROR: CAMERA TYPE WAS FISHEYE BUT LENS DATA MISSING");
                throw new IllegalArgumentException("No lens parameters given for fisheye lens");
            }
        } else if ("".equals(lensType) || lensType == null){
            Log.i(TAG, "Missing lensType for " + exifInterface.getAttribute(ExifInterface.TAG_MAKE) + " " + exifInterface.getAttribute(ExifInterface.TAG_MODEL));
        } else {
            throw new IllegalArgumentException("Unknown lens type: " + lensType);
        }

        // calculate ray angles using undistorted coordinates
        double azimuth = Math.atan2(xUndistorted, fx);
        double elevation = Math.atan2(yUndistorted, fy);

        azimuth = Math.toDegrees(azimuth);
        elevation = Math.toDegrees(elevation);

        // calculation of what the ray angle would be without distortion correction
        // for debug use only
        double azDistorted = Math.atan2(xDistorted, fx);
        double elDistorted = Math.atan2(yDistorted, fy);
        azDistorted = Math.toDegrees(azDistorted);
        elDistorted = Math.toDegrees(elDistorted);

        // physical roll angle of the camera
        double roll = rollAngleDeg;

        double[] TBAngle = correctRayAnglesForRoll(azimuth, elevation, roll);
        azimuth = TBAngle[0];
        elevation = TBAngle[1];

        // for debug use only
        TBAngle = correctRayAnglesForRoll(azDistorted, elDistorted, roll);
        azDistorted = TBAngle[0];
        elDistorted = TBAngle[1];

        Log.d(TAG, "Pixel (" + (xUndistorted + cx) + ", " + (yUndistorted + cy) + ", Roll: " + roll + ") -> Ray (" + azimuth + ", " + elevation + ")");
        Log.d(TAG, "Without distortion correction, would have been:\n"+"Pixel (" + x + ", " + y + ", Roll: " + roll + ") -> Ray (" + azDistorted + ", " + elDistorted + ")");
        return new double[] {azimuth, elevation};
    }


    /**
     * For an image taken where the camera lateral axis is not parallel with the ground, express the ray angle in terms of a frame of reference which is parallel to the ground
     * <p>
     *     While the camera gimbal of most drones attempt to keep the camera lateral axis parallel with the ground, this cannot be assumed for all cases. Therefore, this function rotates the 3D angle (calculated by camera intrinsics) by the same amount and direction as the roll of the camera.
     * </p>
     * @param psi the yaw (in degrees) of the ray relative to the camera. Rightwards is positive.
     * @param theta the pitch angle (in degrees) of the ray relative to the camera. Downwards is positive.
     * @param cameraRoll the roll angle (in degrees) of the camera relative to the earth's gravity. From the perspective of the camera, clockwise is positive
     * @return a corrected Tait-Bryan angle double[phi, theta] (in degrees) representing the same ray but in a new frame of reference where the x axis is parallel to the ground (i.e. perpendicular to Earth's gravity)
     */
    public static double[] correctRayAnglesForRoll(double psi, double theta, double cameraRoll) {
        theta = -1.0d * theta; // convert from OpenAthena notation to standard Tait-Bryan aircraft notation (downward is negative)

        // Convert degrees to radians
        psi = Math.toRadians(psi);
        theta = Math.toRadians(theta);
        cameraRoll = Math.toRadians(cameraRoll);

        // Convert Tait-Bryan angles to unit vector
        // Note that these axis are labeled according to the Tait-Bryan aircraft notation, where:
        //     +x is forward
        //     +y is rightward
        //     +z is downward
        // This is different than either the image plane notation or ENU
        double x = Math.cos(theta) * Math.cos(psi);
        double y = Math.cos(theta) * Math.sin(psi);
        double z = Math.sin(theta);

        // Create rotation matrix for roll angle r around the X-axis
        double[][] rotationMatrix = {
                {1, 0, 0},
                {0, Math.cos(cameraRoll), -Math.sin(cameraRoll)},
                {0, Math.sin(cameraRoll), Math.cos(cameraRoll)}
        };

        // Rotate the unit vector back to correct for the observer's roll
        double[] rotatedVector = {
                rotationMatrix[0][0] * x + rotationMatrix[0][1] * y + rotationMatrix[0][2] * z,
                rotationMatrix[1][0] * x + rotationMatrix[1][1] * y + rotationMatrix[1][2] * z,
                rotationMatrix[2][0] * x + rotationMatrix[2][1] * y + rotationMatrix[2][2] * z
        };

        // Convert rotated unit vector back to Tait-Bryan angle
        double correctedPsi = Math.atan2(rotatedVector[1], rotatedVector[0]);
        double correctedTheta = Math.atan2(rotatedVector[2], Math.sqrt(rotatedVector[0] * rotatedVector[0] + rotatedVector[1] * rotatedVector[1]));

        // Convert from radians back to degrees
        correctedPsi = Math.toDegrees(correctedPsi);
        correctedTheta = Math.toDegrees(correctedTheta);

        correctedTheta = -1.0d * correctedTheta; // convert from Tait-Bryan notation to OpenAthena notation (downwards is positive)

        return new double[]{correctedPsi, correctedTheta};
    }

    /**
     * Convert a rational string to a float
     * @param str containing a rational number in the form "numerator/denominator"
     * @return float representing the rational number
     */
    public static float rationalToFloat(String str)
    {
        if (str == null || str.isEmpty()) {
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
