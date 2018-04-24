package com.davidgyoungtech.beaconscanner;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by dyoung on 3/16/18.
 */

public class BluetoothStateBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = BluetoothStateBroadcastReceiver.class.getSimpleName();
    TransmitterManager mTransmitterManager;
    public BluetoothStateBroadcastReceiver() {
    }

    public BluetoothStateBroadcastReceiver(TransmitterManager transmitterManager) {
        mTransmitterManager = transmitterManager;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            Log.d(TAG, "Bluetooth state changed.");
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (mTransmitterManager == null) {
                Log.d(TAG, "No transmitter manager.  Exiting.");
                return;
            }

            if (state == BluetoothAdapter.STATE_OFF) {
                Log.d(TAG, "Bluetooth off.  Notifying app.");
                mTransmitterManager.markAllOff(context, true);
                Intent intent2 = new Intent("BluetoothOff");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);

            }
            if (state == BluetoothAdapter.STATE_ON) {
                Log.d(TAG, "Bluetooth on.  Not notifying app.");
                mTransmitterManager.ensureAllOn(context);
            }
        }
    }
}

