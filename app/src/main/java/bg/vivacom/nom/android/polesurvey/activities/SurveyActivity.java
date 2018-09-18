package bg.vivacom.nom.android.polesurvey.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import bg.vivacom.nom.android.polesurvey.R;
import bg.vivacom.nom.android.polesurvey.utils.LocationUtils;
import bg.vivacom.nom.android.polesurvey.utils.PermissionUtils;

public class SurveyActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap = null;

    private Context mContext = null;

    private FusedLocationProviderClient mLocationProvider = null;

    private LocationCallback mLocationCallback = null;

    private final String[] LOCATION_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private final int REQUEST_LOCATION_PERMISSIONS = 6969;

    private final int REQUEST_LOCATION_SETTINGS = 6070;

    private final int REQUEST_CONNECTION_SETTINGS = 6071;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton addPoleButton = findViewById(R.id.fab);
        addPoleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mLocationProvider = LocationServices.getFusedLocationProviderClient(this);
        mContext = this;

        setupLocationCallback();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationTracking();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationTracking();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            if (PermissionUtils.verifyPermission(grantResults)) {
                addLocationRequestToProvider();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startLocationTracking() {
        /*
         * We need to make a request to the system for necessary capabilities.
         */
        final LocationRequest mRequest = LocationUtils.getDefaultLocationRequest();
        Task<LocationSettingsResponse> settingsTask = LocationUtils.getLocationSettingsForRequest(mRequest, this);

        /*
         * Our need are satisfied, so no we can start listening for location updates.
         */
        settingsTask.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                /*
                 * If Android M or later we need to ask permissions for location.
                 */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if( ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_DENIED
                            || ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Locations permission has not been granted yet. Request it directly.
                        ActivityCompat.requestPermissions(SurveyActivity.this, LOCATION_PERMISSIONS,
                                REQUEST_LOCATION_PERMISSIONS);

                    } else {
                        // Permission granted
                        addLocationRequestToProvider();
                    }
                    // No need to check for permissions
                } else {
                    addLocationRequestToProvider();
                }
            }
        });

        /*
         * Sorry, settings are not satisfied, notify user to take some actions.
         */
        settingsTask.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(SurveyActivity.this,
                            REQUEST_LOCATION_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });

    }

    private void stopLocationTracking() {
        mLocationProvider.removeLocationUpdates(mLocationCallback);
    }

    @SuppressLint("MissingPermission")
    private void addLocationRequestToProvider() {
        mLocationProvider.requestLocationUpdates(
                LocationUtils.getDefaultLocationRequest(),
                mLocationCallback,
                null
        );
    }

    private void setupLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (mMap != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                    }
                }
            }
        };
    }
}
