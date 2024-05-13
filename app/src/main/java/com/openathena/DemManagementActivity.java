package com.openathena;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public abstract class DemManagementActivity extends AthenaActivity {
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected boolean isGPSFixInProgress = false;

    protected ProgressBar progressBar;
    // semaphore value will be > 0 if a long process is currently running
    protected int showProgressBarSemaphore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = createLocationListener();
    }

    protected LocationListener createLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLatLonText(location);
                locationManager.removeUpdates(this);
                showProgressBarSemaphore--;
                if (showProgressBarSemaphore<=0) {
                    progressBar.setVisibility(View.GONE);
                }
                isGPSFixInProgress = false;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                if (showProgressBarSemaphore > 0 && isGPSFixInProgress) {
                    showProgressBarSemaphore--;
                    if (showProgressBarSemaphore <= 0) {
                        progressBar.setVisibility(View.GONE);
                    }
                    isGPSFixInProgress = false;
                }
                startActivity(intent);
            }
        };
    }

    protected abstract void updateLatLonText(Location location);

    protected boolean requestPermissionGPS() {
        if (!hasAccessCoarseLocation() && !hasAccessFineLocation()) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, requestNo);
            requestNo++;
        }
        return (hasAccessCoarseLocation() || hasAccessFineLocation());
    }

    protected boolean hasAccessFineLocation() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    protected boolean hasAccessCoarseLocation() {
        return checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    protected void onClickGetPosGPS() {
        // Sanity check to prevent user from spamming the GPS button
        if (isGPSFixInProgress) {
            return;
        }

        showProgressBarSemaphore++;
        progressBar.setVisibility(View.VISIBLE);
        isGPSFixInProgress = true;

        boolean hasGPSAccess = requestPermissionGPS();
        if (hasGPSAccess) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            } catch (SecurityException se) {
                Toast.makeText(this, getString(R.string.permissions_toast_error_msg), Toast.LENGTH_SHORT).show();
                showProgressBarSemaphore--;
                if (showProgressBarSemaphore<=0) {
                    progressBar.setVisibility(View.GONE);
                }
                isGPSFixInProgress = false;
            }
        } else {
            Toast.makeText(this, getString(R.string.permissions_toast_error_msg), Toast.LENGTH_SHORT).show();
            showProgressBarSemaphore--;
            if (showProgressBarSemaphore<=0) {
                progressBar.setVisibility(View.GONE);
            }
            isGPSFixInProgress = false;
        }
    }
}
