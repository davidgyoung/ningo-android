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
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.altbeacon.beacon.BeaconManagerV3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dyoung on 3/3/18.
 */

public class TransmitterListActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = BeaconListActivity.class.getSimpleName();
    ListView mListView ;
    TextView mTextView;
    long mLastDialogTime = 0;
    private TransmitterManager mTransmitterManager;
    private Boolean mBluetoothOn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating new Transmitter List Activity");
        setContentView(R.layout.activity_transmitter_list);

        mTransmitterManager = ((BeaconScannerApplication) this.getApplication()).getTransmitterManager();

        mListView = findViewById(R.id.list);
        mTextView = findViewById(R.id.debugHeader);
        mListView.setOnItemClickListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcastReceiver,
                new IntentFilter("AdvertisingStarted"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcastReceiver,
                new IntentFilter("AdvertisingFailed"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcastReceiver,
                new IntentFilter("BluetoothOff"));

        findViewById(R.id.transmitTab).setBackgroundColor(Color.parseColor("#aaccee"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTransmitterManager.refresh(this);
        updateListView();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void updateListView() {
        List<BeaconTransmitter> transmitters = mTransmitterManager.getTransmitters();
        int index = mListView.getFirstVisiblePosition();
        View v = mListView.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();

        TransmitterArrayAdapter adapter = new TransmitterArrayAdapter(this,
                R.layout.transmitter_row, transmitters);

        // Assign adapter to ListView
        mListView.setAdapter(adapter);
        mListView.setSelectionFromTop(index, top);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "transmitter row clicked");
        List<BeaconTransmitter> transmitters = mTransmitterManager.getTransmitters();
        if (transmitters != null) {
            Log.d(TAG, "transmitters is not null");
            if (transmitters.size() > position) {
                Log.d(TAG, "clicked on "+position+" of "+transmitters.size());
                BeaconTransmitter transmitter = transmitters.get(position);

                Intent intent = new Intent(this, EditTransmitterActivity.class);
                intent.putExtra("transmitterPosition", position);
                startActivity(intent);
            }
        }

    }


    public void transmitTabClicked(View view)    {

    }
    public void scanTabClicked(View view)    {
        Intent intent = new Intent(this, BeaconListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public TransmitterManager getTransmitterManager() {
        return mTransmitterManager;
    }

    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got transmittter result intent "+intent);

            final String reason = intent.getStringExtra("reason");
            if (System.currentTimeMillis() -  mLastDialogTime > 1000) {
                if (reason != null) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mLastDialogTime = System.currentTimeMillis();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(TransmitterListActivity.this);
                            builder.setTitle("Failed to start transmitter");
                            builder.setMessage(reason);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            builder.show();

                        }
                    });
                }
            }

            updateListView();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent = new Intent(this, EditTransmitterActivity.class);
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }
}
