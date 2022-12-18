package mil.nga.mgrs.grid.style;

import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

import mil.nga.color.Color;
import mil.nga.mgrs.grid.GridLabeler;
import mil.nga.mgrs.grid.GridType;

/**
 * Grid with Android specific styling
 */
public class Grid extends mil.nga.mgrs.grid.Grid {

    /**
     * Grid line paint by grid type
     */
    private final Map<GridType, Paint> linePaint = new HashMap<>();

    /**
     * Grid label paint
     */
    private Paint labelPaint;

    /**
     * Constructor
     *
     * @param type grid type
     */
    protected Grid(GridType type) {
        super(type);
    }

    /**
     * Get the grid line paint, create if needed
     *
     * @return grid line paint
     */
    public Paint getLinePaint() {
        return getLinePaint(getType());
    }

    /**
     * Get the grid line paint for the grid type, create if needed
     *
     * @param gridType grid type
     * @return grid line paint
     */
    public Paint getLinePaint(GridType gridType) {
        if (gridType == null) {
            gridType = getType();
        }
        Paint paint = linePaint.get(gridType);
        if (paint == null) {
            paint = createLinePaint(gridType);
            linePaint.put(gridType, paint);
        }
        return paint;
    }

    /**
     * Create the grid line paint
     *
     * @return grid line paint
     */
    public Paint createLinePaint() {
        return createLinePaint(getType());
    }

    /**
     * Create the grid line paint
     *
     * @param gridType grid type
     * @return grid line paint
     */
    public Paint createLinePaint(GridType gridType) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth((float) getWidth(gridType));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getColor(gridType).getColorWithAlpha());
        return paint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWidth(double width) {
        super.setWidth(width);
        resetLinePaint(getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColor(Color color) {
        super.setColor(color);
        resetLinePaint(getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColor(GridType gridType, Color color) {
        super.setColor(gridType, color);
        resetLinePaint(gridType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWidth(GridType gridType, double width) {
        super.setWidth(gridType, width);
        resetLinePaint(gridType);
    }

    /**
     * Reset the grid line paint
     */
    public void resetLinePaint() {
        linePaint.clear();
    }

    /**
     * Reset the grid type line paint
     *
     * @param gridType grid type
     */
    public void resetLinePaint(GridType gridType) {
        linePaint.remove(gridType);
    }

    /**
     * Set the grid line paint
     *
     * @param paint grid line paint
     */
    public void setLinePaint(Paint paint) {
        setLinePaint(getType(), paint);
    }

    /**
     * Set the grid line paint
     *
     * @param gridType grid type
     * @param paint    grid line paint
     */
    public void setLinePaint(GridType gridType, Paint paint) {
        linePaint.put(gridType, paint);
    }

    /**
     * Get the grid label paint, create if needed
     *
     * @return grid label paint, null if no labeler
     */
    public Paint getLabelPaint() {
        if (labelPaint == null) {
            createLabelPaint();
        }
        return labelPaint;
    }

    /**
     * Create the grid label paint
     *
     * @return grid label paint, null if no labeler
     */
    public Paint createLabelPaint() {
        GridLabeler labeler = getLabeler();
        if (labeler != null) {
            labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            labelPaint.setColor(labeler.getColor().getColorWithAlpha());
            labelPaint.setTextSize((float) labeler.getTextSize());
            labelPaint.setTypeface(Typeface.MONOSPACE);
        } else {
            labelPaint = null;
        }
        return labelPaint;
    }

    /**
     * Reset the grid label paint
     */
    public void resetLabelPaint() {
        setLabelPaint(null);
    }

    /**
     * Set the grid label paint
     *
     * @param labelPaint grid label paint
     */
    public void setLabelPaint(Paint labelPaint) {
        this.labelPaint = labelPaint;
    }

    /**
     * Reset the grid line and label paint
     */
    public void resetPaint() {
        resetLinePaint();
        resetLabelPaint();
    }

}
