package com.openathena;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

        restorePrefs();

        isImageLoaded = athenaApp.getBoolean("isImageLoaded");
        isDEMLoaded = athenaApp.getBoolean("isDEMLoaded");

        String storedUriString = athenaApp.getString("imageUri");
        if (storedUriString != null) {
            imageUri = Uri.parse(storedUriString);
            if (imageUri != null && imageUri.getPath() != null) Log.d(TAG, imageUri.getPath());
            AssetFileDescriptor fileDescriptor = null;
            try {
                fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(imageUri , "r");
                if (fileDescriptor != null) fileDescriptor.close();
            } catch(IOException e) {
                imageUri = null;
                isImageLoaded = false;
            }
            if (imageUri != null && imageUri.getPath() != null) {
                imageSelected(imageUri);
            }
        }

        if (isImageLoaded) {
            if (AthenaApp.get_selection_x() != -1 && AthenaApp.get_selection_y() != -1) {
                iView.restoreMarker(AthenaApp.get_selection_x(), AthenaApp.get_selection_y());
            } else{
                iView.mark(0.5d, 0.5d); // put marker on center of iView if no current selection
            }
        }

        // Display UI prompt only when activity is first opened, not upon return to activity (e.g. multitasking switch or screen rotation)
        if (savedInstanceState == null) {
            Toast.makeText(this, R.string.zoom_out_or_press_back_to_return, Toast.LENGTH_SHORT).show();
        }
    }

    private void imageSelected(Uri uri) {
        if (uri == null) {
            iView.setImageResource(R.drawable.athena); // put up placeholder icon
            return;
        }
        
        File appCacheDir = new File(getCacheDir(), "images");
        if (!appCacheDir.exists()) {
            appCacheDir.mkdirs();
        }

        // Android 10/11, we can't access this file directly
        // We will copy the file into app's own package cache
        File fileInCache = new File(appCacheDir, uri.getLastPathSegment());
        if (!isCacheUri(uri)) {
            try {
                try (InputStream inputStream = getContentResolver().openInputStream(uri);
                     OutputStream outputStream = new FileOutputStream(fileInCache)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "FileNotFound imageSelected()");
                    throw e;
                } catch (IOException e) {
                    Log.e(TAG, "IOException imageSelected()");
                    throw e;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            uri = Uri.fromFile(fileInCache); // use the uri of the copy in the cache directory
        }

        AssetFileDescriptor fileDescriptor;
        try {
            fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(uri , "r");
        } catch(FileNotFoundException e) {
            imageUri = null;
            return;
        }

        iView.setImageURI(uri);

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
