package com.davidgyoungtech.beaconscanner;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by dyoung on 3/16/18.
 */

public class BluetoothStateBroadcastReceiver extends BroadcastReceiver {
    TransmitterManager mTransmitterManager;
    public BluetoothStateBroadcastReceiver(TransmitterManager transmitterManager) {
        mTransmitterManager = transmitterManager;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            if (state == BluetoothAdapter.STATE_OFF) {
                mTransmitterManager.markAllOff(context, false);
                Intent intent2 = new Intent("BluetoothOff");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);

            }
            if (state == BluetoothAdapter.STATE_ON) {
                mTransmitterManager.ensureAllOn(context);
            }
        }
    }
}

