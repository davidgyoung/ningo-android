package com.davidgyoungtech.beaconscanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dyoung on 3/9/18.
 */

public class TransmitterManager {
    private static final String TAG = TransmitterManager.class.getSimpleName();
    private boolean mStarted = false;
    List<BeaconTransmitter> mTransmitters;
    HashMap<String,org.altbeacon.beacon.BeaconTransmitter> mPhysicalTransmittersMap = new HashMap<String,org.altbeacon.beacon.BeaconTransmitter>();
    List<BeaconTransmitter> mTransmittersStarted = new ArrayList<>();

    public void start(Context context) {
        if (!mStarted) {
            init(context);
            mStarted = true;
        }
    }

    public void refresh(Context context) {
        Log.d(TAG, "refresh");
        // We are marking all off here in case we have edited a transmitter.  This will stop the
        // existing one with the current settings so we can start a new one.  We do not save
        // the sate because this might blow over the newly changed transmitter before we load it
        markAllOff(context, false);
        mTransmitters = BeaconTransmitter.loadAll(context);
        // We mark all off again a second time, but this time we do save state, so the UI knows they
        // are all off.  This is new new list of transmitters so it is safe to save
        markAllOff(context, true);
        ensureAllOn(context);
    }

    public List<BeaconTransmitter> getTransmitters() {
        return mTransmitters;
    }
    private void init(Context context) {
        mTransmitters = BeaconTransmitter.loadAll(context);
        for (BeaconTransmitter transmitter : mTransmitters) {
            transmitter.setTransmitting(false);
        }
        ensureAllOn(context);
    }

    public void ensureAllOn(Context context) {
        Log.d(TAG, "Ensure all on");
        boolean bluetoothOn = BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled();
        if (!bluetoothOn) {
            return;
        }
        // We sort by the transmitter with the oldest nonzero start time, so if we stop and restart
        // we repopulate the old slots in sthe same order as before
        List<BeaconTransmitter> sortedTranmsmitters = new ArrayList<>();
        for (BeaconTransmitter transmitter : mTransmitters) {
            sortedTranmsmitters.add(transmitter);
        }
        Collections.copy(sortedTranmsmitters, mTransmitters);
        Collections.sort(sortedTranmsmitters, new Comparator<BeaconTransmitter>() {
            @Override
            public int compare(BeaconTransmitter t1, BeaconTransmitter t2) {
                if (t1.getLastTransmitStartTime() == t2.getLastTransmitStartTime()) {
                    return 0;
                }
                if (t2.getLastTransmitStartTime() == 0) {
                    return -1;
                }
                if (t1.getLastTransmitStartTime() == 0) {
                    return 1;
                }
                if (t1.getLastTransmitStartTime() > t2.getLastTransmitStartTime()) {
                    return 1;
                }
                return -1;
            }
        });
        Log.d(TAG, "transmitter count "+mTransmitters.size()+", sorted count: "+sortedTranmsmitters.size());

        for (BeaconTransmitter transmitter : sortedTranmsmitters) {
            if (transmitter.isEnabled() && !transmitter.getTransmitting()) {
                Log.d(TAG, "Starting transmitter with last start time: "+transmitter.getLastTransmitStartTime());
                startTransmitter(context, transmitter);
            }
            else {
                Log.d(TAG, "not starging because already transmitting is "+transmitter.getTransmitting()+" and enabled is "+transmitter.isEnabled());
            }
        }
        BeaconTransmitter.saveAll(context, mTransmitters);
    }

    public void markAllOff(Context context, boolean saveState) {
        Log.d(TAG, "markAllOff");
        for (BeaconTransmitter transmitter: mTransmitters) {
            Log.d(TAG, "Stopping: "+transmitter);
            stopTransmitter(context, transmitter, saveState);
        }
    }

