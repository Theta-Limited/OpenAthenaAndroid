package com.openathena;

import static com.openathena.AthenaActivity.TAG;

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

    private long lastScaleGestureTime = 0;
    private static final long SCALE_COOLDOWN_MS = 350;

    private float scale = 1f; // Initial scale
    private float translationX = 0f; // Initial translation
    private float translationY = 0f; // Initial translation

    private int activePointerId = MotionEvent.INVALID_POINTER_ID;
    protected boolean isDragging = false;
    protected boolean isScaling = false;

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
            private final float clickThreshold = 25f;
            private float lastX, lastY;


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Detect gestures
                scaleGestureDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);

                final int action = event.getActionMasked();
                int pointerId = -1;
                int pointerIndex = -1;

                if (isScaling) {
                    // Ignore taps if pinch to zoom scaling is being performed
                    return true;
                }

                // record event time
                long currentTime = System.currentTimeMillis();

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        pointerIndex = event.getActionIndex();

                        lastX = event.getX();
                        lastY = event.getY();
                        isDragging = false;

                        activePointerId = event.getPointerId(0);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        pointerIndex = event.findPointerIndex(activePointerId);
                        if (pointerIndex != -1) {
                            float x = event.getX(pointerIndex);
                            float y = event.getY(pointerIndex);
                            float dx = event.getX() - lastX;
                            float dy = event.getY() - lastY;
                            if (!isDragging) {
                                isDragging = Math.sqrt((dx * dx) + (dy * dy)) >= clickThreshold;
                            }
                            if (isDragging) {
                                // Adjust translations based on drag, accounting for scale
                                translationX += dx / scale;
                                translationY += dy / scale;

                                restrictTranslationToContent();

                                invalidate();
                            }
                            lastX = event.getX();
                            lastY = event.getY();
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_CANCEL: {
                        activePointerId = MotionEvent.INVALID_POINTER_ID;
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        if (!isDragging && currentTime - lastScaleGestureTime > SCALE_COOLDOWN_MS) {
                            handleTap(event.getX(), event.getY());
                        }
                        isDragging = false;

                        pointerIndex = event.getActionIndex();
                        pointerId = event.getPointerId(pointerIndex);
                        if (pointerId == activePointerId) {
                            // This was our active pointer going up. Choose a new active pointer and adjust accordingly.
                            // In a multi-pointer scenario, find the next available pointer that isn't lifting up.
                            int newPointerIndex = -1;
                            for (int i = 0; i < event.getPointerCount(); i++) {
                                if (i != pointerIndex) {
                                    newPointerIndex = i;
                                    break;
                                }
                            }
                            if (newPointerIndex != -1) { // We found a new pointer index
                                lastX = event.getX(newPointerIndex);
                                lastY = event.getY(newPointerIndex);
                                activePointerId = event.getPointerId(newPointerIndex);
                            } else {
                                // No valid new pointer found, reset activePointerId
                                activePointerId = MotionEvent.INVALID_POINTER_ID;
                            }
                        }
                        break;
                }
                return true;
            }



            private void handleTap(float x, float y) {
                if (!parent.isImageLoaded || parent.imageUri == null || parent.iView == null) {
                    return;
                }
                int original_width;
                int original_height;
                int[] original_dimensions = parent.getImageDimensionsFromUri(parent.imageUri);
                if (original_dimensions == null) {
                    return;
                } else {
                    original_width = original_dimensions[0];
                    original_height = original_dimensions[1];
                }
                double render_width = yahweh.getWidth();
                double render_height = yahweh.getHeight();

                // Correctly adjust tap coordinates for scale and translation
                float adjustedX = (x - translationX * scale) / scale;
                float adjustedY = (y - translationY * scale) / scale;

                // Calculate the proportion of the tap within the image dimensions
                float proportionX = adjustedX / getWidth();
                float proportionY = adjustedY / getHeight();

                parent.set_selection_x((int) Math.round(proportionX * original_width));
                parent.set_selection_y((int) Math.round(proportionY * original_height));
                Log.d("X", parent.get_selection_x() + "");
                Log.d("Y", parent.get_selection_y() + "");

                if (parent.isImageLoaded && parent.isDEMLoaded) {
                    parent.calculateImage(yahweh, false); // this may cause the view to re-size due to constraint layout
                    yahweh.mark(proportionX, proportionY);
                }
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
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            long currentTime = System.currentTimeMillis();
            lastScaleGestureTime = currentTime;

            isScaling = true; // Scaling begins
            return true;
        }
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            long currentTime = System.currentTimeMillis();
            final float SCALE_SENSITIVITY = 0.5f;
            float lastScaleValue = scale;

            float scaleFactor = detector.getScaleFactor();
            scaleFactor = 1 + SCALE_SENSITIVITY * (scaleFactor - 1);

            scale *= scaleFactor;
            if (currentTime - lastIntentTime > INTENT_COOLDOWN_MS) {
                if (parent instanceof SelectionActivity) {
                    final float scaleThreshold = 0.92f;
                    if (scale < scaleThreshold && lastScaleValue < scaleThreshold) { // Check for pinch-to-zoom-out gesture
                        Intent intent = new Intent(parent, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        parent.startActivity(intent);
                        lastIntentTime = currentTime;
                    }
                }
            }
            scale = Math.max(0.90f, Math.min(scale, 5.0f)); // Constrain scale between 0.9 and 5.0
            lastScaleGestureTime = currentTime;
            restrictTranslationToContent();
            invalidate();
            return super.onScale(detector);
        }
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            long currentTime = System.currentTimeMillis();
            lastScaleGestureTime = currentTime;

            isScaling = false; // Scaling ends
            super.onScaleEnd(detector);
        }
    }

    private void restrictTranslationToContent() {
        float minTransX; float maxTransX;
        float minTransY; float maxTransY;
        if (scale > 1.0f) { // constrain view window to be within content when zoomed
            // Calculate the boundaries for translation
            minTransX = (getWidth() / scale) - getWidth();
            maxTransX = 0.0f;
            minTransY = (getHeight() / scale) - getHeight();
            maxTransY = 0.0f;
        } else { // constrain content to center of view window when un-zoomed
            minTransX = ((getWidth() / scale) - getWidth()) / 2.0f;
            maxTransX = minTransX;
            minTransY = ((getHeight() / scale) - getHeight()) / 2.0f;
            maxTransY = minTransY;
        }
        // Apply boundaries
        translationX = clamp(translationX, minTransX, maxTransX);
        translationY = clamp(translationY, minTransY, maxTransY);
    }

    // Clamp function to restrict a value between a minimum and a maximum
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
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

        double x = (1.0d * selection_x) / original_x;
        double y = (1.0d * selection_y) / original_y;

        parent.calculateImage(this, false); // this may cause the view to re-size due to constraint layout
        mark(x,y);
    }

    public void reset() {
        this.mark(0.5d, 0.5d);
        scale = 1.0f;
        translationX = 0.0f;
        translationY = 0.0f;
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

    private void setMarkerPosition(float x, float y) {
        float x_prop; float y_prop;
        if (getWidth() > 0 && getHeight() > 0) {
            // Adjust the marker position to account for the current zoom and pan
            x_prop = (x - translationX) / (getWidth() * scale);
            y_prop = (y - translationY) /  (getHeight() * scale);
        } else {
            Log.e(TAG, "Error: width or height was zero at time of marking");
            return;
        }
        mark(x_prop, y_prop);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // Save the current canvas matrix
        canvas.save();

        // Apply zoom and translation
        canvas.scale(scale, scale);
        canvas.translate(translationX, translationY);
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

        // Restore the canvas matrix
        canvas.restore();
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
            Log.e(TAG, "Could not find file: " + uri.toString());
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
            Log.e(TAG, "Could not find file: " + uri.toString());
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
