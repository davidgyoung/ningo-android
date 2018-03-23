package com.davidgyoungtech.beaconscanner;

/**
 * Created by dyoung on 12/2/17.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.util.ArrayList;

public class BeaconArrayAdapter extends ArrayAdapter<TrackedBeacon> {
    private static final String TAG = "BeaconArrayAdapter";
    private ArrayList<TrackedBeacon> mBeacons;
    private Activity mActivity;
    private int mRowResourceId;

    public BeaconArrayAdapter(Activity a, int rowResourceId, ArrayList<TrackedBeacon> beacons) {
        super(a, rowResourceId, beacons);
        mBeacons = beacons;
        mActivity = a;
        mRowResourceId = rowResourceId;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Log.d(TAG, "GetView called");
        if (view == null) {
            LayoutInflater vi =
                    (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(mRowResourceId, null);
        }
        if (position % 2 == 0) {
            view.setBackgroundColor(Color.WHITE);
        }
        else {
            view.setBackgroundColor(Color.parseColor("#aaccee"));
        }
        TextView line1 = view.findViewById(R.id.line1);
        TextView line2 = view.findViewById(R.id.line2);
        TextView line3 = view.findViewById(R.id.line3);
        TextView line4 = view.findViewById(R.id.line4);
        ImageView image = view.findViewById(R.id.icon);

        TrackedBeacon trackedBeacon = mBeacons.get(position);
        Beacon beacon = trackedBeacon.getBeacon();
        org.altbeacon.ningo.Beacon ningoBeacon = NingoDataFetcher.getInstance(mActivity)
                .getNingoBeaconForBeacon(trackedBeacon.getBeacon());

        String parserId = trackedBeacon.getBeacon().getParserIdentifier();
        if (parserId.equalsIgnoreCase("ibeacon")) {
            line1.setText(beacon.getId1().toString());
            line2.setText("Major: "+beacon.getId2().toString()+"  Minor: "+beacon.getId3().toString()+" (iBeacon)");
            image.setImageResource(R.mipmap.ibeacon);
        }
        else if (parserId.equalsIgnoreCase("altbeacon")) {
            line1.setText(beacon.getId1().toString());
            line2.setText("Major: "+beacon.getId2().toString()+"  Minor: "+beacon.getId3().toString()+"  Data: "+beacon.getDataFields().get(0)+" (AltBeacon)");
            image.setImageResource(R.mipmap.altbeacon);
        }
        else if (parserId.equalsIgnoreCase("eddystone-eid")) {
            line1.setText(beacon.getId1().toString().replace("0x",""));
            line2.setText("(Eddystone-EID - Unresolved)");
            image.setImageResource(R.mipmap.eddystone);
        }
        else if (parserId.equalsIgnoreCase("eddystone-url")) {
            String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
            line1.setText(url);
            line2.setText("(Eddystone-URL)");
            image.setImageResource(R.mipmap.eddystone);
        }
        else if (parserId.equalsIgnoreCase("eddystone-uid")) {
            line1.setText(beacon.getId1().toString().replace("0x",""));
            line2.setText("Instance ID: "+beacon.getId2().toString().replace("0x","")+" (Eddystone-UID)");
            image.setImageResource(R.mipmap.eddystone);
        }
        else {
            line1.setText("Unknown beacon type: "+beacon.getBeaconTypeCode());
            line2.setText("Identifiers: ???");
            image.setImageResource(R.mipmap.ic_launcher);
        }
        String ningoIndicator = "";
        if (ningoBeacon != null) {
            ningoIndicator = "(d)";
        }
        line3.setText("MAC address: "+beacon.getBluetoothAddress()+" Packets: "+trackedBeacon.getTotalPacketsDetected());
        line4.setText(String.format("Distance: %1.1fm RSSI: %d PPS: %1.1f %s", beacon.getDistance(), beacon.getRssi(), trackedBeacon.getPacketsPerSec(), ningoIndicator));

        return view;
    }
}


/*

<!--

iBeacon
2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6
Major 1  Minor 2 (iBeacon)
Distance: 20.0m RSSI: -125.0 PPS: 25

AltBeacon:
2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6
Major 1  Minor 2  Data 255 (AltBeacon)
Distance: 20.0m RSSI: -125.0 PPS: 25

EddystoneUID
2F234454CF6D4A0FADF2 (Eddystone-UID)
Instance ID F4911BA9FFA6
Distance: 20.0m RSSI: -125.0 PPS: 25

EddystoneURL
https://www.google.com/xyz
(Eddystone-URL)
Distance: 20.0m RSSI: -125.0 PPS: 25

EddystoneEID

(Eddystone-URL)
Distance: 20.0m RSSI: -125.0 PPS: 25

-->
 */