    public void startTransmitter(final Context context, final BeaconTransmitter transmitter) {
        String layout = null;
        if (transmitter.getFormat().equalsIgnoreCase("ibeacon")) {
            layout = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
        }
        else if (transmitter.getFormat().equalsIgnoreCase("altbeacon")) {
            layout = BeaconParser.ALTBEACON_LAYOUT;
        }
        else if (transmitter.getFormat().equalsIgnoreCase("eddystone-uid")) {
            layout = "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19,d:20-21";
        }
        else if (transmitter.getFormat().equalsIgnoreCase("eddystone-eid")) {
            layout = "s:0-1=feaa,m:2-2=30,p:3-3:-41,i:4-11";
        }
        else if (transmitter.getFormat().equalsIgnoreCase("eddystone-url")) {
            layout = BeaconParser.EDDYSTONE_URL_LAYOUT;
        }
        else if (transmitter.getFormat().equalsIgnoreCase("eddystone-tlm")) {
            layout = BeaconParser.EDDYSTONE_TLM_LAYOUT;
        }
        if (layout != null) {
            BeaconParser parser = new BeaconParser().setBeaconLayout(layout);
            org.altbeacon.beacon.BeaconTransmitter physicalTransmitter = new org.altbeacon.beacon.BeaconTransmitter(context, parser);
            mTransmittersStarted.add(transmitter);
            mPhysicalTransmittersMap.put(transmitter.getUuid(), physicalTransmitter);
            physicalTransmitter.setBeaconParser(parser);
            if (transmitter.getTransmitterPower().equalsIgnoreCase("LOW")) {
                physicalTransmitter.setAdvertiseTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW);
            }
            else if (transmitter.getTransmitterPower().equalsIgnoreCase("MEDIUM")) {
                physicalTransmitter.setAdvertiseTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
            }
            else {
                physicalTransmitter.setAdvertiseTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
            }
            if (transmitter.getAdvertisingRate() == 1) {
                physicalTransmitter.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
            }
            else if (transmitter.getAdvertisingRate() == 3) {
                physicalTransmitter.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
            }
            else {
                physicalTransmitter.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
            }
            Beacon.Builder builder = new Beacon.Builder();
            builder.setTxPower(transmitter.getMeasuredPower());
            if (transmitter.getFormat().equalsIgnoreCase("ibeacon")) {
                builder.setId1(transmitter.getId1());
                builder.setId2(transmitter.getId2());
                builder.setId3(transmitter.getId3());
                builder.setManufacturer(0x004c); // always set to apple so it works with ibeacon
            }
            else if (transmitter.getFormat().equalsIgnoreCase("altbeacon")) {
                builder.setId1(transmitter.getId1());
                builder.setId2(transmitter.getId2());
                builder.setId3(transmitter.getId3());
                builder.setDataFields(
                        Arrays.asList(
                                new Long[] { Long.parseLong(transmitter.getData1()) }
                                ));
                builder.setManufacturer(0x0118); // radius networks

            }
            else if (transmitter.getFormat().equalsIgnoreCase("eddystone-uid")) {
                Log.d(TAG, "identifier 1: "+transmitter.getId1());
                builder.setId1("0x"+transmitter.getId1());
                Log.d(TAG, "identifier 2: "+transmitter.getId2());
                builder.setId2("0x"+transmitter.getId2());
                try {
                    builder.setDataFields(
                            Arrays.asList(
                                    new Long[] { Long.parseLong(transmitter.getData1()) }
                            ));
                }
                catch (NumberFormatException e) {
                    Log.e(TAG, "cannot parse data field value for eddyston-uid transmitter: "+transmitter.getData1());
                }
            }
            else if (transmitter.getFormat().equalsIgnoreCase("eddystone-eid")) {
                builder.setId1("0x"+transmitter.getId1());
            }
            else if (transmitter.getFormat().equalsIgnoreCase("eddystone-url")) {
                try {
                    byte[] urlBytes = UrlBeaconUrlCompressor.compress(transmitter.getId1());
                    builder.setIdentifiers(
                            Arrays.asList(
                                    new Identifier[] {
                                            Identifier.fromBytes(urlBytes, 0, urlBytes.length, false)
                                    }
                            )
                    );
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            else if (transmitter.getFormat().equalsIgnoreCase("eddystone-tlm")) {
                // TODO: try to make this work
            }

            transmitter.setEnabled(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                boolean bluetoothOn = BluetoothAdapter.getDefaultAdapter().isEnabled();
                if (bluetoothOn) {
                    Beacon beacon = builder.build();
                    Log.d(TAG, "Starting transmission for "+beacon);
                    physicalTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
                        @Override
                        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                            super.onStartSuccess(settingsInEffect);
                            transmitter.setLastTransmitStartTime(System.currentTimeMillis());
                            transmitter.setTransmitting(true);
                            Log.d(TAG, "transmit start success");
                            Intent intent = new Intent("AdvertisingStarted");
                            intent.putExtra("transmitter", transmitter);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            BeaconTransmitter.saveAll(context, mTransmitters);
                        }
                        @Override
                        public void onStartFailure(int errorCode) {
                            transmitter.setTransmitting(false);
                            transmitter.setLastTransmitStartTime(0);
                            Intent intent = new Intent("AdvertisingFailed");
                            intent.putExtra("transmitter", transmitter);
                            Log.d(TAG, "transmit start failed "+errorCode);
                            if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {
                                intent.putExtra("reason", "Android operating system internal error starting advertising.");
                            }
                            else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                                intent.putExtra("reason", "Transmission already started.");
                            }
                            else if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
                                intent.putExtra("reason", "Beacon data are too large to fit into an advertisement.");
                            }
                            else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
                                intent.putExtra("reason", "This device does not support advertising.");
                            }
                            else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
                                if (mTransmittersStarted.size() == 0){
                                    intent.putExtra("reason", "No more advertising slots available on this device.  It may be that another app is running that is using all the bluetooth resources on this device.  Please try to stop any other bluetooth apps and try again.  If that does not help, there may be a problem with bluetooth on this device model.  Please report this to the manufacturer.");
                                }
                                else if (mTransmittersStarted.size() == 1){
                                    intent.putExtra("reason", "No more advertising slots available on this device.  While many Android device models have chips that support multiple bluetooth advertisements, it looks like this one does not.");
                                }
                                else {
                                    intent.putExtra("reason", "No more advertising slots available on this device.  This app has already started "+mTransmittersStarted.size()+" beacon advertisements.  You may have reached the limit of this device's bluetoth chip.");
                                }
                            }
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            BeaconTransmitter.saveAll(context, mTransmitters);
                        }
                    });
                }
                else {
                    Log.d(TAG, "Cannot start transmission.  BLE is turned off.");

                    // This is thrown if bluetooth is off
                    transmitter.setTransmitting(false);
                    Intent intent = new Intent("AdvertisingFailed");
                    intent.putExtra("transmitter", transmitter);
                    intent.putExtra("reason", "Cannot start beacon transmission with bluetooth off.");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            }
            else {
                Log.d(TAG, "No android 5.0 cannot transmit");
                transmitter.setTransmitting(false);
                transmitter.setLastTransmitStartTime(0l);
                Intent intent = new Intent("AdvertisingFailed");
                intent.putExtra("transmitter", transmitter);
                intent.putExtra("reason", "Android 5.0+ is needed to transmit.");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        }
        else {
            throw new RuntimeException("unknown layout: "+transmitter.getFormat());
        }
        Log.d(TAG, "transmitter "+transmitter.getUuid()+" / "+transmitter.hashCode()+" should be enabled: "+transmitter.isEnabled());
        for (BeaconTransmitter tx : mTransmitters) {
            Log.d(TAG, tx.getUuid()+" / "+tx.hashCode()+" is our transmitter ? "+transmitter.getUuid().equalsIgnoreCase(tx.getUuid()) +" enabled: "+tx.isEnabled()+" transmitting: "+tx.getTransmitting() );
        }
        Log.d(TAG, "Calling saveAll");
        BeaconTransmitter.saveAll(context, mTransmitters);
    }

    public void stopTransmitter(final Context context, final BeaconTransmitter transmitter, boolean saveState) {
        org.altbeacon.beacon.BeaconTransmitter physicalTransmitter = mPhysicalTransmittersMap.get(transmitter.getUuid());
        if (physicalTransmitter == null) {
            Log.d(TAG, "No physical transmitter.");

        }
        else {
            physicalTransmitter.stopAdvertising();
        }
        mPhysicalTransmittersMap.remove(transmitter);
        List<BeaconTransmitter> newTransmittersStarted = new ArrayList<>();
        for (BeaconTransmitter tx: mTransmittersStarted) {
            if (!tx.getUuid().equalsIgnoreCase(transmitter.getUuid())) {
                newTransmittersStarted.add(tx);
            }
        }
        mTransmittersStarted = newTransmittersStarted;
        transmitter.setTransmitting(false);
        if (saveState) {
            BeaconTransmitter.saveAll(context, mTransmitters);
        }
    }

}

