package com.openathena;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;

import java.io.FileNotFoundException;

public class MarkableImageView extends androidx.appcompat.widget.AppCompatImageView {
    Marker theMarker = null;
    AthenaActivity parent;

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    // Time when last intent was fired
    private long lastIntentTime = 0;
    // Cooldown period of 1 second
    private static final long INTENT_COOLDOWN_MS = 1000;

    public MarkableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!(context instanceof AthenaActivity)) {
            return;
        }
        parent = (AthenaActivity) context;

        scaleGestureDetector = new ScaleGestureDetector(context, new MyScaleGestureListener());
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastIntentTime > INTENT_COOLDOWN_MS) {
                    if (parent instanceof MainActivity){
                        Intent intent = new Intent(parent, SelectionActivity.class);
                        parent.startActivity(intent);
                        lastIntentTime = currentTime;
                    } else if (parent instanceof SelectionActivity) {
                        Intent intent = new Intent(parent, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        parent.startActivity(intent);
                        lastIntentTime = currentTime;
                    }
                }
                return super.onDoubleTap(e);
            }
        });

        MarkableImageView yahweh = this; // reference to this MarkableImageView, for use in listener

        this.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Detect gestures
                scaleGestureDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                long currentTime = System.currentTimeMillis();

                if (event.getAction() == MotionEvent.ACTION_UP && currentTime - lastIntentTime > INTENT_COOLDOWN_MS){
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
                    parent.set_selection_x((int) Math.round(((1.0d * event.getX()) / render_width) * original_width));
                    parent.set_selection_y((int) Math.round(((1.0d * event.getY()) / render_height) * original_height));
                    Log.d("X",parent.get_selection_x() + "");
                    Log.d("Y",parent.get_selection_y() + "");

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

    private class MyScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastIntentTime > INTENT_COOLDOWN_MS) {
                if (parent instanceof MainActivity && detector.getCurrentSpan() > 125 && detector.getTimeDelta() > 75) {
                    Intent intent = new Intent(parent, SelectionActivity.class);
                    parent.startActivity(intent);
                    lastIntentTime = currentTime;
                } else if (parent instanceof SelectionActivity) {
                    final float scaleFactorThreshold = 0.97f;
                    if (detector.getScaleFactor() < scaleFactorThreshold) { // Check for pinch-to-zoom-out gesture
                        Intent intent = new Intent(parent, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        parent.startActivity(intent);
                        lastIntentTime = currentTime;
                    }
                }
            }
            return super.onScale(detector);
        }
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

        parent.calculateImage(this, false); // this may cause the view to re-size due to constraint layout
        mark(x,y);
    }

    /**
     * Given an x and y proportion (range [0, 1]) draw a mark that point
     * @param x The x proportion of a point on this imageView to draw a mark on. 0.0d is left, 0.5d is mid, 1.0d is right
     * @param y The y proportion of a point on this imageView to draw a mark on. 0.0d is top, 0.5d is mid, 1.0d is bottom
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
                theMarker = new Marker(0.5d, 0.5d);
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

    @Override
    /**
     * Override ImageView's setImageURI to make sure we scale down huge images instead of crashing
     */
    public void setImageURI(final Uri uri) {
        if(getWidth() == 0 && getHeight() == 0) {
            ViewTreeObserver vto = getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    int render_width = getMeasuredWidth();
                    int render_height = getMeasuredHeight();
                    Bitmap bitmap = decodeSampledBitmapFromUri(uri, render_width, render_height);
                    setImageBitmap(bitmap);
                    return true;
                }
            });
        } else {
            int render_width = getMeasuredWidth();
            int render_height = getMeasuredHeight();
            Bitmap bitmap = decodeSampledBitmapFromUri(uri, render_width, render_height);
            setImageBitmap(bitmap);
        }
    }

    public Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth, int reqHeight) {
        if (uri == null) {
            return null;
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(parent.getContentResolver().openInputStream(uri), null, options);
        } catch (FileNotFoundException fnfe) {
            Log.e(AthenaActivity.TAG, "Could not find file: " + uri.toString());
            fnfe.printStackTrace();
            return null;
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        try {
            return BitmapFactory.decodeStream(parent.getContentResolver().openInputStream(uri), null, options);
        } catch (FileNotFoundException fnfe) {
            Log.e(AthenaActivity.TAG, "Could not find file: " + uri.toString());
            fnfe.printStackTrace();
            return null;
        }
    }

    public int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
