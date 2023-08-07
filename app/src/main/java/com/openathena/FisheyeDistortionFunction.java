package com.openathena;

import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

public class FisheyeDistortionFunction implements MultivariateJacobianFunction {
    private final double xDistorted, yDistorted, c, d, e, f;

    public FisheyeDistortionFunction(double xDistorted, double yDistorted, double c, double d, double e, double f) {
        this.xDistorted = xDistorted;
        this.yDistorted = yDistorted;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    @Override
    public Pair<RealVector, RealMatrix> value(RealVector point) {
        double x = point.getEntry(0);
        double y = point.getEntry(1);
        double r = Math.sqrt(x * x + y * y);
        double r2 = r * r;
        double r3 = r2 * r;
        double r5 = r3 * r2;
        double r7 = r5 * r2;

        double rDistorted = c * r + d * r3 + e * r5 + f * r7;

        double xModel = x * rDistorted / r;
        double yModel = y * rDistorted / r;

        RealVector residual = new ArrayRealVector(new double[]{xDistorted - xModel, yDistorted - yModel});

        // Calculate the Jacobian matrix
        double[][] jacobianData = new double[2][2];
        jacobianData[0][0] = (c + 3 * d * r2 + 5 * e * r5 + 7 * f * r7) * x / r;
        jacobianData[0][1] = (c + 3 * d * r2 + 5 * e * r5 + 7 * f * r7) * y / r;
        jacobianData[1][0] = (c + 3 * d * r2 + 5 * e * r5 + 7 * f * r7) * x / r;
        jacobianData[1][1] = (c + 3 * d * r2 + 5 * e * r5 + 7 * f * r7) * y / r;

        RealMatrix jacobian = MatrixUtils.createRealMatrix(jacobianData);

        return new Pair<>(residual, jacobian);
    }
}
