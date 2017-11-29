package com.nyletech.nyleride.location;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.nyletech.nyleride.TripSheetActivity;

/**
 * Created by Deepu Pradeep on 08-10-2017.
 */

public class GooglePlayServiceLocation implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult> {

    protected static GoogleApiClient mGoogleApiClient;
    protected LocationRequest locationRequest;
    int REQUEST_CHECK_SETTINGS = 100;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    public static Location loc;

    private Status status;

    private Activity a;

    GPSLocation gpsLocation;

    public GooglePlayServiceLocation(Activity a) {
        this.a = a;
    }

    public void connect() {
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }
    }

    // Method to verify google play services on the device
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(a);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) a, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //Toast.makeText(a, "This device is not supported for Google Play Service.", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }

    // Creating google api client object
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(a)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationSetting();
        //getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        //Toast.makeText(a, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode(), Toast.LENGTH_LONG).show();
    }

    private void locationSetting() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        builder.build()
                );
        result.setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // NO need to show the dialog;
                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                //  GPS disabled show the user a dialog to turn it on
                try {
                    // Show the dialog to ask for enabling GPS
                    status.startResolutionForResult((Activity) a, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    //failed to show dialog
                }
                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are unavailable so not possible to show any dialog now
                Toast.makeText(a, "Location settings are unavailable.", Toast.LENGTH_LONG).show();
                break;
        }
    }

    // Method to get the location
    public Location getLocation() {

        Location current_loc;

        if (ActivityCompat.checkSelfPermission(a, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(a, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //new PermissionsCheck(context);
            Toast.makeText(a, "Please enable the Location permission to our App in settings", Toast.LENGTH_LONG).show();
            return null;
        }
        current_loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (current_loc != null) {
            return current_loc;
        } else {
            gpsLocation = new GPSLocation(a);
            if (gpsLocation.canGetLocation()) {
                current_loc = gpsLocation.getLocation();
            }
            if (current_loc != null) {
                return current_loc;
            } else {
                Toast.makeText(a, "Sorry! unable to find your location.", Toast.LENGTH_LONG).show();
            }
        }

        return loc;
    }
}
