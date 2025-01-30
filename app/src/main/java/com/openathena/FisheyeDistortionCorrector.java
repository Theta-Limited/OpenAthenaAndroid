package com.openathena;

public class FisheyeDistortionCorrector {
    // Polynomial coefficients for the angle mapping:
    //   θ_undistorted = p0 + p1·θ_distorted + p2·θ_distorted^2 + p3·θ_distorted^3 + p4·θ_distorted^4
    private final double p0, p1, p2, p3, p4;

    // Affine deformation parameters (2×2 matrix):
    //   [ c   d ]
    //   [ e   f ]
    // for the final (x,y).
    private final double c, d, e, f;

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

    /**
     * Given a fisheye-distorted point (xDist,yDist) in normalized coords,
     * produce the undistorted point (xUnd,yUnd).  This code assumes:
     *
     *   1)  rDistorted = sqrt(xDist^2 + yDist^2)
     *   2)  thetaDistorted = atan(rDistorted)
     *   3)  thetaUndistorted = p0 + p1·θ_dist + p2·θ_dist^2 + ...
     *   4)  rUndistorted = tan(thetaUndistorted)
     *   5)  (xRadial, yRadial) = scale * (xDist, yDist)
     *         with scale = rUndistorted / rDistorted
     *   6)  final affine transform:  [xOut  yOut]^T = [ [c  d], [e  f] ] * [xRad yRad]^T
     */
    public double[] correctDistortion(double xDist, double yDist) {
        // Radius in "distorted" normalized coords:
        double rDistorted = Math.sqrt(xDist * xDist + yDist * yDist);

        // Angle in the distorted domain:
        double thetaDistorted = Math.atan(rDistorted);

        // Map the distorted angle to the undistorted angle via polynomial:
        double thetaUndistorted =
                p0
                        + p1 * thetaDistorted
                        + p2 * Math.pow(thetaDistorted, 2)
                        + p3 * Math.pow(thetaDistorted, 3)
                        + p4 * Math.pow(thetaDistorted, 4);

        // Convert angle back to a radial coordinate:
        double rUndistorted = Math.tan(thetaUndistorted);

        // Scale x,y so their radius is rUndistorted:
        double scale = (rDistorted < 1e-12) ? 1.0 : (rUndistorted / rDistorted);
        double xRadial = xDist * scale;
        double yRadial = yDist * scale;

        // ----- Affine deformation (C, D, E, F) -----
        // Instead of “Brown tangential” offsets, typical fisheye uses a 2×2 matrix:
        //   [ c  d ] [ xRadial ]
        //   [ e  f ] [ yRadial ]
        double xFinal = c * xRadial + d * yRadial;
        double yFinal = e * xRadial + f * yRadial;

        return new double[] { xFinal, yFinal };
    }
}
