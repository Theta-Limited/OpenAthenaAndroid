package mil.nga.mgrs.tile;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.util.Collection;

import mil.nga.mgrs.MGRS;
import mil.nga.mgrs.grid.GridType;
import mil.nga.mgrs.grid.style.Grid;
import mil.nga.mgrs.grid.style.Grids;

/**
 * MGRS Tile Provider
 *
 * @author wnewman
 * @author osbornb
 */
public class MGRSTileProvider implements TileProvider {

    /**
     * Tile width
     */
    private int tileWidth;

    /**
     * Tile height
     */
    private int tileHeight;

    /**
     * Grids
     */
    private Grids grids;

    /**
     * Create a tile provider with all grids
     *
     * @param context app context
     * @return tile provider
     */
    public static MGRSTileProvider create(Context context) {
        return new MGRSTileProvider(context);
    }

    /**
     * Create a tile provider with grid types
     *
     * @param context app context
     * @param types   grids types to enable
     * @return tile provider
     */
    public static MGRSTileProvider create(Context context, GridType... types) {
        return new MGRSTileProvider(context, types);
    }

    /**
     * Create a tile provider with grid types
     *
     * @param context app context
     * @param types   grids types to enable
     * @return tile provider
     */
    public static MGRSTileProvider create(Context context, Collection<GridType> types) {
        return new MGRSTileProvider(context, types);
    }

    /**
     * Create a tile provider with grids
     *
     * @param context app context
     * @param grids   grids
     * @return tile provider
     */
    public static MGRSTileProvider create(Context context, Grids grids) {
        return new MGRSTileProvider(context, grids);
    }

    /**
     * Create a tile provider with all grids
     *
     * @param tileLength tile width and height
     * @return tile provider
     */
    public static MGRSTileProvider create(int tileLength) {
        return new MGRSTileProvider(tileLength);
    }

    /**
     * Create a tile provider with grid types
     *
     * @param tileLength tile width and height
     * @param types      grids types to enable
     * @return tile provider
     */
    public static MGRSTileProvider create(int tileLength, GridType... types) {
        return new MGRSTileProvider(tileLength, types);
    }

    /**
     * Create a tile provider with grid types
     *
     * @param tileLength tile width and height
     * @param types      grids types to enable
     * @return tile provider
     */
    public static MGRSTileProvider create(int tileLength, Collection<GridType> types) {
        return new MGRSTileProvider(tileLength, types);
    }

    /**
     * Create a tile provider with grids
     *
     * @param tileLength tile width and height
     * @param grids      grids
     * @return tile provider
     */
    public static MGRSTileProvider create(int tileLength, Grids grids) {
        return new MGRSTileProvider(tileLength, grids);
    }

    /**
     * Create a tile provider with all grids
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @return tile provider
     */
    public static MGRSTileProvider create(int tileWidth, int tileHeight) {
        return new MGRSTileProvider(tileWidth, tileHeight);
    }

    /**
     * Create a tile provider with grid types
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @param types      grids types to enable
     * @return tile provider
     */
    public static MGRSTileProvider create(int tileWidth, int tileHeight, GridType... types) {
        return new MGRSTileProvider(tileWidth, tileHeight, types);
    }

    /**
     * Create a tile provider with grid types
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @param types      grids types to enable
     * @return tile provider
     */
    public static MGRSTileProvider create(int tileWidth, int tileHeight, Collection<GridType> types) {
        return new MGRSTileProvider(tileWidth, tileHeight, types);
    }

    /**
     * Create a tile provider with grids
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @param grids      grids
     * @return tile provider
     */
    public static MGRSTileProvider create(int tileWidth, int tileHeight, Grids grids) {
        return new MGRSTileProvider(tileWidth, tileHeight, grids);
    }

    /**
     * Create a tile provider with Grid Zone Designator grids
     *
     * @param context app context
     * @return tile provider
     */
    public static MGRSTileProvider createGZD(Context context) {
        return createGZD(TileUtils.tileLength(context));
    }

    /**
     * Create a tile provider with Grid Zone Designator grids
     *
     * @param tileLength tile length
     * @return tile provider
     */
    public static MGRSTileProvider createGZD(int tileLength) {
        return createGZD(tileLength, tileLength);
    }

    /**
     * Create a tile provider with Grid Zone Designator grids
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @return tile provider
     */
    public static MGRSTileProvider createGZD(int tileWidth, int tileHeight) {
        return new MGRSTileProvider(tileWidth, tileHeight, Grids.createGZD());
    }

    /**
     * Constructor
     *
     * @param context app context
     */
    public MGRSTileProvider(Context context) {
        this(TileUtils.tileLength(context));
    }

    /**
     * Constructor
     *
     * @param context app context
     * @param types   grids types to enable
     */
    public MGRSTileProvider(Context context, GridType... types) {
        this(TileUtils.tileLength(context), types);
    }

