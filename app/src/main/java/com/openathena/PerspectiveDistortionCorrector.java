package com.openathena;

public class PerspectiveDistortionCorrector {
    // Assuming these values are obtained from lens calibration
    private double k1, k2, p1, p2;

    public PerspectiveDistortionCorrector(double k1, double k2, double p1, double p2) {
        this.k1 = k1;
        this.k2 = k2;
        this.p1 = p1;
        this.p2 = p2;
    }

    public double[] correctDistortion(double xNormalized, double yNormalized) {
        double x = xNormalized;
        double y = yNormalized;
        double r2 = x*x + y*y;
        double r4 = r2 * r2;

        // Radial distortion correction
        double xCorrectedNormalized = x * (1 + k1 * r2 + k2 * r4);
        double yCorrectedNormalized = y * (1 + k1 * r2 + k2 * r4);

        // Tangential distortion correction
        xCorrectedNormalized = xCorrectedNormalized + (2 * p1 * x * y + p2 * (r2 + 2 * x * x));
        yCorrectedNormalized = yCorrectedNormalized + (p1 * (r2 + 2 * y * y) + 2 * p2 * x * y);

        return new double[] {xCorrectedNormalized, yCorrectedNormalized};
    }
}
