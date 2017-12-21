package com.davidgyoungtech.beaconscanner;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by dyoung on 12/11/17.
 */

public class LocationAccessor {
    private static final String TAG = LocationAccessor.class.getSimpleName();


    public Location getLocation(Context context) {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        Double newestLatitude = null;
        Double newestLongitude = null;
        Location newestLocation= null;
        long newestTime = 0;

        for (String provider : locationManager.getAllProviders()) {
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null && location.getTime() > newestTime) {
                    newestLocation = location;
                    newestTime = location.getTime();
                    newestLatitude = location.getLatitude();
                    newestLongitude = location.getLongitude();
                }
            }
            catch (SecurityException e) {
                Log.d(TAG, "I do not have permission to access location from provider: "+provider+".  No worries.  We will use others.");
            }
        }
        if (newestLatitude != null && newestLongitude!= null) {
            Log.d(TAG, "Location is known");
            Log.d(TAG, "Current location is "+newestLatitude+", "+newestLongitude);
            return newestLocation;
        }
        else {
            Log.d(TAG, "Location is not known");
            return null;
        }

    }
}
