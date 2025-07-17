package com.openathena;

/**
 * Encapsulation class for storing parameters of 2 factor linear model for target location error estimation
 */
public class TLE_Model_Parameters {

    // represents the y intercept (starting value) for the target location error linear model for a given drone
    // This is usually around 5.25 meters, which may be attributed to the (in)accuracy of common GPS units.
    public double tle_model_y_intercept;
    // represents how much (in meters) each additional meter of distance adds to target location error for this drone. Slant range has routinely been observed as the primary factor influencing target location error. Values between 0.02 and 0.035 meters error per meter distance are typical.
    public double tle_model_slant_range_coeff;
    // represents how the vertical:horizontal distance slant ratio effects target location error. This may often be a negative value given that a steeper slant angle typically results in less target location error. If this factor was determined to be statistically-insignificant for a given drone, it is replaced in the model with 0.0
    public double tle_model_slant_ratio_coeff;

    public TLE_Model_Parameters(double tle_model_y_intercept, double tle_model_slant_range_coeff, double tle_model_slant_ratio_coeff) {
        this.tle_model_y_intercept = tle_model_y_intercept;
        this.tle_model_slant_range_coeff = tle_model_slant_range_coeff;
        this.tle_model_slant_ratio_coeff = tle_model_slant_ratio_coeff;
    }
}
