package com.davidgyoungtech.beaconscanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconManagerV3;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dyoung on 11/17/17.
 */

public class BeaconTracker {
    private static BeaconTracker mInstance = null;
    public static final String UPDATE_NOTIFICATION_NAME = "tracked_beacons_updated";
    private static final String TAG = BeaconTracker.class.getSimpleName();
    private Context mContext;
    private HashMap<String,TrackedBeacon> mTrackedBeacons = new HashMap<>();
    private int mSecsBeforeDroppingBeacon = 10;
    private BeaconManagerV3 mBeaconManager;
    private boolean mStarted = false;

    public static BeaconTracker getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BeaconTracker(context);
        }
        return mInstance;
    }

    public void start() {
        if (!mStarted) {
            mBeaconManager.setDebug(true);
            mBeaconManager.startRangingBeaconsInRegion(new Region("allbeacons", null, null, null));
            IntentFilter filter = new IntentFilter(BeaconManagerV3.RANGING_NOTIFICATION_NAME);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mLocalReceiver, filter);
            mStarted = true;
        }
    }

    public void stop() {
        if (mStarted) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mLocalReceiver);
            mBeaconManager.stopRangingBeaconsInRegion(new Region("allbeacons", null, null, null));
            reset();
            mStarted = false;
        }
    }

    public void reset() {
        for (String key: new ArrayList<String>(mTrackedBeacons.keySet())) {
            TrackedBeacon trackedBeacon = mTrackedBeacons.get(key);
            trackedBeacon.setMeasurementsStabilized(false);
            trackedBeacon.setLastMeasurementCount(0);
            trackedBeacon.setTotalPacketsDetected(0);
            trackedBeacon.setTotalRangePeriods(0);
            trackedBeacon.setTotalRangeSamples(0);
        }
    }

    private BeaconTracker() {
    }

    private BeaconTracker(Context context) {
        mContext = context;
        mBeaconManager = BeaconManagerV3.getInstance(mContext);
        Beacon.setHardwareEqualityEnforced(true);
    }


    BroadcastReceiver mLocalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Beacon> beacons = (ArrayList<Beacon>) intent.getSerializableExtra("beacons");
            updateTrackedBeacons(beacons);
            Intent forwardIntent = new Intent(UPDATE_NOTIFICATION_NAME);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(forwardIntent);
            Log.d(TAG, "detected beacon count "+beacons.size());
        }
    };

    public void updateTrackedBeacons(List<Beacon> beaconsDetected) {
        Date now = new Date();
        for (TrackedBeacon trackedBeacon: getTrackedBeacons()) {
            trackedBeacon.setTotalRangePeriods(trackedBeacon.getTotalRangePeriods()+1);
        }
        for (Beacon beacon: beaconsDetected) {
            String key = beacon.toString();
            TrackedBeacon trackedBeacon = mTrackedBeacons.get(beacon.toString());
            if (trackedBeacon == null) {
                trackedBeacon = new TrackedBeacon(beacon);
                mTrackedBeacons.put(key, trackedBeacon);
            }
            else {
                trackedBeacon.setBeacon(beacon);
            }
            trackedBeacon.setLastSeenTime(new Date());
            Log.d(TAG, "beacon packets this range cycle: "+beacon.getPacketCount());
            trackedBeacon.setTotalPacketsDetected(trackedBeacon.getTotalPacketsDetected()+beacon.getPacketCount());
            trackedBeacon.setTotalRangeSamples(trackedBeacon.getTotalRangeSamples()+1);
            if (trackedBeacon.getLastMeasurementCount() > beacon.getMeasurementCount()) {
                if (!trackedBeacon.isMeasurementsStabilized()) {
                    Log.d(TAG, "Measurements stabilized because last count was "+trackedBeacon.getLastMeasurementCount()+" and new count is "+beacon.getMeasurementCount());
                    trackedBeacon.setMeasurementsStabilized(true);
                }
            }
            trackedBeacon.setLastMeasurementCount(beacon.getMeasurementCount());

            if (trackedBeacon.getTotalPacketsDetected() != 0 && trackedBeacon.getTotalRangePeriods() != 0) {
                BeaconManager beaconManagerV2 = BeaconManager.getInstanceForApplication(mContext);
                long rangePeriodMillis = beaconManagerV2.getForegroundScanPeriod()+beaconManagerV2.getForegroundBetweenScanPeriod();
                if (rangePeriodMillis != 0) {
                    trackedBeacon.setPacketsPerSec(trackedBeacon.getTotalPacketsDetected()*1.0/trackedBeacon.getTotalRangePeriods()*1000/rangePeriodMillis);
                }
            }
        }

        for (String key: new ArrayList<String>(mTrackedBeacons.keySet())) {
            if (now.getTime() - mTrackedBeacons.get(key).getLastSeenTime().getTime() > mSecsBeforeDroppingBeacon*1000) {
                mTrackedBeacons.remove(key);
            }
        }
    }

    public Collection<TrackedBeacon> getTrackedBeacons() {
        return mTrackedBeacons.values();
    }

    public TrackedBeacon getTrackedBeacon(Beacon beacon) {
        return mTrackedBeacons.get(beacon.toString());
    }
}
