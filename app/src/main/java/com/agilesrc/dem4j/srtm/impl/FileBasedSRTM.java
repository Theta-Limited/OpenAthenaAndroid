/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.srtm.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agilesrc.dem4j.BoundingBox;
import com.agilesrc.dem4j.DEM;
import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.Resolution;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;
import com.agilesrc.dem4j.impl.AbstractFileBasedTerrain;
import com.agilesrc.dem4j.srtm.SRTMLevelEnum;

/**
 * <p>
 * The FileBasedSRTM object is the SRTM implementation of the Terrain interface
 * </p>
 * 
 * <p>
 * Organization: AgileSrc LLC (www.agilesrc.com)
 * </p>
 * 
 * @author Mark Horn
 */
public class FileBasedSRTM extends AbstractFileBasedTerrain {
	// =========================================================================
	// CONSTANTS
	// =========================================================================
	private static final Logger _LOGGER = LoggerFactory
			.getLogger(FileBasedSRTM.class);

	private static final int _DATA_SIZE_BYTES = 2;

	// =========================================================================
	// VARIABLES
	// =========================================================================
	private final SRTMLevelEnum _level;
	private final BoundingBox _tile;
	private final Point _origin;

	// =========================================================================
	// CONSTRUCTORS
	// =========================================================================
	/**
	 * @param file
	 * @param level
	 * @throws InstantiationException
	 */
	public FileBasedSRTM(final File file) throws InstantiationException {
		super(file);

		try {
			_level = SRTMLevelEnum.fromFile(file);

			if (_level == null) {
				throw new InstantiationException("SRTM level can not be null");
			}

			if (_LOGGER.isTraceEnabled()) {
				_LOGGER.trace("file size is " + _file.length()
						/ _DATA_SIZE_BYTES);
			}

			String name = file.getName();
			name = StringUtils.substringBefore(name, ".");
			_origin = new Point();

			int northSouth = Integer.parseInt(name.substring(1, 3));
			if (name.startsWith("N")) {
				_origin.setLatitude(northSouth);
			} else {
				_origin.setLatitude(-1 * northSouth);
			}

			int westEast = Integer.parseInt(name.substring(4, 7));
			if (name.contains("E")) {
				_origin.setLongitude(westEast);
			} else {
				_origin.setLongitude(-1 * westEast);
			}

			final Point northEast = _origin.add(
					(_level.getRows() - 1) * _level.getSpacing(),
					(_level.getColumns() - 1) * _level.getSpacing());

			_tile = new BoundingBox(_origin, northEast);
		} catch (final FileNotFoundException e) {
			if (_LOGGER.isErrorEnabled()) {
				_LOGGER.error("could not find file " + file, e);
			}

			throw new InstantiationException(e.getMessage());
		} catch (final IOException e) {
			if (_LOGGER.isErrorEnabled()) {
				_LOGGER.error("could not read file " + file, e);
			}

			throw new InstantiationException(e.getMessage());
		}
	}

	// =========================================================================
	// PUBLIC METHODS
	// =========================================================================
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
	 * @see com.agilesrc.dted4j.Terrain#getResolution()
	 */
	public Resolution getResolution() throws CorruptTerrainException {
		return _level;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dted4j.Terrain#getElevation(com.agilesrc.dted4j.Point)
	 */
	public Elevation getElevation(final Point point)
			throws InvalidValueException, CorruptTerrainException {

		double resolution = _level.getSpacing();
		double elevation = Short.MIN_VALUE;

		double rowAndColumn[] = rowAndColumn(point);

		int vertLocation = (int) Math.round(Math.abs(rowAndColumn[0]));
		int hozLocation = (int) Math.round(Math.abs(rowAndColumn[1]));

		long skipTo = vertLocation * _DATA_SIZE_BYTES * _level.getColumns()
				+ hozLocation * _DATA_SIZE_BYTES;

		try {
			if (skipTo >= _file.length()) {			    
				throw new CorruptTerrainException(
						"ran past end of file requested move to was " + skipTo
								+ " but file length is " + _file.length());
			}

			if (_LOGGER.isTraceEnabled()) {
				_LOGGER.trace("move to data element "
						+ (skipTo / _DATA_SIZE_BYTES + 1) + " out of "
						+ (_file.length() / _DATA_SIZE_BYTES));
			}

			_file.seek(skipTo);
			elevation = _file.readShort();

		} catch (IOException e) {
			if (_LOGGER.isErrorEnabled()) {
				_LOGGER.error("Error reading file", e);
			}

			throw new CorruptTerrainException(e);
		}

		Point actual = new Point(_origin.getLatitude() + (_level.getColumns() - vertLocation - 1)
				* resolution, _origin.getLongitude() + hozLocation * resolution);

		if (elevation == -32768) {
			elevation = Double.NaN;
		}

		return new Elevation(elevation, actual);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dted4j.Terrain#getNorthWestCorner()
	 */
	public Point getNorthWestCorner() throws CorruptTerrainException {
		return new Point(_tile.getNorth(), _tile.getWest());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dted4j.Terrain#getSouthWestCorner()
	 */
	public Point getSouthWestCorner() throws CorruptTerrainException {
		return _origin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dted4j.Terrain#getNorthEastCorner()
	 */
	public Point getNorthEastCorner() throws CorruptTerrainException {
		return new Point(_tile.getNorth(), _tile.getEast());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dted4j.Terrain#getSouthEastCorner()
	 */
	public Point getSouthEastCorner() throws CorruptTerrainException {
		return new Point(_tile.getSouth(), _tile.getEast());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agilesrc.dted4j.Terrain#contains(com.agilesrc.dted4j.Point)
	 */
	public boolean contains(final Point point) {
		return _tile.contains(point);
	}

	// =========================================================================
	// DEFAULT METHODS
	// =========================================================================
	/**
	 * @param point
	 * @return
	 */
	double[] rowAndColumn(final Point point) {
		double resolution = _level.getSpacing();
		double row = _level.getRows()
				- ((point.getLatitude() - _origin.getLatitude()) / resolution) - 1;
		double column = (point.getLongitude() - _origin.getLongitude())
				/ resolution;
		
		return new double[]{row, column};
	}
	
	// =========================================================================
	// PROTECTED METHODS
	// =========================================================================

	// =========================================================================
	// PRIVATE METHODS
	// =========================================================================

	// =========================================================================
	// INNER CLASSES
	// =========================================================================

}
