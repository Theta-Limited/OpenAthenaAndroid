package com.openathena;

/**
 * Encapsulation class for storing parameters of linear model for target location error estimation
 */
public class TLE_Model_Parameters {

    // represents the y intercept (starting value) for the target location error linear model for a given drone
    // This is usually around 5.25 meters, which may be attributed to the (in)accuracy of common GPS units.
    public double tle_model_y_intercept;
    // represents how much (in meters) each additional meter of distance adds to target location error for this drone. Slant range has routinely been observed as the primary factor influencing target location error. Values between 0.02 and 0.035 meters error per meter distance are typical.
    public double tle_model_slant_range_coeff;

    public TLE_Model_Parameters(double tle_model_y_intercept, double tle_model_slant_range_coeff) {
        this.tle_model_y_intercept = tle_model_y_intercept;
        this.tle_model_slant_range_coeff = tle_model_slant_range_coeff;
    }
}
