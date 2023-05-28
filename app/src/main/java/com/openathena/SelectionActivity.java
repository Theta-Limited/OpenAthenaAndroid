package com.openathena;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.FileNotFoundException;

public class SelectionActivity extends AthenaActivity{
    public static String TAG = SelectionActivity.class.getSimpleName();
    /**
     * All the important point selection logic is handled by parent, so we don't change much here
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        iView = (MarkableImageView) findViewById(R.id.selection_image_view);

        restorePrefOutputMode();

        isImageLoaded = athenaApp.getBoolean("isImageLoaded");
        isDEMLoaded = athenaApp.getBoolean("isDEMLoaded");

        String storedUriString = athenaApp.getString("imageUri");
        if (storedUriString != null) {
            imageUri = Uri.parse(storedUriString);
            Log.d(TAG, imageUri.getPath());
            AssetFileDescriptor fileDescriptor = null;
            try {
                fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(imageUri , "r");
            } catch(FileNotFoundException e) {
                imageUri = null;
                isImageLoaded = false;
            }
            if (imageUri != null && imageUri.getPath() != null) {
                imageSelected(imageUri);
            }
        }

        if (isImageLoaded) {
            if (get_selection_x() != -1 && get_selection_y() != -1) {
                iView.restoreMarker(get_selection_x(), get_selection_y());
            } else{
                iView.mark(0.5d, 0.5d); // put marker on center of iView if no current selection
            }
        }
    }

    private void imageSelected(Uri uri) {
        if (uri == null) {
            iView.setImageResource(R.drawable.athena); // put up placeholder icon
            return;
        }

        AssetFileDescriptor fileDescriptor;
        try {
            fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(uri , "r");
        } catch(FileNotFoundException e) {
            imageUri = null;
            return;
        }

        long filesize = fileDescriptor.getLength();
        Log.d(TAG, "filesize: " + filesize);
        if (filesize < 1024 * 1024 * 20) { // check if filesize below 20Mb
            iView.setImageURI(uri);
        }  else { // otherwise:
            Toast.makeText(SelectionActivity.this, getString(R.string.image_is_too_large_error_msg), Toast.LENGTH_SHORT).show();
            iView.setImageResource(R.drawable.athena); // put up placeholder icon
        }

        // constrain MarkableImageView rendered aspect ratio to that of original image
        constrainViewAspectRatio();
    }

    @Override
    public void calculateImage(View view) {
        athenaApp.shouldISendCoT = false;
        athenaApp.needsToCalculateForNewSelection = true; // flag in singleton, new target must be calculated
        return; // do nothing
    }

    @Override
    public void calculateImage(View view, boolean shouldISendCoT) {
        athenaApp.shouldISendCoT = false;
        athenaApp.needsToCalculateForNewSelection = true; // flag in singleton, new target must be calculated
        return; // do nothing
    }

    @Override
    protected void onSaveInstanceState(Bundle saveInstanceState) {
        Log.d(TAG,"onSaveInstanceState started");
        super.onSaveInstanceState(saveInstanceState);
        saveStateToSingleton();
    }

    @Override
    protected void saveStateToSingleton() {
        athenaApp.putBoolean("isImageLoaded", isImageLoaded);
    }
}
