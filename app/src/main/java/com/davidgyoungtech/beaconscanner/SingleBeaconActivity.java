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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManagerV3;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.EddystoneTelemetryAccessor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by dyoung on 11/17/17.
 */

public class SingleBeaconActivity extends Activity {

    private static final String TAG = SingleBeaconActivity.class.getSimpleName();
    private BeaconTracker mBeaconTracker;
    private Beacon mChosenBeacon;
    private Button mNingoDataButton;
    private Button mCalibrationButton;
    private ProgressBar mCalibrationSpinner;
    private TrackedBeacon mTrackedBeacon;
    private TextView mTextView;
    private boolean mCalibrating = false;
    private TextView mEddystoneTelemetryTextView;
    private TextView mReceptionStatisticsHeaderTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_beacon);
        BeaconManagerV3.getInstance(this).setDebug(true);
        mBeaconTracker = BeaconTracker.getInstance(this);
        mCalibrationSpinner = findViewById(R.id.progressBar);
        mCalibrationSpinner.setVisibility(View.GONE);
        mNingoDataButton = findViewById(R.id.ningoDataButton);
        mReceptionStatisticsHeaderTextView = findViewById(R.id.receptionStatsHeader);
        mCalibrationButton = findViewById(R.id.calibrationButton);
        mTextView = findViewById(R.id.receptionStats);
        Intent intent = this.getIntent();
        if (intent != null) {
            mChosenBeacon = (Beacon) intent.getSerializableExtra("beacon");
        }
        mEddystoneTelemetryTextView = findViewById(R.id.eddystoneTelemetry);
        TextView eddystoneTelemetryHeader = findViewById(R.id.eddystoneTelemetryHeader);
        if (mChosenBeacon.getParserIdentifier().equalsIgnoreCase("eddystone-uid") ||
                mChosenBeacon.getParserIdentifier().equalsIgnoreCase("eddystone-url") ||
            mChosenBeacon.getParserIdentifier().equalsIgnoreCase("eddystone-eid")) {
            mEddystoneTelemetryTextView.setVisibility(View.VISIBLE);
            eddystoneTelemetryHeader.setVisibility(View.VISIBLE);
            mEddystoneTelemetryTextView.setText("(No Eddystone-TLM frame detected.)");
        }
        else {
            mEddystoneTelemetryTextView.setVisibility(View.GONE);
            eddystoneTelemetryHeader.setVisibility(View.GONE);
        }

        mEddystoneTelemetryTextView = findViewById(R.id.eddystoneTelemetry);
        if (mChosenBeacon.getParserIdentifier().equalsIgnoreCase("eddystone-uid") ||
                mChosenBeacon.getParserIdentifier().equalsIgnoreCase("ibeacon") ||
                mChosenBeacon.getParserIdentifier().equalsIgnoreCase("altbeacon")) {
            mNingoDataButton.setEnabled(true);
            mNingoDataButton.setAlpha((float)1.0);
        }
        else {
            mNingoDataButton.setEnabled(false);
            mNingoDataButton.setAlpha((float)0.5);
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
            NingoDataFetcher.getInstance(SingleBeaconActivity.this).fetchIfStale();
            updateView();
        }
    };

    private void updateView() {


        String type = "Unknown";
        ((ImageView)findViewById(R.id.icon)).setImageResource(R.mipmap.ic_launcher);
        if (mChosenBeacon.getParserIdentifier().equalsIgnoreCase("ibeacon")) {
            type = "iBeacon";
            ((ImageView)findViewById(R.id.icon)).setImageResource(R.mipmap.ibeacon);
        }
        else if (mChosenBeacon.getParserIdentifier().equalsIgnoreCase("altbeacon")) {
            type = "AltBeacon";
            ((ImageView)findViewById(R.id.icon)).setImageResource(R.mipmap.altbeacon);
        }
        else if (mChosenBeacon.getParserIdentifier() != null) {
            type = mChosenBeacon.getParserIdentifier().toUpperCase();
        }
        if (mChosenBeacon.getParserIdentifier().indexOf("eddystone") >= 0) {
            ((ImageView)findViewById(R.id.icon)).setImageResource(R.mipmap.eddystone);
        }

        ((TextView)findViewById(R.id.beaconType)).setText(type);


        ((TextView)findViewById(R.id.beaconFirstIdentifierValue)).setText(mChosenBeacon.getId1().toString());
        //sb.append(mChosenBeacon.getId1().toString());
        //sb.append("\n");
        if (mChosenBeacon.getIdentifiers().size() > 1) {
            ((TextView)findViewById(R.id.beaconSecondIdentifierValue)).setText(mChosenBeacon.getId2().toString());
        }
        else {
            (findViewById(R.id.beaconSecondIdentifier)).setVisibility(View.GONE);
        }
        if (mChosenBeacon.getIdentifiers().size() > 2) {
            ((TextView)findViewById(R.id.beaconThirdIdentifierValue)).setText(mChosenBeacon.getId3().toString());
        }
        else {
            (findViewById(R.id.beaconThirdIdentifier)).setVisibility(View.GONE);
        }
        if (mChosenBeacon.getDataFields().size() > 0) {
            ((TextView)findViewById(R.id.beaconFirstDataValue)).setText(mChosenBeacon.getDataFields().get(0).toString());
        }
        else {
            (findViewById(R.id.beaconFirstData)).setVisibility(View.GONE);
        }
        ((TextView)findViewById(R.id.measuredPoweValue)).setText(""+mChosenBeacon.getTxPower()+" dBm");

        TrackedBeacon trackedBeacon = mBeaconTracker.getTrackedBeacon(mChosenBeacon);
        if (trackedBeacon != null) {
            mTrackedBeacon = trackedBeacon;
        }
        if (trackedBeacon != null) {
            String rssiStr = String.format("%.1f dBm",mTrackedBeacon.getBeacon().getRunningAverageRssi());
            String distanceStr = String.format("%.1f meters",mTrackedBeacon.getBeacon().getDistance());
            String pps = String.format("%.1f",mTrackedBeacon.getPacketsPerSec());
            String detectionRate = "--";
            if (mTrackedBeacon.getTotalRangePeriods() > 0) {
                if (mTrackedBeacon.getTotalRangeSamples() > mTrackedBeacon.getTotalRangePeriods()) {
                    detectionRate = "100";
                }
                else {
                    detectionRate = String.format("%.0f",mTrackedBeacon.getTotalRangeSamples()*100.0/mTrackedBeacon.getTotalRangePeriods()+0.5);
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("distance: "+distanceStr);
            sb.append("\n");
            sb.append("rssi: "+mTrackedBeacon.getBeacon().getRssi()+" dBm");
            sb.append("\n");
            sb.append("average rssi: "+rssiStr);
            sb.append("\n");
            sb.append("packets: "+mTrackedBeacon.getTotalPacketsDetected());
            sb.append("\n");
            sb.append("packets/sec: "+pps);
            sb.append("\n");
            sb.append("detection rate: "+detectionRate+"%");
            sb.append("\n");
            sb.append("stabilized: "+mTrackedBeacon.isMeasurementsStabilized());
            sb.append("\n");
            org.altbeacon.ningo.Beacon ningoBeacon = NingoDataFetcher.getInstance(this)
                    .getNingoBeaconForBeacon(trackedBeacon.getBeacon());
            if (ningoBeacon != null && ningoBeacon.getMetadata() != null && ningoBeacon.getMetadata().getLocation() != null) {
                sb.append(String.format("Ningo latitude: %1.2f\n",ningoBeacon.getMetadata().getLocation().getLatitude()));
                sb.append(String.format("Ningo longitude: %1.2f\n",ningoBeacon.getMetadata().getLocation().getLongitude()));
            }
            else if (ningoBeacon != null && ningoBeacon.getWikiBeaconLatitude() != null) {
                sb.append(String.format("Wikibeacon latitude: %1.2f\n",ningoBeacon.getWikiBeaconLatitude()));
                sb.append(String.format("Wikibeacon longitude: %1.2f\n",ningoBeacon.getWikiBeaconLongitude()));
            }

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
            if (mCalibrating) {
            }
            else {
                mCalibrationSpinner.setVisibility(View.GONE);
                mCalibrationButton.setText("Start Calibration");
                mCalibrationButton.setEnabled(true);
                mCalibrationButton.setAlpha((float)1.0);
            }
            mTextView.setText(sb.toString());
            mTextView.setAlpha((float)1.0);
            mReceptionStatisticsHeaderTextView.setText("Reception Statistics");
        }
        else {
            mReceptionStatisticsHeaderTextView.setText("Reception Statistics (No longer detected)");
            mTextView.setAlpha((float)0.5);
        }
        if (mChosenBeacon.getExtraDataFields() != null && mChosenBeacon.getExtraDataFields().size() >= 4) {
            List<Long> extraDataFields = mChosenBeacon.getExtraDataFields();
            long version = (extraDataFields.get(0).intValue()) & 0xff;
            long battery = extraDataFields.get(1).intValue();
            long unsignedTemp = extraDataFields.get(2).intValue() >> 8;
            long unsignedTempLowByte = extraDataFields.get(2).intValue() & 0xff;
            double temperature = (unsignedTemp > 128 ? unsignedTemp - 256 : unsignedTemp) + unsignedTempLowByte/256.0;
            long pduCount =  extraDataFields.get(3);
            long uptime = extraDataFields.get(4).longValue();

            if (version == 0l) {
                String batteryString = "N/A";
                if (battery != 0) {
                    batteryString = String.format("%.1f", battery/1000.0);
                }
                mEddystoneTelemetryTextView.setText(String.format("Temperature: %.1f deg C\nBattery: %sV\nAdvertisements transmitted: %d\nUptime: %.1f secs", temperature, batteryString, pduCount, uptime/10.0));
            }
            else {
                mEddystoneTelemetryTextView.setText(String.format("Unknown Eddystone Telemetry Version: %d", version));
            }
        }

    }
    public void showData(View view) {
        Intent intent = new Intent(this, NingoShowBeaconActivity.class);
        if (mChosenBeacon.getParserIdentifier().equalsIgnoreCase("ibeacon")) {
            intent.putExtra("ningo_beacon_identifier", mChosenBeacon.getId1().toString()+"_"+mChosenBeacon.getId2().toString()+"_"+mChosenBeacon.getId3().toString()+"_ibeacon");
        }
        if (mChosenBeacon.getParserIdentifier().equalsIgnoreCase("altbeacon")) {
            intent.putExtra("ningo_beacon_identifier", mChosenBeacon.getId1().toString()+"_"+mChosenBeacon.getId2().toString()+"_"+mChosenBeacon.getId3().toString()+"_ibeacon");
        }
        if (mChosenBeacon.getParserIdentifier().equalsIgnoreCase("eddystone-uid")) {
            intent.putExtra("ningo_beacon_identifier", mChosenBeacon.getId1().toString().replaceAll("0x", "")+"_"+mChosenBeacon.getId2().toString().replaceAll("0x", "")+"_eddystone-uid");
        }
        startActivity(intent);

    }

    public void startCalibration(View view) {
        mCalibrationSpinner.setVisibility(View.VISIBLE);
        mCalibrationButton.setText("Calibrating Beacon");
        mCalibrationButton.setEnabled(false);
        mCalibrationButton.setAlpha((float)0.5);
        mCalibrating = true;
        mBeaconTracker.stop();
        mBeaconTracker.start();
    }
}
