package com.openathena;

import org.apache.commons.math3.fitting.leastsquares.*;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

/**
 * Inverts the standard Brown–Conrady perspective-lens distortion model
 * using a Levenberg–Marquardt numerical solve.
 *
 * Forward model (given x_u,y_u => x_dist,y_dist):
 *
 *   r^2 = x_u^2 + y_u^2
 *   radial = 1 + k1*r^2 + k2*r^4 + k3*r^6
 *   x_dist = x_u*radial + 2*p1*x_u*y_u + p2*(r^2 + 2*x_u^2)
 *   y_dist = y_u*radial + p1*(r^2 + 2*y_u^2) + 2*p2*x_u*y_u
 *
 * We find (x_u,y_u) for a given (x_dist,y_dist).
 */
public class PerspectiveDistortionCorrector {
    private final double k1, k2, k3; // Radial
    private final double p1, p2;     // Tangential

    private final int maxIterations = 100;  // Tweak as needed

    public PerspectiveDistortionCorrector(double k1, double k2, double k3, double p1, double p2) {
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * Given a distorted normalized point (xDist, yDist),
     * solve for the undistorted (xU, yU) via a LM least-squares.
     */
    public double[] correctDistortion(double xDist, double yDist) {
        // "A simplification of the standard OpenCV model with the denominator coefficients and tangential coefficients omitted."
        // https://support.skydio.com/hc/en-us/articles/4417425974683-Skydio-camera-and-metadata-overview
        // simplified distortion correction model based on Brown-Conrady model, omitting some terms
        //  A Flexible New Technique for Camera Calibration, 1998
        // https://www.microsoft.com/en-us/research/wp-content/uploads/2016/02/tr98-71.pdf
        
        // If near zero, trivial solution
        double rDist = Math.sqrt(xDist*xDist + yDist*yDist);
        if (rDist < 1e-12) {
            return new double[]{0.0, 0.0};
        }

        // Build the LMA problem: we want (xU, yU) s.t. forwardModel(xU,yU) ~ (xDist, yDist).
        MultivariateJacobianFunction model = new MultivariateJacobianFunction() {
            @Override
            public Pair<RealVector, RealMatrix> value(RealVector point) {
                double xU = point.getEntry(0);
                double yU = point.getEntry(1);

                // 1) Forward-distort to get predicted (xDistModel,yDistModel)
                double[] distModel = forwardBrownConrady(xU, yU);

                // residual = predicted - actual
                double rx = distModel[0] - xDist;
                double ry = distModel[1] - yDist;

                // 2) Jacobian
                double[][] j = jacobianBrownConrady(xU, yU);

                RealMatrix jacobian = new Array2DRowRealMatrix(new double[][]{
                        { j[0][0], j[0][1] },  // partials of rx wrt (xU,yU)
                        { j[1][0], j[1][1] }   // partials of ry wrt (xU,yU)
                }, false);

                RealVector residuals = new ArrayRealVector(new double[]{rx, ry}, false);
                return new Pair<>(residuals, jacobian);
            }
        };

        // Start guess: (xDist,yDist) is often close enough
        RealVector initGuess = new ArrayRealVector(new double[]{ xDist, yDist }, false);

        // Build problem
        LeastSquaresProblem lsp = new LeastSquaresBuilder()
                .maxEvaluations(1000)
                .maxIterations(maxIterations)
                .model(model)
                .target(new double[]{ 0.0, 0.0 })  // residual = 0
                .start(initGuess)
                .build();

        // Solve
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        LeastSquaresProblem.Evaluation eval = optimizer.optimize(lsp);

        RealVector solution = eval.getPoint();
        double xU = solution.getEntry(0);
        double yU = solution.getEntry(1);
        return new double[]{ xU, yU };
    }

    /**
     * Forward Brown–Conrady for (xU,yU).
     * Returns [ xDist, yDist ] in normalized coords.
     */
    private double[] forwardBrownConrady(double xU, double yU) {
        double r2 = xU*xU + yU*yU;
        double r4 = r2*r2;
        double r6 = r4*r2;

        double radial = 1.0 + k1*r2 + k2*r4 + k3*r6;

        // tangential
        double xTan = 2.0*p1*xU*yU + p2*(r2 + 2.0*xU*xU);
        double yTan = p1*(r2 + 2.0*yU*yU) + 2.0*p2*xU*yU;

        double xDist = xU*radial + xTan;
        double yDist = yU*radial + yTan;
        return new double[]{xDist, yDist};
    }

    /**
     * Jacobian of the forward Brown–Conrady model wrt (xU,yU).
     * j[i][j] = partial of i-th output wrt j-th input
     *  i=0 => xDist, i=1 => yDist; j=0 => xU, j=1 => yU.
     */
    private double[][] jacobianBrownConrady(double xU, double yU) {
        double r2 = xU*xU + yU*yU;
        double r4 = r2*r2;
        double r6 = r4*r2;
        double radial = 1.0 + k1*r2 + k2*r4 + k3*r6;

        // partial of radial wrt xU:
        // d(r2)/dxU = 2xU; so
        // d(radial)/dxU = k1*2xU + k2*4xU*r2 + k3*6xU*r4
        double dradial_dxU = (2.0*k1*xU) + (4.0*k2*xU*r2) + (6.0*k3*xU*r4);
        double dradial_dyU = (2.0*k1*yU) + (4.0*k2*yU*r2) + (6.0*k3*yU*r4);

        // tangential terms
        // xTan = 2p1 xU yU + p2(r2 + 2xU^2)
        // partial wrt xU => 2p1 yU + p2(2xU*2 + 2xU*r2?)
        // Actually carefully:
        //   xTan = 2 p1 xU yU + p2(r2 + 2 xU^2)
        // d(xTan)/dxU = 2p1 yU + p2( (d r2/dxU) + 4xU )
        // d(r2)/dxU= 2xU => so => p2( 2xU + 4xU ) => p2 * 6xU
        double dxTan_dxU = 2.0*p1*yU + p2*(2.0*xU + 4.0*xU);
        //  = 2 p1 yU + 6 p2 xU
        double dxTan_dyU = 2.0*p1*xU + p2*(2.0*yU);

        // yTan = p1(r2 + 2yU^2) + 2p2 xU yU
        // d(yTan)/dxU = p1(d(r2)/dxU) + 2p2 yU
        // d(r2)/dxU=2xU => p1(2xU)
        double dyTan_dxU = p1*(2.0*xU) + 2.0*p2*yU;
        double dyTan_dyU = p1*(2.0*yU) + 2.0*p2*xU;

        // xDist = xU*radial + xTan
        // partial wrt xU => radial + xU dradial/dxU + dxTan/dxU
        double dxDist_dxU = radial + xU*dradial_dxU + dxTan_dxU;
        double dxDist_dyU = xU*dradial_dyU + dxTan_dyU;

        // yDist = yU*radial + yTan
        double dyDist_dxU = yU*dradial_dxU + dyTan_dxU;
        double dyDist_dyU = radial + yU*dradial_dyU + dyTan_dyU;

        return new double[][] {
                { dxDist_dxU, dxDist_dyU },
                { dyDist_dxU, dyDist_dyU }
        };
    }
}
