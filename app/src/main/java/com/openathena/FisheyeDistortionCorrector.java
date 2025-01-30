package com.openathena;

import org.apache.commons.math3.fitting.leastsquares.*;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.util.Pair;

public class FisheyeDistortionCorrector {
    // Forward fisheye polynomial:
    //   theta_dist = p0 + p1*theta_und + p2*theta_und^2 + ...
    // plus a 2x2 affine transform [C D; E F].
    private final double p0, p1, p2, p3, p4;
    private final double C, D, E, F;

    // LM solver settings:
    private final int maxIterations = 50;
    private final double relTol = 1e-10;  // relative tolerance
    private final double absTol = 1e-12;  // absolute tolerance

    public FisheyeDistortionCorrector(
            double p0, double p1, double p2, double p3, double p4,
            double c, double d, double e, double f
    ) {
        this.p0 = p0;  this.p1 = p1;  this.p2 = p2;
        this.p3 = p3;  this.p4 = p4;
        this.C  = c;   this.D  = d;   this.E  = e;   this.F  = f;
    }

    /**
     * Given a distorted normalized point (xDist, yDist),
     * find the undistorted (xU, yU) that reproduces it via the forward model.
     *
     * Implementation uses a small 2D least-squares problem with
     * Levenberg–Marquardt from Apache Commons Math3.
     */
    public double[] correctDistortion(double xDist, double yDist) {
        // If near the principal point, the solution is trivially (0,0).
        double rDist = Math.sqrt(xDist*xDist + yDist*yDist);
        if (rDist < 1e-12) {
            return new double[]{0.0, 0.0};
        }

        // Build an LMA problem: solve for xU,yU s.t. forwardModel(xU,yU) ~ (xDist,yDist).
        // We'll do it in "one shot" each time we call correctDistortion.

        // 1) The function that, given (xU,yU), produces residuals:
        MultivariateJacobianFunction model = new MultivariateJacobianFunction() {
            @Override
            public Pair<RealVector, RealMatrix> value(final RealVector point) {
                // point[0] = xU, point[1] = yU
                double xU = point.getEntry(0);
                double yU = point.getEntry(1);

                // forward model -> predicted xDist, yDist
                double[] distModel = forwardFisheyeModel(xU, yU);

                // residual = predicted - actual
                double rx = distModel[0] - xDist;
                double ry = distModel[1] - yDist;

                // Evaluate the Jacobian wrt xU,yU
                // J = d(rx, ry)/d(xU,yU)  =>  2x2 matrix
                double[][] jac = jacobianFisheyeModel(xU, yU);

                // We'll store partial derivatives of [rx, ry].
                // i.e. derivative of rx wrt xU, derivative of rx wrt yU, etc.
                // Then we'll add them to the final for 2 residual eqns.

                // But note: rx = xDistModel - xDist => partial derivatives are
                // the same as partial derivative of xDistModel wrt xU,yU (since xDist is constant).

                // So we do: Jrx_xU = jacModel[0][0], Jrx_yU = jacModel[0][1]
                // Similarly for ry.

                double jrx_xU = jac[0][0];
                double jrx_yU = jac[0][1];
                double jry_xU = jac[1][0];
                double jry_yU = jac[1][1];

                // Build RealMatrix 2x2
                RealMatrix jMatrix = new org.apache.commons.math3.linear.Array2DRowRealMatrix(
                        new double[][] {
                                { jrx_xU, jrx_yU },
                                { jry_xU, jry_yU }
                        },
                        false
                );

                RealVector residuals = new ArrayRealVector(new double[]{rx, ry}, false);
                return new Pair<>(residuals, jMatrix);
            }
        };

        // 2) Initial guess: for moderate distortion, (xDist,yDist) is a good start
        RealVector initGuess = new ArrayRealVector(new double[]{ xDist, yDist }, false);

        // 3) Build the LM problem
        LeastSquaresProblem lsp = new LeastSquaresBuilder()
                .maxEvaluations(1000)
                .maxIterations(maxIterations)
                .model(model)
                .target(new double[]{0.0, 0.0})
                .start(initGuess)
                .build();

        // 4) Solve
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        LeastSquaresProblem.Evaluation eval = optimizer.optimize(lsp);

        // 5) The solution:
        RealVector solution = eval.getPoint();
        double xU = solution.getEntry(0);
        double yU = solution.getEntry(1);
        return new double[]{ xU, yU };
    }

