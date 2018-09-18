package bg.vivacom.nom.android.polesurvey.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

public class LocationUtils {

    public final static int DEFAULT_INTERVAL = 10000;
    public final static int DEFAULT_FASTEST_INTERVAL = 5000;
    public final static int DEFAULT_ACCURACY = LocationRequest.PRIORITY_HIGH_ACCURACY;

    public static LocationRequest getDefaultLocationRequest() {
        return new LocationRequest()
                .setPriority(DEFAULT_ACCURACY)
                .setInterval(DEFAULT_INTERVAL)
                .setFastestInterval(DEFAULT_FASTEST_INTERVAL);
    }


    public static Task<LocationSettingsResponse> getLocationSettingsForRequest(@NonNull LocationRequest request,
                                                                               @NonNull Activity activity) {
        return getLocationSettingsForRequest(request, activity.getApplicationContext());
    }

    public static Task<LocationSettingsResponse> getLocationSettingsForRequest(@NonNull LocationRequest request,
                                                                               @NonNull Context context) {

        LocationSettingsRequest settingsRequest = new LocationSettingsRequest
                .Builder()
                .addLocationRequest(request)
                .build();

        SettingsClient client = LocationServices.getSettingsClient(context);
        return client.checkLocationSettings(settingsRequest);
    }
}
