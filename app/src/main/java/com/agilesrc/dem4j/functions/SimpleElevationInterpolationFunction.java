package com.agilesrc.dem4j.functions;

import com.agilesrc.dem4j.BoundingBox;
import com.agilesrc.dem4j.DEM;
import com.agilesrc.dem4j.DEM.Elevation;
import com.agilesrc.dem4j.ElevationInterpolationFunction;
import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.exceptions.ComputableAreaException;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidPointException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;

/**
 * <p>
 * The A3x3MatrixFunction object is an abstract class that provides filling of a
 * 3x3 matrix for processing by an implementing function
 * </p>
 * <p>
 * Organization: AgileSrc LLC (www.agilesrc.com)
 * </p>
 * 
 * @author Mark Horn
 */
public class SimpleElevationInterpolationFunction extends
    A2x2MatrixFunction<DEM.Elevation> implements
        ElevationInterpolationFunction {
    //=============================================================================================
    // CONSTANTS
    //=============================================================================================

    //=============================================================================================
    // VARIABLES
    //=============================================================================================
    private DEM _source = null;

    //=============================================================================================
    // CONSTRUCTORS
    //=============================================================================================
    /**
     * @param dem
     */
    public SimpleElevationInterpolationFunction(final DEM dem) {
        _source = dem;
    }

    //=============================================================================================
    // PUBLIC METHODS
    //=============================================================================================
    /*
     * (non-Javadoc)
     * @see
     * com.agilesrc.dem4j.ElevationInterpolationFunction#compute(com.agilesrc
     * .dem4j.Point)
     */
    public Elevation compute(final Point point) throws CorruptTerrainException,
            InvalidValueException, InvalidPointException,
            ComputableAreaException {

        Elevation[][] matrix = fill(_source, point);

        BoundingBox box = boundingBox();
        if (box == null || !box.contains(point)) {
            throw new InvalidPointException();
        }

        double result = Double.NaN;
        double[] lats = new double[2];
        double[] lons = new double[2];
        double[][] elevations = new double[2][2];
        
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                final Elevation elevation = matrix[i][j];
                final Point tmp = elevation.getPoint();
                lats[i] = tmp.getLatitude();
                lons[j] = tmp.getLongitude();
                elevations[i][j] = elevation.getElevation();
            }
        }

        try {
            double lat = point.getLatitude();
            double lon = point.getLongitude();
            result = biLinear(lat, lon, elevations[0][0], elevations[0][1], elevations[1][0], 
                    elevations[1][1], lats[0], lats[1], lons[0], lons[1]);
        } catch (final Exception e) {
            throw new InvalidValueException("interpolation failed", e);
        }

        return new Elevation(result, point);
    }

    /**
     * @param x : requested
     * @param x1
     * @param x2
     * @param value1
     * @param value2
     * @return
     */
    public static double linear(final double x, final double x1, final double x2, 
            final double value1, final double value2) {
        return ((x2 - x) / (x2 - x1)) * value1 + ((x - x1) / (x2 - x1)) * value2;
    }

    /**
     * @param x
     * @param y
     * @param value11 : value at x1/y1
     * @param value12 : value at x1/y2
     * @param value21 : value at x2/y1
     * @param value22 : value at x2/y2
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @return
     */
    public static double biLinear(final double x, final double y, final double value11, 
            final double value12, final double value21, final double value22, final double x1, 
            final double x2, final double y1, final double y2) {
        double r1 = linear(x, x1, x2, value11, value21);
        double r2 = linear(x, x1, x2, value12, value22);

        return linear(y, y1, y2, r1, r2);
    }
    //=============================================================================================
    // DEFAULT METHODS
    //=============================================================================================

    //=============================================================================================
    // PROTECTED METHODS
    //=============================================================================================

    //=============================================================================================
    // PRIVATE METHODS
    //=============================================================================================

    //=============================================================================================
    // INNER CLASSES
    //=============================================================================================

}
