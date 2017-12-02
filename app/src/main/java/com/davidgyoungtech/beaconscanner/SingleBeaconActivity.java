package com.davidgyoungtech.beaconscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManagerV3;
import org.altbeacon.beacon.Region;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by dyoung on 11/17/17.
 */

public class SingleBeaconActivity extends Activity {

    private static final String TAG = SingleBeaconActivity.class.getSimpleName();
    private BeaconTracker mBeaconTracker;
    private Beacon mChosenBeacon;
    private TrackedBeacon mTrackedBeacon;
    private TextView mTextView;
    private boolean mCalibrating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_beacon);
        BeaconManagerV3.getInstance(this).setDebug(true);
        mBeaconTracker = BeaconTracker.getInstance(this);
        mTextView = findViewById(R.id.text);
        Intent intent = this.getIntent();
        if (intent != null) {
            mChosenBeacon = (Beacon) intent.getSerializableExtra("beacon");
        }
        updateView();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mBeaconTracker.start();

        IntentFilter filter = new IntentFilter(BeaconTracker.UPDATE_NOTIFICATION_NAME);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBeaconTracker.stop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalReceiver);
    }

    BroadcastReceiver mLocalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received debug message");
            updateView();
        }
    };

    private void updateView() {
        StringBuilder sb = new StringBuilder();

        sb.append(mChosenBeacon.getId1().toString());
        sb.append("\n");
        sb.append("major: "+mChosenBeacon.getId2().toString());
        sb.append("\n");
        sb.append("minor: "+mChosenBeacon.getId3().toString());
        sb.append("\n");
        TrackedBeacon trackedBeacon = mBeaconTracker.getTrackedBeacon(mChosenBeacon);
        if (trackedBeacon != null) {
            mTrackedBeacon = trackedBeacon;
        }
        if (trackedBeacon != null) {
            String rssiStr = String.format("%.1f",mTrackedBeacon.getBeacon().getRunningAverageRssi());
            String distanceStr = String.format("%.1f",mTrackedBeacon.getBeacon().getDistance());
            String pps = String.format("%.1f",mTrackedBeacon.getPacketsPerSec());

            sb.append("distance: "+distanceStr);
            sb.append("\n");
            sb.append("rssi: "+mTrackedBeacon.getBeacon().getRssi());
            sb.append("\n");
            sb.append("packets: "+mTrackedBeacon.getTotalPacketsDetected());
            sb.append("\n");
            sb.append("packets/sec: "+pps);
            sb.append("\n");
            sb.append("stabilized: "+mTrackedBeacon.isMeasurementsStabilized());
            sb.append("\n");
            sb.append("running average rssi: "+rssiStr);
            sb.append("\n");
            if (mTrackedBeacon.isMeasurementsStabilized() || trackedBeacon == null) {
                if (mCalibrating) {
                    mCalibrating = false;
                    String title = "Calibration complete";
                    if (mTrackedBeacon.getTotalPacketsDetected() < 10) {
                        title = "Failed -- too few detections";
                    }
                    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    alertDialog.setTitle(title);
                    alertDialog.setMessage("RSSI is "+rssiStr+"\nPacket count: "+mTrackedBeacon.getTotalPacketsDetected());
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
            if (mCalibrating && !mTrackedBeacon.isMeasurementsStabilized()) {
                mTextView.setBackgroundColor(Color.YELLOW);
            }
            else {
                if (trackedBeacon != null) {
                    mTextView.setBackgroundColor(Color.WHITE);
                }
                else {
                    mTextView.setBackgroundColor(Color.RED);
                    sb.append("NOT DETECTED");
                }
            }
        }

        mTextView.setText(sb.toString());
    }
    public void showData(View view) {
        Intent intent = new Intent(this, NingoShowBeaconActivity.class);
        intent.putExtra("ningo_beacon_identifier", mChosenBeacon.getId1().toString()+"_"+mChosenBeacon.getId2().toString()+"_"+mChosenBeacon.getId3().toString()+"_ibeacon");
        startActivity(intent);

    }

    public void startCalibration(View view) {
        mCalibrating = true;
        mTextView.setBackgroundColor(Color.GRAY);
        mBeaconTracker.stop();
        mBeaconTracker.start();
    }
}
