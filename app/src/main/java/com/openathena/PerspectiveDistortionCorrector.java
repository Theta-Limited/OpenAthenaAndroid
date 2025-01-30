package com.openathena;

public class PerspectiveDistortionCorrector {
    // Assuming these values are obtained from lens calibration
    private double k1, k2, k3;

    public PerspectiveDistortionCorrector(double k1, double k2, double k3) {
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;
    }

    public double[] correctDistortion(double xNormalizedDistorted, double yNormalizedDistorted) {
        // simplified distortion correction based on the division model
        // omits correction for tangential distortion.
        // https://en.wikipedia.org/wiki/Distortion_(optics)#Software_correction

        // Compute r^2
        double r2 = xNormalizedDistorted * xNormalizedDistorted + yNormalizedDistorted * yNormalizedDistorted;
        double r4 = r2 * r2;
        double r6 = r2 * r4;

        // Denominator: 1 + k1*r^2 + k2*r^4 + k3*r^6
        double denom = 1.0 + k1*r2 + k2*r4 + k3*r6;

        // Invert the division model
        double xNormalizedUndistorted = xNormalizedDistorted / denom;
        double yNormalizedUndistorted = yNormalizedDistorted / denom;

        return new double[] { xNormalizedUndistorted, yNormalizedUndistorted };
    }
}
