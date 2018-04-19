package com.davidgyoungtech.beaconscanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

public class BluetoothHelper {
    private Context mContext;

    public BluetoothHelper(Context context) {
        mContext = context;
    }

    public boolean ensureBluetoothOn() {
        if (!isBleAvailable()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE. You cannot use this app.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //System.exit(0);
                }

            });
            builder.show();
            return false;
        } else {
            if (!isBluetoothOn()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Bluetooth is Off");
                builder.setMessage("This app cannot run without bluetooth.  Turn it on?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        turnOnBluetooth();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
                builder.show();
                return false;
            }
        }
        return true;
    }
    /**
     * Returns false of BLE is not available on this device
     * @return
     */
    public boolean isBleAvailable() {
        if(Build.VERSION.SDK_INT < 18) {
            return false;
        } else if(!this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            return false;
        }
        return true;
    }
    /**
     * Returns false if bluetooth is turned of on this device
     * @return
     */
    @SuppressLint("MissingPermission")
    public boolean isBluetoothOn() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }

    /**
     * Turns bluetooth on
     */
    @SuppressLint("MissingPermission")
    public void turnOnBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.enable();
    }
}