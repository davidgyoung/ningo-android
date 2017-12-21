package com.davidgyoungtech.beaconscanner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconManagerV3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BeaconListActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = BeaconListActivity.class.getSimpleName();
    ListView mListView ;
    TextView mTextView;
    PermissionHelper mPermissionHelper;
    private boolean mSortByDistance = false;
    private BeaconTracker mBeaconTracker;
    private List<TrackedBeacon> mSortedBeacons;
    private boolean mLaunchedDetailActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_list);
        BeaconManagerV3.getInstance(this).setDebug(true);

        mListView = findViewById(R.id.list);
        mTextView = findViewById(R.id.debugHeader);
        mPermissionHelper = new PermissionHelper(this);
        mPermissionHelper.requestPermissions();
        mBeaconTracker = BeaconTracker.getInstance(this);
        mListView.setOnItemClickListener(this);
        mTextView.setText("Looking for beacons...");
    }
    @Override
    protected void onResume() {
        super.onResume();
        mBeaconTracker.start();
        IntentFilter filter = new IntentFilter(BeaconTracker.UPDATE_NOTIFICATION_NAME);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalReceiver, filter);
        mLaunchedDetailActivity = false;
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (!mLaunchedDetailActivity) {
            mBeaconTracker.stop();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalReceiver);
    }

    BroadcastReceiver mLocalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received debug message");
            ArrayList<TrackedBeacon> sortableList = new ArrayList<TrackedBeacon>(BeaconTracker.getInstance(BeaconListActivity.this).getTrackedBeacons());
            Collections.sort(sortableList, mBeaconComparator);
            updateListView(sortableList);
            Log.d(TAG, "detected beacon count "+sortableList.size());
            NingoDataFetcher.getInstance(BeaconListActivity.this).fetchIfStale();
        }
    };

    private Comparator<TrackedBeacon> mBeaconComparator = new Comparator<TrackedBeacon>() {
        @Override
        public int compare(TrackedBeacon o1, TrackedBeacon o2) {
            if (mSortByDistance == false) {
                return o1.toString().compareTo(o2.toString());
            }
            else {
                return new Double(o1.getBeacon().getRunningAverageRssi()).compareTo(new Double(o2.getBeacon().getRunningAverageRssi()));
            }

        }
    };

    private void updateListView(ArrayList<TrackedBeacon> list) {
        int index = mListView.getFirstVisiblePosition();
        View v = mListView.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();

        if (list.size() == 0) {
            mTextView.setText("No beacons detected.");
        }
        else {
            mTextView.setText(""+list.size()+" beacons detected:");
        }
        BeaconArrayAdapter adapter = new BeaconArrayAdapter(this,
                R.layout.beacon_row, list);

        mSortedBeacons = list;

        // Assign adapter to ListView
        mListView.setAdapter(adapter);
        mListView.setSelectionFromTop(index, top);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        mPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mSortedBeacons != null) {
            List<TrackedBeacon> copyOfTrackedBeacons = new ArrayList<TrackedBeacon>(mSortedBeacons);
            if (copyOfTrackedBeacons.size() > position) {
                TrackedBeacon tappedBeacon = copyOfTrackedBeacons.get(position);
                Intent intent = new Intent(this, SingleBeaconActivity.class);
                intent.putExtra("beacon", (Serializable) tappedBeacon.getBeacon());
                mLaunchedDetailActivity = true;
                startActivity(intent);
            }
        }

    }
}