    /**
     * Constructor
     *
     * @param context app context
     * @param types   grids types to enable
     */
    public MGRSTileProvider(Context context, Collection<GridType> types) {
        this(TileUtils.tileLength(context), types);
    }

    /**
     * Constructor
     *
     * @param context app context
     * @param grids   grids
     */
    public MGRSTileProvider(Context context, Grids grids) {
        this(TileUtils.tileLength(context), grids);
    }

    /**
     * Constructor
     *
     * @param tileLength tile width and height
     */
    public MGRSTileProvider(int tileLength) {
        this(tileLength, tileLength);
    }

    /**
     * Constructor
     *
     * @param tileLength tile width and height
     * @param types      grids types to enable
     */
    public MGRSTileProvider(int tileLength, GridType... types) {
        this(tileLength, tileLength, types);
    }

    /**
     * Constructor
     *
     * @param tileLength tile width and height
     * @param types      grids types to enable
     */
    public MGRSTileProvider(int tileLength, Collection<GridType> types) {
        this(tileLength, tileLength, types);
    }

    /**
     * Constructor
     *
     * @param tileLength tile width and height
     * @param grids      grids
     */
    public MGRSTileProvider(int tileLength, Grids grids) {
        this(tileLength, tileLength, grids);
    }

    /**
     * Constructor
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     */
    public MGRSTileProvider(int tileWidth, int tileHeight) {
        this(tileWidth, tileHeight, Grids.create());
    }

    /**
     * Constructor
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @param types      grids types to enable
     */
    public MGRSTileProvider(int tileWidth, int tileHeight, GridType... types) {
        this(tileWidth, tileHeight, Grids.create(types));
    }

    /**
     * Constructor
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @param types      grids types to enable
     */
    public MGRSTileProvider(int tileWidth, int tileHeight, Collection<GridType> types) {
        this(tileWidth, tileHeight, Grids.create(types));
    }

    /**
     * Constructor
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @param grids      grids
     */
    public MGRSTileProvider(int tileWidth, int tileHeight, Grids grids) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.grids = grids;
    }

    /**
     * Get the tile width
     *
     * @return tile width
     */
    public int getTileWidth() {
        return tileWidth;
    }

    /**
     * Set the tile width
     *
     * @param tileWidth tile width
     */
    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    /**
     * Get the tile height
     *
     * @return tile height
     */
    public int getTileHeight() {
        return tileHeight;
    }

    /**
     * Set the tile height
     *
     * @param tileHeight tile height
     */
    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    /**
     * Get the grids
     *
     * @return grids
     */
    public Grids getGrids() {
        return grids;
    }

    /**
     * Set the grids
     *
     * @param grids grids
     */
    public void setGrids(Grids grids) {
        this.grids = grids;
    }

    /**
     * Get the grid
     *
     * @param type grid type
     * @return grid
     */
    public Grid getGrid(GridType type) {
        return grids.getGrid(type);
    }

    /**
     * Get the Military Grid Reference System coordinate for the location in one
     * meter precision
     *
     * @param latLng location
     * @return MGRS coordinate
     */
    public String getCoordinate(LatLng latLng) {
        return getMGRS(latLng).coordinate();
    }

    /**
     * Get the Military Grid Reference System coordinate for the location in the
     * zoom level precision
     *
     * @param latLng location
     * @param zoom   zoom level precision
     * @return MGRS coordinate
     */
    public String getCoordinate(LatLng latLng, int zoom) {
        return getCoordinate(latLng, getPrecision(zoom));
    }

    /**
     * Get the Military Grid Reference System coordinate for the location in the
     * grid type precision
     *
     * @param latLng location
     * @param type   grid type precision
     * @return MGRS coordinate
     */
    public String getCoordinate(LatLng latLng, GridType type) {
        return getMGRS(latLng).coordinate(type);
    }

    /**
     * Get the Military Grid Reference System for the location
     *
     * @param latLng location
     * @return MGRS
     */
    public MGRS getMGRS(LatLng latLng) {
        return MGRS.from(TileUtils.toPoint(latLng));
    }

    /**
     * Get the grid precision for the zoom level
     *
     * @param zoom zoom level
     * @return grid type precision
     */
    public GridType getPrecision(int zoom) {
        return grids.getPrecision(zoom);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tile getTile(int x, int y, int zoom) {
        return TileUtils.toTile(drawTile(x, y, zoom));
    }

    /**
     * Draw the tile
     *
     * @param x    x coordinate
     * @param y    y coordinate
     * @param zoom zoom level
     * @return bitmap
     */
    public Bitmap drawTile(int x, int y, int zoom) {
        return grids.drawTile(tileWidth, tileHeight, x, y, zoom);
    }

}
