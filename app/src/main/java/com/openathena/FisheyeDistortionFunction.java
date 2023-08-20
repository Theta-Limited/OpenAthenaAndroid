package com.openathena;

import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

public class FisheyeDistortionFunction implements MultivariateJacobianFunction {
    private final double xDistorted, yDistorted, p2, p3, p4, c, d, e, f;

    public FisheyeDistortionFunction(double xDistorted, double yDistorted, double p2, double p3, double p4, double c, double d, double e, double f) {
        this.xDistorted = xDistorted;
        this.yDistorted = yDistorted;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
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

        double theta = (2.0 / Math.PI) * Math.atan(r);
        double rho = theta + p2 * Math.pow(theta, 2) + p3 * Math.pow(theta, 3) + p4 * Math.pow(theta, 4);

        double xh = rho * x / r;
        double yh = rho * y / r;

        double xModel = c * xh + d * yh;
        double yModel = e * xh + f * yh;

        RealVector residual = new ArrayRealVector(new double[]{xDistorted - xModel, yDistorted - yModel});

        // Calculate the Jacobian matrix
        double[][] jacobianData = new double[2][2];

        double drho_dtheta = 1 + 2 * p2 * theta + 3 * p3 * theta * theta + 4 * p4 * theta * theta * theta;
        double dtheta_dx = (-2 * x) / (Math.PI * r * r * (1 + r * r));
        double dtheta_dy = (-2 * y) / (Math.PI * r * r * (1 + r * r));

        jacobianData[0][0] = c * (drho_dtheta * dtheta_dx * x / r - rho * x * x / (r * r * r)) + d * (drho_dtheta * dtheta_dx * y / r - rho * x * y / (r * r * r));
        jacobianData[0][1] = c * (drho_dtheta * dtheta_dy * x / r - rho * x * y / (r * r * r)) + d * (drho_dtheta * dtheta_dy * y / r - rho * y * y / (r * r * r));
        jacobianData[1][0] = e * (drho_dtheta * dtheta_dx * x / r - rho * x * x / (r * r * r)) + f * (drho_dtheta * dtheta_dx * y / r - rho * x * y / (r * r * r));
        jacobianData[1][1] = e * (drho_dtheta * dtheta_dy * x / r - rho * x * y / (r * r * r)) + f * (drho_dtheta * dtheta_dy * y / r - rho * y * y / (r * r * r));

        RealMatrix jacobian = MatrixUtils.createRealMatrix(jacobianData);

        return new Pair<>(residual, jacobian);
    }
}
