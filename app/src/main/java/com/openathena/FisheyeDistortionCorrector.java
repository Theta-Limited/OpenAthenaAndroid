package com.openathena;

public class FisheyeDistortionCorrector {
    private double p0, p1, p2, p3, p4;
    private double c, d, e, f;

    public FisheyeDistortionCorrector(double p0, double p1, double p2, double p3, double p4,
                                      double c, double d, double e, double f) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    public double[] correctDistortion(double xNormalized, double yNormalized) {
        double x = xNormalized;
        double y = yNormalized;
        double rDistorted = Math.sqrt(x * x + y * y);
        double thetaDistorted = Math.atan(rDistorted);
        double thetaUndistorted = p0 + p1 * thetaDistorted + p2 * Math.pow(thetaDistorted, 2) +
                p3 * Math.pow(thetaDistorted, 3) + p4 * Math.pow(thetaDistorted, 4);

        double rUndistorted = Math.tan(thetaUndistorted);

        double xRadial = x * (rUndistorted / rDistorted);
        double yRadial = y * (rUndistorted / rDistorted);

        double deltaX = 2 * c * xRadial * yRadial + d * (rUndistorted * rUndistorted + 2 * xRadial * xRadial);
        double deltaY = 2 * d * xRadial * yRadial + c * (rUndistorted * rUndistorted + 2 * yRadial * yRadial);

        double xUndistortedNormalized = xRadial + deltaX;
        double yUndistortedNormalized = yRadial + deltaY;

        return new double[] {xUndistortedNormalized, yUndistortedNormalized};
    }
}
