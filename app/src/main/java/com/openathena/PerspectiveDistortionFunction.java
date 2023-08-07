package com.openathena;

import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

public class PerspectiveDistortionFunction implements MultivariateJacobianFunction {
    private final double xDistorted, yDistorted, k1, k2, k3, p1, p2;

    public PerspectiveDistortionFunction(double xDistorted, double yDistorted, double k1, double k2, double k3, double p1, double p2) {
        this.xDistorted = xDistorted;
        this.yDistorted = yDistorted;
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public Pair<RealVector, RealMatrix> value(RealVector point) {
        double x = point.getEntry(0);
        double y = point.getEntry(1);
        double r2 = x * x + y * y;

        double xModel = x * (1 + k1 * r2 + k2 * r2 * r2 + k3 * r2 * r2 * r2) + 2 * p1 * x * y + p2 * (r2 + 2 * x * x);
        double yModel = y * (1 + k1 * r2 + k2 * r2 * r2 + k3 * r2 * r2 * r2) + p1 * (r2 + 2 * y * y) + 2 * p2 * x * y;

        RealVector residual = new ArrayRealVector(new double[]{xDistorted - xModel, yDistorted - yModel});

        // Calculate the Jacobian matrix
        double[][] jacobianData = new double[2][2];
        double r4 = r2 * r2;
        double r6 = r4 * r2;

        jacobianData[0][0] = 1 + 3 * k1 * r2 + 5 * k2 * r4 + 7 * k3 * r6 + 2 * p1 * y + 6 * p2 * x;
        jacobianData[0][1] = 2 * p1 * x + 2 * p2 * y;
        jacobianData[1][0] = 2 * p1 * x + 2 * p2 * y;
        jacobianData[1][1] = 1 + 3 * k1 * r2 + 5 * k2 * r4 + 7 * k3 * r6 + 2 * p2 * x + 6 * p1 * y;

        RealMatrix jacobian = MatrixUtils.createRealMatrix(jacobianData);

        return new Pair<>(residual, jacobian);
    }
}
