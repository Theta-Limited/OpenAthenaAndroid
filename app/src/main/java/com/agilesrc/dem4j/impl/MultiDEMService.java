/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agilesrc.dem4j.BoundingBox;
import com.agilesrc.dem4j.DEM;
import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.Resolution;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;
import com.agilesrc.dem4j.util.MultiDEMPointIterator;

/**
 * <p>
 * The MultiDEMService provides methods for retrieving information from
 * multiple DEM files.  All DEMS should be the same format and the same region
 * </p>
 *
 * <p>
 * Organization: AgileSrc LLC (www.agilesrc.com)
 * </p>
 * 
 * @author Adam Nutt
 */
public class MultiDEMService implements DEM {
	// =============================================================================================
	// CONSTANTS
	// =============================================================================================
	private static final Logger _LOGGER = LoggerFactory
			.getLogger(MultiDEMService.class);

	// =============================================================================================
	// VARIABLES
	// =============================================================================================
	private List<DEM> _dems = null;
	private Resolution _resolution = null;
	private BoundingBox _box = null;

	// =============================================================================================
	// CONSTRUCTORS
	// =============================================================================================
	/**
	 * @param dems
	 * @throws CorruptTerrainException
	 */
	public MultiDEMService(final List<DEM> dems)
			throws CorruptTerrainException {
		if (dems != null && dems.size() > 0) {
			_dems = dems;
		}

		Collections.sort(_dems);

		MultiDEMPointIterator iterator = new MultiDEMPointIterator(dems);
		_resolution = iterator.getResolution();
		_box = iterator.getBounds();
	}

	// =============================================================================================
	// PUBLIC METHODS
	// =============================================================================================
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final DEM other) {
		int result = Integer.MIN_VALUE;

		try {
			result = new CompareToBuilder().append(getSouthWestCorner(),
					other.getSouthWestCorner()).toComparison();
		} catch (CorruptTerrainException e) {
			if (_LOGGER.isErrorEnabled()) {
				_LOGGER.error("Unable to get point information", e);
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dem4j.DEM#getResolution()
	 */
	public Resolution getResolution() throws CorruptTerrainException {
		return _resolution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dem4j.DEM#getElevation(com.agilesrc.dem4j.Point)
	 */
	public Elevation getElevation(final Point point)
			throws InvalidValueException, CorruptTerrainException {
		Elevation result = null;

		for (DEM dem : _dems) {

			if (dem.contains(point)) {
				if (result != null) {
					if (_LOGGER.isWarnEnabled()) {
						_LOGGER.warn("Duplicate points found for " + point);
					}
					if (!dem.getElevation(point).equals(result)) {
						// Ensure that if one value is NaN, the other value is
						// returned.
						if (!Double.isNaN(dem.getElevation(point)
								.getElevation())
								&& Double.isNaN(result.getElevation())) {
							result = dem.getElevation(point);
						} else if (_LOGGER.isErrorEnabled()) {
							_LOGGER.error("Duplicate points did not have matching heights. Found ["
									+ result
									+ "] from index "
									+ (_dems.indexOf(dem) - 1)
									+ " and ["
									+ dem.getElevation(point)
									+ "] from index "
									+ _dems.indexOf(dem) + ".");
						}
					}
				} else {
					result = dem.getElevation(point);
				}
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dem4j.DEM#getElevations(double, double, double, double)
	 */
	public Elevation[][] getElevations(double north, double south, double west,
			double east) throws InvalidValueException, CorruptTerrainException {
		Resolution resolution = getResolution();
		Elevation southWestElev = getElevation(new Point(south, west));
		Point southWest = southWestElev.getPoint();
		int rows = (int) Math.ceil((north - southWest.getLatitude())
				/ resolution.getSpacing()) + 1;
		int cols = (int) Math.ceil((east - southWest.getLongitude())
				/ resolution.getSpacing()) + 1;
		Elevation[][] result = new Elevation[rows][cols];

		result[0][0] = southWestElev;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				if (row == 0 && col == 0) {
					continue;
				}

				double latDiff = row * resolution.getSpacing();
				double lonDiff = col * resolution.getSpacing();
				Point tmp = southWest.add(latDiff, lonDiff);
				result[row][col] = getElevation(tmp);
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dem4j.DEM#destroy()
	 */
	public void destroy() {
		for (DEM dem : _dems) {
			dem.destroy();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dem4j.DEM#contains(com.agilesrc.dem4j.Point)
	 */
	public boolean contains(final Point point) {
		boolean result = false;
		for (DEM dem : _dems) {
			if (dem.contains(point)) {
				result = true;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dem4j.DEM#getNorthWestCorner()
	 */
	public Point getNorthWestCorner() throws CorruptTerrainException {
		return new Point(_box.getNorth(), _box.getWest());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dem4j.DEM#getSouthWestCorner()
	 */
	public Point getSouthWestCorner() throws CorruptTerrainException {
		return new Point(_box.getSouth(), _box.getWest());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dem4j.DEM#getNorthEastCorner()
	 */
	public Point getNorthEastCorner() throws CorruptTerrainException {
		return new Point(_box.getNorth(), _box.getEast());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dem4j.DEM#getSouthEastCorner()
	 */
	public Point getSouthEastCorner() throws CorruptTerrainException {
		return new Point(_box.getSouth(), _box.getEast());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dem4j.DEM#iterator()
	 */
	public Iterator<Point> iterator() {
		MultiDEMPointIterator result = null;
		try {
			result = new MultiDEMPointIterator(_dems);
		} catch (CorruptTerrainException e) {
			if (_LOGGER.isErrorEnabled()) {
				_LOGGER.error("Unable to create iterator.", e);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dem4j.DEM#iterator(double, double, double, double)
	 */
	public Iterator<Point> iterator(double north, double south, double west,
			double east) {
		MultiDEMPointIterator result = null;
		try {
			result = new MultiDEMPointIterator(_dems, north, south, west, east);
		} catch (CorruptTerrainException e) {
			if (_LOGGER.isErrorEnabled()) {
				_LOGGER.error("Unable to create iterator.", e);
			}
		}
		return result;
	}

	// =============================================================================================
	// DEFAULT METHODS
	// =============================================================================================

	// =============================================================================================
	// PROTECTED METHODS
	// =============================================================================================

	// =============================================================================================
	// PRIVATE METHODS
	// =============================================================================================

	// =============================================================================================
	// INNER CLASSES
	// =============================================================================================

}