    /**
     * The forward model: given undistorted (xU,yU),
     * returns the predicted (xDist,yDist) in the lens-distorted plane
     * using the polynomial + 2x2 affine approach.
     */
    private double[] forwardFisheyeModel(double xU, double yU) {
        double rU = Math.sqrt(xU*xU + yU*yU);
        if (rU < 1e-12) {
            // near center
            return new double[]{0.0, 0.0};
        }

        double thetaUnd = Math.atan(rU);  // angle in undist coords
        double thetaDist = p0 + p1*thetaUnd + p2*thetaUnd*thetaUnd
                + p3*Math.pow(thetaUnd,3) + p4*Math.pow(thetaUnd,4);

        double rDist = Math.tan(thetaDist);

        double scale = rDist / rU;
        double xRad = xU*scale;
        double yRad = yU*scale;

        // 2x2 affine
        double xF = C*xRad + D*yRad;
        double yF = E*xRad + F*yRad;
        return new double[]{ xF, yF };
    }

    /**
     * Partial derivatives of forwardFisheyeModel wrt (xU,yU).
     *
     * jac[i][j] = partial of i-th output wrt j-th input
     * i in {0=>xDist,1=>yDist}, j in {0=>xU,1=>yU}.
     */
    private double[][] jacobianFisheyeModel(double xU, double yU) {
        // We'll do the chain rule step by step
        double rU = Math.sqrt(xU*xU + yU*yU);
        if (rU < 1e-12) {
            // near zero => partial derivatives ~ C or E only? We'll approximate zero
            return new double[][]{
                    {0.0, 0.0},
                    {0.0, 0.0}
            };
        }

        // 1) partials of rU wrt xU,yU
        double drU_dxU = xU / rU;
        double drU_dyU = yU / rU;

        // 2) partials of thetaUnd = atan(rU)
        double thetaUnd = Math.atan(rU);
        double dThetaUnd_drU = 1.0 / (1.0 + rU*rU);

        double dThetaUnd_dxU = dThetaUnd_drU * drU_dxU;
        double dThetaUnd_dyU = dThetaUnd_drU * drU_dyU;

        // 3) partials of thetaDist = p0 + p1*thetaUnd + p2*thetaUnd^2 + ...
        double thetaDist = p0 + p1*thetaUnd + p2*thetaUnd*thetaUnd
                + p3*Math.pow(thetaUnd,3) + p4*Math.pow(thetaUnd,4);

        double dThetaDist_dThetaUnd =
                p1
                        + 2.0*p2*thetaUnd
                        + 3.0*p3*thetaUnd*thetaUnd
                        + 4.0*p4*Math.pow(thetaUnd,3);

        double dThetaDist_dxU = dThetaDist_dThetaUnd * dThetaUnd_dxU;
        double dThetaDist_dyU = dThetaDist_dThetaUnd * dThetaUnd_dyU;

        // 4) rDist = tan(thetaDist)
        double rDist = Math.tan(thetaDist);
        double drDist_dThetaDist = 1.0 / Math.cos(thetaDist)*Math.cos(thetaDist); // => sec^2(thetaDist), or 1+tan^2
        // simpler: drDist_dThetaDist = 1 + rDist^2
        // indeed, d(tan(z))/dz = 1+tan^2(z)
        double drDist_dThetaDist_simpl = 1.0 + rDist*rDist;

        double drDist_dxU = drDist_dThetaDist_simpl * dThetaDist_dxU;
        double drDist_dyU = drDist_dThetaDist_simpl * dThetaDist_dyU;

        // 5) scale = rDist / rU
        // partials wrt xU,yU:
        // dScale_dxU = (drDist_dxU * rU - rDist*drU_dxU) / (rU^2)
        // dScale_dyU similarly
        double scale = rDist / rU;
        double dScale_dxU = (drDist_dxU*rU - rDist*drU_dxU)/(rU*rU);
        double dScale_dyU = (drDist_dyU*rU - rDist*drU_dyU)/(rU*rU);

        // 6) xRad = xU*scale; yRad = yU*scale
        // dxRad_dxU = scale + xU*dScale_dxU
        // dxRad_dyU = xU*dScale_dyU
        double dxRad_dxU = scale + xU*dScale_dxU;
        double dxRad_dyU = xU*dScale_dyU;

        double dyRad_dxU = yU*dScale_dxU;
        double dyRad_dyU = scale + yU*dScale_dyU;

        // 7) xDist = C*xRad + D*yRad; yDist = E*xRad + F*yRad
        // partial wrt xU,yU => chain rule
        // dxDist_dxU = C*dxRad_dxU + D*dyRad_dxU etc.
        double dxDist_dxU = C*dxRad_dxU + D*dyRad_dxU;
        double dxDist_dyU = C*dxRad_dyU + D*dyRad_dyU;

        double dyDist_dxU = E*dxRad_dxU + F*dyRad_dxU;
        double dyDist_dyU = E*dxRad_dyU + F*dyRad_dyU;

        return new double[][] {
                { dxDist_dxU, dxDist_dyU },
                { dyDist_dxU, dyDist_dyU }
        };
    }
}
