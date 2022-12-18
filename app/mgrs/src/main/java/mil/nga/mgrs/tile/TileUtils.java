package mil.nga.mgrs.tile;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Tile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import mil.nga.grid.features.Point;

/**
 * Tile Utils
 *
 * @author wnewman
 * @author osbornb
 */
public class TileUtils {

    /**
     * Displayed device-independent pixels
     */
    public static final int TILE_DP = 256;

    /**
     * Tile pixels for default dpi tiles
     */
    public static final int TILE_PIXELS_DEFAULT = TILE_DP;

    /**
     * Tile pixels for high dpi tiles
     */
    public static final int TILE_PIXELS_HIGH = TILE_PIXELS_DEFAULT * 2;

    /**
     * High density scale
     */
    public static final float HIGH_DENSITY = ((float) DisplayMetrics.DENSITY_HIGH) / DisplayMetrics.DENSITY_DEFAULT;

    /**
     * Get the tile side (width and height) dimension based upon the app context display density scale
     *
     * @param context app context
     * @return default tile length
     */
    public static int tileLength(Context context) {
        return tileLength(context.getResources().getDisplayMetrics().density);
    }

    /**
     * Get the tile side (width and height) dimension based upon the display density scale
     *
     * @param density display density: {@link android.util.DisplayMetrics#density}
     * @return default tile length
     */
    public static int tileLength(float density) {
        int length;
        if (density < HIGH_DENSITY) {
            length = TILE_PIXELS_DEFAULT;
        } else {
            length = TILE_PIXELS_HIGH;
        }
        return length;
    }

    /**
     * Get the tile density based upon the display density scale and tile dimensions
     *
     * @param density    display density: {@link android.util.DisplayMetrics#density}
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @return tile density
     */
    public static float tileDensity(float density, int tileWidth, int tileHeight) {
        return tileDensity(density, Math.min(tileWidth, tileHeight));
    }

    /**
     * Get the tile density based upon the display density scale and tile length (width or height)
     *
     * @param density    display density: {@link android.util.DisplayMetrics#density}
     * @param tileLength tile length (width or height)
     * @return tile density
     */
    public static float tileDensity(float density, int tileLength) {
        return density * TILE_DP / tileLength;
    }

    /**
     * Get the density based upon the tile dimensions
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @return density
     */
    public static float density(int tileWidth, int tileHeight) {
        return density(Math.min(tileWidth, tileHeight));
    }

    /**
     * Get the density based upon the tile length (width or height)
     *
     * @param tileLength tile length (width or height)
     * @return density
     */
    public static float density(int tileLength) {
        return ((float) tileLength) / TILE_DP;
    }

    /**
     * Compress the bitmap to a byte array
     *
     * @param bitmap bitmap
     * @return bytes
     */
    public static byte[] toBytes(Bitmap bitmap) {

        byte[] bytes = null;

        if (bitmap != null) {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
                bytes = byteStream.toByteArray();
            } finally {
                try {
                    byteStream.close();
                } catch (IOException e) {
                    Log.w(TileUtils.class.getSimpleName(),
                            "Failed to close bitmap compression byte stream", e);
                }
            }
        }

        return bytes;
    }

    /**
     * Compress the bitmap to a tile
     *
     * @param bitmap bitmap
     * @return tile
     */
    public static Tile toTile(Bitmap bitmap) {

        Tile tile = null;

        if (bitmap != null) {

            byte[] bytes = toBytes(bitmap);

            if (bytes != null) {
                tile = new Tile(bitmap.getWidth(), bitmap.getHeight(), bytes);
            }

            bitmap.recycle();
        }

        return tile;
    }

    /**
     * Convert a map coordinate to a point
     *
     * @param latLng map coordinate
     * @return point
     */
    public static Point toPoint(LatLng latLng) {
        return Point.degrees(latLng.longitude, latLng.latitude);
    }

    /**
     * Convert a point to a map coordinate
     *
     * @param point point
     * @return latLng
     */
    public static LatLng toLatLng(Point point) {
        point = point.toDegrees();
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

}
