package com.davidgyoungtech.beaconscanner;

/**
 * Created by dyoung on 3/2/18.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class TransmitterArrayAdapter extends ArrayAdapter<BeaconTransmitter> {
    private static final String TAG = "TransmitterArrayAdapter";
    private List<BeaconTransmitter> mTransmitters;
    private TransmitterListActivity mActivity;
    private int mRowResourceId;

    public TransmitterArrayAdapter(TransmitterListActivity a, int rowResourceId, List<BeaconTransmitter> transmitters) {
        super(a, rowResourceId, transmitters);
        mTransmitters = transmitters;
        mActivity = a;
        mRowResourceId = rowResourceId;
    }

    private void updateEnabledIcon(View view, boolean enabled, boolean transmitting) {
        Button button = view.findViewById(R.id.transmitterOnButton);
        if (enabled && !transmitting) {
            button.setBackgroundColor(Color.YELLOW);
            button.setText("fail");
        }
        else if (enabled && transmitting) {
            button.setBackgroundColor(Color.parseColor("#888800"));
            button.setText("on");
        }
        else {
            button.setBackgroundColor(Color.RED);
            button.setText("off");
        }

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final BeaconTransmitter transmitter = mTransmitters.get(position);

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
        View iconArea = view.findViewById(R.id.transmitterIconArea);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (transmitter.isEnabled()) {
                    transmitter.setEnabled(false);
                    mActivity.getTransmitterManager().stopTransmitter(mActivity, transmitter);
                    mActivity.getTransmitterManager().ensureAllOn(mActivity);
                }
                else {
                    mActivity.getTransmitterManager().startTransmitter(mActivity, transmitter);
                }
                updateEnabledIcon(view, transmitter.isEnabled(), transmitter.isTransmitting());

            }
        };
        iconArea.setOnClickListener(listener);
        Button button = view.findViewById(R.id.transmitterOnButton);
        button.setOnClickListener(listener);
        ImageView image = view.findViewById(R.id.icon);


        line1.setText(transmitter.getName());
        line2.setText(transmitter.getId1());


        if (transmitter.getFormat().equalsIgnoreCase("ibeacon")) {
            line3.setText("Major: "+transmitter.getId2().toString()+"  Minor: "+transmitter.getId3().toString()+" (iBeacon)");
            image.setImageResource(R.mipmap.ibeacon);
        }
        else if (transmitter.getFormat().equalsIgnoreCase("altbeacon")) {
            line3.setText("Major: "+transmitter.getId2().toString()+"  Minor: "+transmitter.getId3().toString()+"  Data: "+transmitter.getData1()+" (AltBeacon)");
            image.setImageResource(R.mipmap.altbeacon);
        }
        else if (transmitter.getFormat().equalsIgnoreCase("eddystone-eid")) {
            line3.setText("(Eddystone-EID)");
            image.setImageResource(R.mipmap.eddystone);
        }
        else if (transmitter.getFormat().equalsIgnoreCase("eddystone-url")) {
            line3.setText("(Eddystone-URL)");
            image.setImageResource(R.mipmap.eddystone);
        }
        else if (transmitter.getFormat().equalsIgnoreCase("eddystone-uid")) {
            line3.setText("Instance ID: "+transmitter.getId2().toString()+" (Eddystone-UID)");
            image.setImageResource(R.mipmap.eddystone);
        }
        else {
            line2.setText("Unknown beacon type: "+transmitter.getFormat());
            line3.setText("");
            image.setImageResource(R.mipmap.ic_launcher);
        }
        updateEnabledIcon(view, transmitter.isEnabled(), transmitter.isTransmitting());


        return view;
    }
}


