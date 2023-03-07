package com.openathena;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class MarkableImageView extends androidx.appcompat.widget.AppCompatImageView {
    Marker theMarker = null;
    MainActivity parent;

    public MarkableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!(context instanceof MainActivity)) {
            return;
        }
        parent = (MainActivity) context;
        MarkableImageView yahweh = this;
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
                    int render_width = yahweh.getWidth();
                    int render_height = yahweh.getHeight();
                    parent.selection_x = (int) (((1.0d * event.getX()) / render_width) * original_width);
                    parent.selection_y = (int) (((1.0d * event.getY()) / render_height) * original_height);
                    Log.d("X",parent.selection_x+"");
                    Log.d("Y",parent.selection_y+"");

                    if (parent.isImageLoaded && parent.isDEMLoaded) {
                        parent.calculateImage(yahweh, false);
                        theMarker = new Marker((int) event.getX(), (int) event.getY());
                        yahweh.invalidate();
                    }
                }

                return true;
            }

        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (theMarker != null) {
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#FE00DD"));
            float radius = Math.max(getWidth()/64, getHeight()/64);
            canvas.drawCircle(theMarker.x, theMarker.y, radius, paint);
        } else {
            if (parent.isImageLoaded) {
                theMarker = new Marker(getWidth() / 2, getHeight() / 2);
                invalidate();
            }
        }
    }

    protected class Marker {
        public int x;
        public int y;

        public Marker(int x, int y) {
            this.x  = x;
            this.y = y;
        }
    }
}
