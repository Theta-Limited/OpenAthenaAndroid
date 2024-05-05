package com.openathena;

import static com.openathena.AthenaActivity.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
    // Time when last scale gesture was performed

    // Cooldown period of 1 second
    private static final long INTENT_COOLDOWN_MS = 1000;

    private long lastScaleGestureTime = 0;
    private static final long SCALE_COOLDOWN_MS = 350;
    private static final long SCALE_EXIT_FULLSCREEN_COOLDOWN_MS = 550;
    private static float lastScaleValue = 1.0f;

    private float screen_pixel_density = getResources().getDisplayMetrics().density;

    private Matrix matrix = new Matrix();

    private int activePointerId = MotionEvent.INVALID_POINTER_ID;
    protected boolean isDragging = false;
    protected boolean isScaling = false;

    @SuppressLint("ClickableViewAccessibility")
    public MarkableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!(context instanceof AthenaActivity)) {
            return;
        }
        parent = (AthenaActivity) context;

        DisplayMetrics metrics = new DisplayMetrics();
        final float clickThreshold = 6f * screen_pixel_density;

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
            private float lastX, lastY;


            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Detect gestures
                scaleGestureDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);

                final int action = event.getActionMasked();
                int pointerIndex = event.getActionIndex();
                int pointerId = event.getPointerId(pointerIndex);

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
                        if (isScaling) {
                            // Ignore move if pinch to zoom scaling is being performed
                            return true;
                        }
                        if (pointerIndex != -1 && event.getPointerCount() == 1) {
                            float x = event.getX(pointerIndex);
                            float y = event.getY(pointerIndex);
                            float dx = x - lastX;
                            float dy = y - lastY;
                            if (!isDragging) {
                                isDragging = Math.sqrt((dx * dx) + (dy * dy)) >= clickThreshold;
                            }

                            if (isDragging) {
                                matrix.postTranslate(event.getX() - lastX, event.getY() - lastY);
                                restrictTranslationToContent();
                                setImageMatrix(matrix);
                            }
                            lastX = x;
                            lastY = y;
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        // When a new pointer is added, evaluate and possibly reset the drag start coordinates
                        if (event.getPointerCount() == 2) {  // Start of a pinch-to-zoom gesture
                            int newPointerIndex = (pointerIndex == 0) ? 1 : 0;
                            lastX = event.getX(newPointerIndex);
                            lastY = event.getY(newPointerIndex);
                            activePointerId = event.getPointerId(newPointerIndex);
                        }
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

                int[] original_dimensions = parent.getImageDimensionsFromUri(parent.imageUri);
                if (original_dimensions == null) {
                    return;
                }

                int original_width = original_dimensions[0];
                int original_height = original_dimensions[1];

                // Get the inverse of the current matrix transformation
                Matrix inversedMatrix = new Matrix();
                matrix.invert(inversedMatrix);

                // Transform the tap coordinates to the original image coordinates
                float[] transformedCoords = { x, y };
                inversedMatrix.mapPoints(transformedCoords);

                // Calculate the proportion of the tap within the image dimensions
                float proportionX = transformedCoords[0] / getWidth();
                float proportionY = transformedCoords[1] / getHeight();

                parent.set_selection_x((int) Math.round(proportionX * original_width));
                parent.set_selection_y((int) Math.round(proportionY * original_height));

                if (parent.isImageLoaded && parent.isDEMLoaded) {
                    parent.calculateImage(yahweh, false);
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
//            long currentTime = System.currentTimeMillis();
//            lastScaleGestureTime = currentTime;

            isScaling = true; // Scaling begins
            isDragging = false;

            return true;
        }
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            long currentTime = System.currentTimeMillis();
            float adjustScaleFactor = detector.getScaleFactor();

            // Get current scale
            float[] matrixValues = new float[9];
            matrix.getValues(matrixValues);
            float currentScale = matrixValues[Matrix.MSCALE_X]; // Assumes uniform scaling

            // Calculate the scale factor to apply considering the constraints
            float targetScale = currentScale * adjustScaleFactor;
            if (currentTime - lastIntentTime > INTENT_COOLDOWN_MS) {
                if (parent instanceof SelectionActivity) {
                    final float scaleThreshold = 0.92f;
                    if (targetScale < scaleThreshold && lastScaleValue < scaleThreshold) { // Check for pinch-to-zoom-out gesture
                        if (currentTime - lastScaleGestureTime > SCALE_EXIT_FULLSCREEN_COOLDOWN_MS) {
                            Intent intent = new Intent(parent, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            parent.startActivity(intent);
                            lastIntentTime = currentTime;
                        }
                    }
                }
            }

            float clampedScaleFactor = clamp(targetScale, 0.90f, 5.0f) / currentScale;

            matrix.postScale(clampedScaleFactor, clampedScaleFactor, detector.getFocusX(), detector.getFocusY());
            restrictTranslationToContent();
            setImageMatrix(matrix);

            return true;
        }
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            long currentTime = System.currentTimeMillis();
            lastScaleGestureTime = currentTime;

            isScaling = false; // Scaling ends
            isDragging = false;

            float[] matrixValues = new float[9];
            matrix.getValues(matrixValues);
            lastScaleValue = matrixValues[Matrix.MSCALE_X]; // Assumes uniform scaling

            super.onScaleEnd(detector);
        }
    }

    private void restrictTranslationToContent() {
        // Get drawable dimensions
        float drawableWidth = getDrawable().getIntrinsicWidth();
        float drawableHeight = getDrawable().getIntrinsicHeight();

        // Create a matrix values array
        float[] values = new float[9];
        matrix.getValues(values);

        // Extract the scale and translation from the matrix
        float scale = values[Matrix.MSCALE_X]; // Assuming uniform scale in X and Y
        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];

        float minTransX; float maxTransX;
        float minTransY; float maxTransY;

        if (scale > 1.0f) {
            minTransX = getWidth() - getWidth() * scale; // Maximum leftward translation
            minTransY = getHeight() - getHeight() * scale; // Maximum upward translation
            maxTransX = 0; // Image cannot translate right beyond original position
            maxTransY = 0; // Image cannot translate down beyond original position
        } else {
            minTransX = (getWidth() - getWidth() * scale) / 2.0f;
            maxTransX = minTransX;
            minTransY = (getHeight() - getHeight() * scale) / 2.0f;
            maxTransY = minTransY;
        }
        // Clamp current translations
        float clampedTransX = clamp(transX, minTransX, maxTransX);
        float clampedTransY = clamp(transY, minTransY, maxTransY);

        // Calculate the delta to correct any out-of-bound positions
        float deltaX = clampedTransX - transX;
        float deltaY = clampedTransY - transY;

        // Apply translation to the matrix
        matrix.postTranslate(deltaX, deltaY);
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
        matrix.reset();
        setImageMatrix(matrix);
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
        float x_prop, y_prop;
        if (getWidth() > 0 && getHeight() > 0) {
            // Get the inverse of the current matrix transformation
            Matrix inversedMatrix = new Matrix();
            matrix.invert(inversedMatrix);

            // Transform the coordinates to the original image coordinates
            float[] transformedCoords = {x, y};
            inversedMatrix.mapPoints(transformedCoords);

            // Calculate the marker position as a proportion of the view dimensions
            x_prop = transformedCoords[0] / getWidth();
            y_prop = transformedCoords[1] / getHeight();
        } else {
            Log.e(TAG, "Error: width or height was zero at time of marking");
            return;
        }
        mark(x_prop, y_prop);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Save the current canvas state
        canvas.save();

        // Apply the combined transformations from the matrix
        canvas.concat(matrix);

        // Draw the image
        super.onDraw(canvas);

        // Draw the marker if it exists
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
            // If no marker exists and the image is loaded, create a default marker at the center
            if (parent.isImageLoaded) {
                theMarker = new Marker(0.5d, 0.5d);
                invalidate();
            }
        }

        // Restore the canvas state
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
