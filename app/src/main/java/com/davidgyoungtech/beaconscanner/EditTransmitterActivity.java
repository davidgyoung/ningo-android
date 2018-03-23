package com.davidgyoungtech.beaconscanner;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

/**
 * Created by dyoung on 3/23/18.
 */

public class EditTransmitterActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transmitter);
        Spinner spinner;
        spinner = (Spinner) findViewById(R.id.beaconType) ;
        java.util.ArrayList<String> strings = new java.util.ArrayList<>();
        strings.add("iBeacon") ;
        strings.add("AltBeacon");
        strings.add("Eddystone-UID");
        strings.add("Eddystone-URL");
        strings.add("Eddystone-EID");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, strings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void saveChanges(View view) {
    }
}
