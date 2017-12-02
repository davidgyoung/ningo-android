package com.davidgyoungtech.beaconscanner;

import android.app.Application;
import android.util.Log;

import org.altbeacon.beacon.BeaconManagerV3;
import org.altbeacon.beacon.BeaconParser;

public class BeaconScannerApplication extends Application {
    private static final String TAG = BeaconScannerApplication.class.getSimpleName();
    private StringBuilder mLog = new StringBuilder();

    @Override
    public void onCreate() {
        super.onCreate();
        BeaconManagerV3.getInstance(this).addBeaconParser(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        Log.i(TAG, "Beacon scanner starting up.");
    }
}
