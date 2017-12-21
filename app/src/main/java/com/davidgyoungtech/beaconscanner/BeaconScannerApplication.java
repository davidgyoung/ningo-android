package com.davidgyoungtech.beaconscanner;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconManagerV3;
import org.altbeacon.beacon.BeaconParser;

public class BeaconScannerApplication extends Application {
    private static final String TAG = BeaconScannerApplication.class.getSimpleName();
    private StringBuilder mLog = new StringBuilder();
    private NingoDataFetcher mNingoDataFetcher;

    @Override
    public void onCreate() {
        super.onCreate();
        BeaconManagerV3.getInstance(this).addBeaconParser(new BeaconParser("ibeacon").setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        BeaconManagerV3.getInstance(this).addBeaconParser(new BeaconParser("eddystone-uid").setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        BeaconManagerV3.getInstance(this).addBeaconParser(new BeaconParser("eddystone-eid").setBeaconLayout("s:0-1=feaa,m:2-2=30,p:3-3:-41,i:4-11"));
        BeaconManagerV3.getInstance(this).addBeaconParser(new BeaconParser("eddystone-tlm").setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        BeaconManagerV3.getInstance(this).addBeaconParser(new BeaconParser("eddystone-url").setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));

        Log.d(TAG, "We have set up "+ BeaconManager.getInstanceForApplication(this).getBeaconParsers().size() + " beacon parsers on BeaconManager: "+BeaconManager.getInstanceForApplication(this)+" and parser collection instance "+BeaconManager.getInstanceForApplication(this).getBeaconParsers());

        // This token was obtained from the API.  See the README from this repo for details on obtaining one for your account
        new Settings(this).saveSetting(Settings.NINGO_READONLY_API_TOKEN, "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoxLCJ3cml0ZWFibGUiOmZhbHNlLCJleHAiOjQ2NjkwMjk2NTF9.2aHrvak4hwpuuvi9uOS9jwtf3ZPXd6nOSOXbDfW9Onk");

        mNingoDataFetcher = NingoDataFetcher.getInstance(this);
        mNingoDataFetcher.fetchIfStale();

        Log.i(TAG, "Beacon scanner starting up.");
    }
}
