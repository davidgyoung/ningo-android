package com.davidgyoungtech.beaconscanner;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.altbeacon.ningo.Beacon;
import org.altbeacon.ningo.QueryBeaconClient;

import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dyoung on 12/11/17.
 */

public class NingoDataFetcher {
    private static final String TAG = NingoDataFetcher.class.getSimpleName();
    private static double QUERY_RADIUS_METERS = 10000;
    private static long REFRESH_INTERVAL_MILLIS = 1000 * 3600; // 1 hour
    private static long RETRY_INTERVAL_MILLIS = 1000 * 60; // 1 minute

    Context mContext;
    LocationAccessor mLocationAccessor;
    List<Beacon> mNingoBeacons = new ArrayList<Beacon>();
    long mLastRefreshTime = 0;
    long mLastFailedRefreshTime = 0;
    QueryBeaconClient mQueryBeaconClient;

    private static NingoDataFetcher sInstance;
    public static NingoDataFetcher getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NingoDataFetcher(context.getApplicationContext());
        }
        return sInstance;
    }
    private NingoDataFetcher(Context context) {
        mLocationAccessor = new LocationAccessor();
        mContext = context;
    }

    public void fetchIfStale() {
        long now = System.currentTimeMillis();
        if (mLastFailedRefreshTime > 0 && now - mLastFailedRefreshTime > REFRESH_INTERVAL_MILLIS) {
            Log.d(TAG, "It is try to retry fetching ningo data");
            fetch();
        }
        if (now-mLastRefreshTime > REFRESH_INTERVAL_MILLIS && now-mLastFailedRefreshTime > RETRY_INTERVAL_MILLIS) {
            Log.d(TAG, "It is time to fetch ningo data.");
            fetch();
        }
    }

    public List<Beacon> getNingoBeacons() {
        return mNingoBeacons;
    }

    public Beacon getNingoBeaconForBeacon(org.altbeacon.beacon.Beacon beacon) {
        String ningoIdentifier = makeNingoIdentifier(beacon);
        for (Beacon ningoBeacon : getNingoBeacons()) {
            if (ningoBeacon.getIdentifier().equalsIgnoreCase(ningoIdentifier)) {
                return ningoBeacon;
            }
        }
        return null;
    }

    private String makeNingoIdentifier(org.altbeacon.beacon.Beacon beacon) {
        StringBuilder ningoIdentifierBuilder = new StringBuilder();
        for (Identifier identifier : beacon.getIdentifiers()) {
            ningoIdentifierBuilder.append(identifier.toString()).append("_");
        }
        if (beacon.getParserIdentifier() != null) {
            ningoIdentifierBuilder.append(beacon.getParserIdentifier());
        }
        else if (beacon.getBeaconTypeCode() == 0x0215) {
            ningoIdentifierBuilder.append("ibeacon");
        }
        else if (beacon.getBeaconTypeCode() == 0xbeac) {
            ningoIdentifierBuilder.append("altbeacon");
        }
        else if (beacon.getServiceUuid() == 0xfeaa) {
            if (beacon.getBeaconTypeCode() == 0x00 ) {
                ningoIdentifierBuilder.append("eddystone-uid");
            }
            else if (beacon.getBeaconTypeCode() == 0x10 ) {
                ningoIdentifierBuilder.append("eddystone-url");
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
        return ningoIdentifierBuilder.toString();
    }

    private void fetch() {
        String token = new Settings(mContext).getSetting(Settings.NINGO_READONLY_API_TOKEN, null);
        if (token != null) {
            Location location = mLocationAccessor.getLocation(mContext);
            if (location != null) {
                mQueryBeaconClient = new QueryBeaconClient(token);
                mQueryBeaconClient.query(location.getLatitude(), location.getLongitude(), QUERY_RADIUS_METERS, new QueryBeaconClient.QueryBeaconClientResponseHandler() {
                    @Override
                    public void onFail(Exception e) {
                        Log.d(TAG, "Failed to query beacons from ningo based on distance", e);
                        mLastFailedRefreshTime = System.currentTimeMillis();
                    }

                    @Override
                    public void onResponse(List<Beacon> beacons, String errorCode, String errorDescription) {
                        if (errorCode != null) {
                            Log.d(TAG, "Failed to query beacons from ningo based on distance: "+errorCode);
                            mLastFailedRefreshTime = System.currentTimeMillis();
                        }
                        else {
                            mLastRefreshTime = System.currentTimeMillis();
                            mLastFailedRefreshTime = 0l;
                            mNingoBeacons = beacons;
                        }
                    }
                });
            }
        }
    }

}
