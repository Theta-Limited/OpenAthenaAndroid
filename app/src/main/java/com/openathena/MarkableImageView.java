package com.openathena;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

public class MarkableImageView extends androidx.appcompat.widget.AppCompatImageView {
    Marker theMarker = null;
    MainActivity parent;

    public MarkableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!(context instanceof MainActivity)) {
            return;
        }
        parent = (MainActivity) context;
        MarkableImageView yahweh = this; // reference to this MarkableImageView, for use in listener

        this.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    if (!parent.isImageLoaded || parent.imageUri == null || parent.iView == null) {
                        return true;
                    }
                    int original_width;
                    int original_height;
                    int[] original_dimensions = parent.getImageDimensionsFromUri(parent.imageUri);
                    if (original_dimensions == null) {
                        return true;
                    } else {
                        original_width = original_dimensions[0];
                        original_height = original_dimensions[1];
                    }
                    double render_width = yahweh.getWidth();
                    double render_height = yahweh.getHeight();
                    parent.selection_x = (int) Math.round(((1.0d * event.getX()) / render_width) * original_width);
                    parent.selection_y = (int) Math.round(((1.0d * event.getY()) / render_height) * original_height);
                    Log.d("X",parent.selection_x+"");
                    Log.d("Y",parent.selection_y+"");

                    if (parent.isImageLoaded && parent.isDEMLoaded) {
                        parent.calculateImage(yahweh, false); // this may cause the view to re-size due to constraint layout
                        yahweh.mark((double) event.getX() / (1.0d * render_width), (double) event.getY() / (1.0d * render_height));
                    }
                }

                return true;
            }

        });

        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (theMarker != null) {
                    // Invalidate the view to redraw the marker at the correct position
                    yahweh.invalidate();
                }
                // Remove the listener to avoid multiple calls
                yahweh.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    /**
     * Given an x and y of a pixel in full-sized image, draw a mark on that same point on this imageView
     * @param selection_x The x coordinate of a pixel in full-sized image. 0 is left side and increases rightward
     * @param selection_y The y coordinate of a pixel in full-sized image. 0 is top side and increases downward
     */
    public void restoreMarker(int selection_x, int selection_y) {
        int[] original_dimensions = parent.getImageDimensionsFromUri(parent.imageUri);
        int original_x = original_dimensions[0];
        int original_y = original_dimensions[1];

        int render_width = getMeasuredWidth();
        int render_height = getMeasuredHeight();

        double x = (1.0d * selection_x) / original_x;
        double y = (1.0d * selection_y) / original_y;

        mark(x,y);
    }

    /**
     * Given an x and y proportion (range [0, 1]) draw a mark that point
     * @param x The x proportion of a point on this imageView to draw a mark on. 0.0d is left, 0.5d is mid, 1.0d is right
     * @param y The y proportion of a point on this imageView to draw a mark on. 0.0d is left, 0.5d is mid, 1.0d is right
     */
    public void mark(double x, double y) {
        theMarker = new Marker(x, y);
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (theMarker != null) {
            float length = Math.max(getWidth() / 48, getHeight() / 48);
            float gap = length / 1.5f;

            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#FE00DD")); // HI-VIS PINK
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(gap);

            float actualX = (float) theMarker.x_prop * getWidth();
            float actualY = (float) theMarker.y_prop * getHeight();

            // Draw horizontal lines
            canvas.drawLine(actualX - length - gap, actualY, actualX - gap, actualY, paint);
            canvas.drawLine(actualX + gap, actualY, actualX + length + gap, actualY, paint);

            // Draw vertical lines
            canvas.drawLine(actualX, actualY - length - gap, actualX, actualY - gap, paint);
            canvas.drawLine(actualX, actualY + gap, actualX, actualY + length + gap, paint);
        } else {
            if (parent.isImageLoaded) {
                theMarker = new Marker(getWidth() / 2, getHeight() / 2);
                invalidate();
            }
        }
    }

    protected class Marker {
        public double x_prop;
        public double y_prop;

        public Marker(double x_prop, double y_prop) {
            this.x_prop = x_prop;
            this.y_prop = y_prop;
        }
    }
}
