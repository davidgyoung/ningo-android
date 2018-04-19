package com.davidgyoungtech.beaconscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dyoung on 3/23/18.
 */

public class EditTransmitterActivity extends Activity {
    private static final String TAG = EditTransmitterActivity.class.getSimpleName();
    private Spinner mBeaconTypeSpinner;
    private Spinner mTransmitterPowerSpinner;
    private Spinner mAdvertisingRateSpinner;
    private HashMap<String,BeaconTypeAttributes> mBeaconTypeAttributseMap = new HashMap<String,BeaconTypeAttributes>();
    private TextView[] mIdentifierLabels;
    private EditText[] mIdentifierEditTexts;
    private TextView mDataFieldLabel;
    private EditText mDataFieldEditText;
    private String mLastBeaconTypeValue = "";
    private int mBeaconTransmitterIndex = -1;
    private EditText mNameEditText;
    private EditText mMeasuredPowerEditText;

    public void setEditTransmitterIndex(int index) {
        mBeaconTransmitterIndex = index;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getIntent() != null) {
            mBeaconTransmitterIndex= this.getIntent().getIntExtra("transmitterPosition", -1);
        }
        initializeBeaconTypeLookups();
        setContentView(R.layout.activity_edit_transmitter);
        mNameEditText = findViewById(R.id.name);
        mMeasuredPowerEditText = findViewById(R.id.measured_power);
        mIdentifierLabels = new TextView[] {findViewById(R.id.identifier1_label),
                                            findViewById(R.id.identifier2_label),
                                            findViewById(R.id.identifier3_label)};
        mIdentifierEditTexts = new EditText[] {findViewById(R.id.identifier1),
                findViewById(R.id.identifier2),
                findViewById(R.id.identifier3)};
        mDataFieldLabel = findViewById(R.id.data1_label);
        mDataFieldEditText= findViewById(R.id.data1);
        initializeSpinners();
        load();
        updateViewsForBeaconType();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mBeaconTransmitterIndex >= 0) {
            getMenuInflater().inflate(R.menu.delete, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure?");
        builder.setMessage("Are you sure you want to delete this beacon transmitter?");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<BeaconTransmitter> transmitters = BeaconTransmitter.loadAll(EditTransmitterActivity.this);
                transmitters.remove(mBeaconTransmitterIndex);
                BeaconTransmitter.saveAll(EditTransmitterActivity.this, transmitters);
                EditTransmitterActivity.this.finish();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();

        return super.onOptionsItemSelected(item);
    }

    public void load() {
        List<BeaconTransmitter> transmitters = BeaconTransmitter.loadAll(this);
        BeaconTransmitter transmitter  = null;
        if (mBeaconTransmitterIndex < 0) {
            transmitter = BeaconTransmitter.createTransmitter();
            mNameEditText.setText("Transmitter "+(transmitters.size()+1));
        }
        else {
            transmitter = transmitters.get(mBeaconTransmitterIndex);
            mNameEditText.setText(transmitter.getName());
            Log.d(TAG, "format is "+transmitter.getFormat());
            BeaconTypeAttributes attrs = mBeaconTypeAttributseMap.get(transmitter.getFormat().toLowerCase());
            if (attrs.identifierNames.length > 0) {
                mIdentifierEditTexts[0].setText(transmitter.getId1());
            }
            if (attrs.identifierNames.length > 1) {
                mIdentifierEditTexts[1].setText(transmitter.getId2());
            }
            if (attrs.identifierNames.length > 2) {
                mIdentifierEditTexts[2].setText(transmitter.getId3());
            }
            if (attrs.dataFieldNames.length > 0) {
                mDataFieldEditText.setText(transmitter.getData1());
            }
            mMeasuredPowerEditText.setText(""+transmitter.getMeasuredPower());
            setSelectedBeaconType(transmitter.getFormat());
            setSelectedAdvertisingRate(transmitter.getAdvertisingRate());
            setSelectedTransmitterPower(transmitter.getTransmitterPower());
        }
    }
    public void saveChanges(View view) {
        if (validate()) {
            List<BeaconTransmitter> transmitters = BeaconTransmitter.loadAll(this);
            BeaconTransmitter transmitter  = null;
            if (mBeaconTransmitterIndex < 0) {
                transmitter = BeaconTransmitter.createTransmitter();
            }
            else {
                transmitter = transmitters.get(mBeaconTransmitterIndex);
            }
            transmitter.setFormat(getSelectedBeaconType());
            transmitter.setName(mNameEditText.getText().toString());

            BeaconTypeAttributes attrs = mBeaconTypeAttributseMap.get(getSelectedBeaconType().toLowerCase());

            if (attrs.identifierNames.length > 0) {
                transmitter.setId1(mIdentifierEditTexts[0].getText().toString());
            }
            if (attrs.identifierNames.length > 1) {
                transmitter.setId2(mIdentifierEditTexts[1].getText().toString());
            }
            if (attrs.identifierNames.length > 2) {
                transmitter.setId3(mIdentifierEditTexts[2].getText().toString());
            }
            if (attrs.dataFieldNames.length > 0) {
                transmitter.setData1(mDataFieldEditText.getText().toString());
            }
            transmitter.setFormat(getSelectedBeaconType());
            transmitter.setMeasuredPower(Integer.parseInt(mMeasuredPowerEditText.getText().toString()));
            transmitter.setTransmitterPower(getSelectedTransmitterPower());
            transmitter.setAdvertisingRate(getSelectedAdvertisingRate());

            if (mBeaconTransmitterIndex < 0) {
                transmitters.add(transmitter);
            }
            BeaconTransmitter.saveAll(this, transmitters);
            this.finish();

        }
    }


    class BeaconTypeAttributes {
        String[] identifierNames;
        String[] identifierExamplePatterns;
        String[] identifierRegexPatterns;
        boolean[] identifierIsHex;
        int[] identifierMaxValues;
        String[] dataFieldNames;
        String[] dataFieldExamplePatterns;
        String[] dataFieldRegexPatterns;
        long[] dataFieldMaxValues;
    }

    private void initializeBeaconTypeLookups() {
        BeaconTypeAttributes iBeaconAttributes = new BeaconTypeAttributes();
        iBeaconAttributes.identifierNames = new String[] {"Proximity UUID", "Major", "Minor"};
        iBeaconAttributes.identifierExamplePatterns = new String[] {"2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6", "1", "1"};
        iBeaconAttributes.identifierRegexPatterns = new String[] {"[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}", "[0-9]+", "[0-9]+"};
        iBeaconAttributes.identifierMaxValues = new int[] {-1, 65535, 65535};
        iBeaconAttributes.identifierIsHex = new boolean[] {true, false, false};
        iBeaconAttributes.dataFieldNames = new String[] {};
        iBeaconAttributes.dataFieldExamplePatterns = new String[] {};
        iBeaconAttributes.dataFieldRegexPatterns = new String[] {};
        iBeaconAttributes.dataFieldMaxValues = new long[] {};
        mBeaconTypeAttributseMap.put("ibeacon", iBeaconAttributes);

        BeaconTypeAttributes altBeaconAttributes = new BeaconTypeAttributes();
        altBeaconAttributes.identifierNames = new String[] {"Id1", "Id2", "Id3"};
        altBeaconAttributes.identifierExamplePatterns = new String[] {"2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6", "1", "1"};
        altBeaconAttributes.identifierRegexPatterns = new String[] {"[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}", "[0-9]+", "[0-9]+"};
        altBeaconAttributes.identifierMaxValues = new int[] {-1, 65535, 65535};
        altBeaconAttributes.identifierIsHex = new boolean[] {true, false, false};
        altBeaconAttributes.dataFieldNames = new String[] {"Data Field"};
        altBeaconAttributes.dataFieldExamplePatterns = new String[] {"0"};
        altBeaconAttributes.dataFieldRegexPatterns = new String[] {"[0-9][0-9]?[0-9]?"};
        altBeaconAttributes.dataFieldMaxValues = new long[] {255};
        mBeaconTypeAttributseMap.put("altbeacon", altBeaconAttributes);

        BeaconTypeAttributes eddystoneUidBeaconAttributes = new BeaconTypeAttributes();
        eddystoneUidBeaconAttributes.identifierNames = new String[] {"Namespace Id", "Instance Id"};
        eddystoneUidBeaconAttributes.identifierExamplePatterns = new String[] {"2F234454CF6D4A0FADF2", "00000000001A"};
        eddystoneUidBeaconAttributes.identifierRegexPatterns = new String[] {"[A-Fa-f0-9]{20}", "[A-Fa-f0-9]{12}"};
        eddystoneUidBeaconAttributes.identifierMaxValues = new int[] {-1, -1, -1};
        eddystoneUidBeaconAttributes.identifierIsHex = new boolean[] {true, true};
        eddystoneUidBeaconAttributes.dataFieldNames = new String[] {"Reserved"};
        eddystoneUidBeaconAttributes.dataFieldExamplePatterns = new String[] {"0"};
        eddystoneUidBeaconAttributes.dataFieldRegexPatterns = new String[] {"[0-9]+"};
        eddystoneUidBeaconAttributes.dataFieldMaxValues = new long[] {65535};
        mBeaconTypeAttributseMap.put("eddystone-uid", eddystoneUidBeaconAttributes);

        BeaconTypeAttributes eddystoneEidBeaconAttributes = new BeaconTypeAttributes();
        eddystoneEidBeaconAttributes.identifierNames = new String[] {"Encrypted Identifier"};
        eddystoneEidBeaconAttributes.identifierExamplePatterns = new String[] {"0102030405060708"};
        eddystoneEidBeaconAttributes.identifierRegexPatterns = new String[] {"[A-Fa-f0-9]{16}"};
        eddystoneEidBeaconAttributes.identifierIsHex = new boolean[] {true};
        eddystoneEidBeaconAttributes.identifierMaxValues = new int[] {-1};
        eddystoneEidBeaconAttributes.dataFieldNames = new String[] {};
        eddystoneEidBeaconAttributes.dataFieldExamplePatterns = new String[] {};
        eddystoneEidBeaconAttributes.dataFieldRegexPatterns = new String[] {};
        eddystoneEidBeaconAttributes.dataFieldMaxValues = new long[] {};
        mBeaconTypeAttributseMap.put("eddystone-eid", eddystoneEidBeaconAttributes);

        BeaconTypeAttributes eddystoneUrlBeaconAttributes = new BeaconTypeAttributes();
        eddystoneUrlBeaconAttributes.identifierNames = new String[] {"Short Url"};
        eddystoneUrlBeaconAttributes.identifierIsHex = new boolean[] {false};
        eddystoneUrlBeaconAttributes.identifierExamplePatterns = new String[] {"http://davidgyoungtech.com"};
        eddystoneUrlBeaconAttributes.identifierRegexPatterns = new String[] {"^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"};
        eddystoneUrlBeaconAttributes.identifierMaxValues = new int[] {-1};
        eddystoneUrlBeaconAttributes.dataFieldNames = new String[] {};
        eddystoneUrlBeaconAttributes.dataFieldExamplePatterns = new String[] {};
        eddystoneUrlBeaconAttributes.dataFieldRegexPatterns = new String[] {};
        eddystoneUrlBeaconAttributes.dataFieldMaxValues = new long[] {};
        mBeaconTypeAttributseMap.put("eddystone-url", eddystoneUrlBeaconAttributes);

    }

    private void initializeSpinners() {
        mBeaconTypeSpinner = (Spinner) findViewById(R.id.beacon_type) ;
        java.util.ArrayList<String> btStrings = new java.util.ArrayList<>();
        btStrings.add("iBeacon") ;
        btStrings.add("AltBeacon");
        btStrings.add("Eddystone-UID");
        btStrings.add("Eddystone-URL");
        btStrings.add("Eddystone-EID");
        ArrayAdapter<String> btAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, btStrings);
        btAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBeaconTypeSpinner.setAdapter(btAdapter);

        mTransmitterPowerSpinner = (Spinner) findViewById(R.id.transmitter_power) ;
        java.util.ArrayList<String> tpStrings = new java.util.ArrayList<>();
        tpStrings.add("High") ;
        tpStrings.add("Medium");
        tpStrings.add("Low");
        tpStrings.add("Ultra Low");
        ArrayAdapter<String> tpAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, tpStrings);
        tpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTransmitterPowerSpinner.setAdapter(tpAdapter);

        mAdvertisingRateSpinner = (Spinner) findViewById(R.id.advertising_rate) ;
        java.util.ArrayList<String> arStrings = new java.util.ArrayList<>();
        arStrings.add("1 Hz");
        arStrings.add("3 Hz");
        arStrings.add("10 Hz") ;
        ArrayAdapter<String> arAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, arStrings);
        arAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAdvertisingRateSpinner.setAdapter(arAdapter);
        mBeaconTypeSpinner.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (!getSelectedBeaconType().equalsIgnoreCase(mLastBeaconTypeValue)) {
                    mLastBeaconTypeValue = getSelectedBeaconType();
                    updateViewsForBeaconType();
                }
            }
        });
    }

    private String getSelectedBeaconType() {
        return ((String)mBeaconTypeSpinner.getSelectedItem()).toLowerCase();
    }

    private void setSelectedBeaconType(String type) {
        int selectedItem = 0;
        for (int i = 0; i < mBeaconTypeSpinner.getAdapter().getCount(); i++) {
            String item = (String) mBeaconTypeSpinner.getAdapter().getItem(i);
            if (item.toLowerCase().startsWith(type.toLowerCase())) {
                selectedItem = i;
                break;
            }
        }
        mBeaconTypeSpinner.setSelection(selectedItem);
    }

    private void setSelectedTransmitterPower(String transmitterPower) {
        int selectedItem = 0;
        for (int i = 0; i < mTransmitterPowerSpinner.getAdapter().getCount(); i++) {
            String item = (String) mTransmitterPowerSpinner.getAdapter().getItem(i);
            if (item.toLowerCase().startsWith(transmitterPower.toLowerCase())) {
                selectedItem = i;
                break;
            }
        }
        mTransmitterPowerSpinner.setSelection(selectedItem);
    }
    private void setSelectedAdvertisingRate(int advertisingRate) {
        int selectedItem = 0;
        for (int i = 0; i < mAdvertisingRateSpinner.getAdapter().getCount(); i++) {
            String item = (String) mAdvertisingRateSpinner.getAdapter().getItem(i);
            if (item.startsWith(""+advertisingRate)) {
                selectedItem = i;
                break;
            }
        }
        mAdvertisingRateSpinner.setSelection(selectedItem);
    }


    private String getSelectedTransmitterPower() {
        return ((String)mTransmitterPowerSpinner.getSelectedItem()).toUpperCase();
    }

    private int getSelectedAdvertisingRate() {
        return Integer.parseInt(((String)mAdvertisingRateSpinner.getSelectedItem()).split(" ")[0]);
    }

    private void updateViewsForBeaconType() {
        String type = getSelectedBeaconType();
        Log.d(TAG, "Selected beacon type is "+type);
        BeaconTypeAttributes attrs = mBeaconTypeAttributseMap.get(type);
        Log.d(TAG, "attrs is: "+attrs);
        for (int i = 0; i < 3; i++) {
            if (attrs.identifierNames.length < i+1) {
                mIdentifierLabels[i].setVisibility(View.GONE);
                mIdentifierEditTexts[i].setVisibility(View.GONE);
            }
            else {
                mIdentifierLabels[i].setVisibility(View.VISIBLE);
                mIdentifierEditTexts[i].setVisibility(View.VISIBLE);
                mIdentifierLabels[i].setText(attrs.identifierNames[i]);
                mIdentifierEditTexts[i].setHint(attrs.identifierExamplePatterns[i]);
            }
        }
        Log.d(TAG, "Data field length is "+attrs.dataFieldNames.length);
        if (attrs.dataFieldNames.length == 0) {
            Log.d(TAG, "hiding data filed");
            mDataFieldLabel.setVisibility(View.GONE);
            mDataFieldEditText.setVisibility(View.GONE);
        }
        else {
            Log.d(TAG, "showing data filed");
            mDataFieldLabel.setVisibility(View.VISIBLE);
            mDataFieldEditText.setVisibility(View.VISIBLE);
            mDataFieldLabel.setText(attrs.dataFieldNames[0]);
            mDataFieldEditText.setHint(attrs.dataFieldExamplePatterns[0]);
        }
    }

    public boolean validate() {
        if (mNameEditText.getText().length() == 0) {
            return showError("Name invalid", "You must provide a name for the transmitter");
        }
        if (mMeasuredPowerEditText.getText().length() == 0) {
            mMeasuredPowerEditText.setText("-59");
        }
        try {
            int value = Integer.parseInt(mMeasuredPowerEditText.getText().toString());
            if (value > 0 || value < -255) {
                return showError("measured power invalid", "Must be a nagative integer between -1 and -255");
            }
        }
        catch (NumberFormatException e) {
            return showError("measured power invalid", "Must be a nagative integer between -1 and -255");
        }


        Log.d(TAG, "Validating beacon");
        String type = getSelectedBeaconType();
        Log.d(TAG, "Selected beacon type is "+type);
        BeaconTypeAttributes attrs = mBeaconTypeAttributseMap.get(type);
        Log.d(TAG, "attrs is: "+attrs);
        for (int i = 0; i < 3; i++) {
            if (attrs.identifierNames.length >= i+1) {
                String identifierValue = mIdentifierEditTexts[i].getText().toString();
                if (identifierValue.length() == 0) {
                    mIdentifierEditTexts[i].setText(attrs.identifierExamplePatterns[i]);
                    identifierValue = mIdentifierEditTexts[i].getText().toString();
                }
                if (attrs.identifierMaxValues[i] > 0) {

                    int identifierIntValue = 0;
                    try {
                        identifierIntValue = (Integer.parseInt(identifierValue, attrs.identifierIsHex[i] ? 16 : 10));
                    }
                    catch (NumberFormatException e) {
                        return showError(attrs.identifierNames[i]+" invalid", "Value must be a decimal number.");
                    }
                    if (attrs.identifierMaxValues[i] < identifierIntValue) {
                        String maxValueString = Integer.toString(attrs.identifierMaxValues[i], attrs.identifierIsHex[i] ? 16 : 10);
                        return showError(attrs.identifierNames[i]+" invalid", "Value must be less than or equal to "+maxValueString);
                    }
                    if (identifierIntValue < 0) {
                        return showError(attrs.identifierNames[i]+" invalid", "Value must be greater than or equal to 0");
                    }
                }
                if (!identifierValue.matches(attrs.identifierRegexPatterns[i])) {
                    Log.d(TAG, "regex: "+ attrs.identifierRegexPatterns[i]+" failed to match "+identifierValue);
                    return showError(attrs.identifierNames[i]+" invalid", "Value must be of the form "+attrs.identifierRegexPatterns[i]/*attrs.identifierExamplePatterns[i]*/);
                }
            }
        }
        Log.d(TAG, "Data field length is "+attrs.dataFieldNames.length);
        if (attrs.dataFieldNames.length != 0) {
            String dataValue = mDataFieldEditText.getText().toString();
            if (dataValue.length() == 0) {
                mDataFieldEditText.setText(attrs.dataFieldExamplePatterns[0]);
                dataValue = mDataFieldEditText.getText().toString();
            }
            if (attrs.dataFieldMaxValues[0] > 0) {
                int dataLongValue = (Integer.parseInt(dataValue,  10));
                if (attrs.dataFieldMaxValues[0] < dataLongValue) {
                    String maxValueString = Long.toString(attrs.dataFieldMaxValues[0]);
                    return showError(dataLongValue+" invalid", "Value must be less than or equal to "+maxValueString);
                }
                if (dataLongValue < 0) {
                    return showError(attrs.dataFieldNames[0]+" invalid", "Value must be greater than or equal to 0");
                }
            }
            if (!dataValue.matches(attrs.dataFieldRegexPatterns[0])) {
                Log.d(TAG, "regex: "+ attrs.dataFieldRegexPatterns[0]+" failed to match "+dataValue);
                return showError(attrs.dataFieldNames[0]+" invalid", "Value must be of the form "+attrs.dataFieldRegexPatterns[0]);
            }
        }
        if (type.equalsIgnoreCase("eddystone-url")) {
            String url = mIdentifierEditTexts[0].getText().toString();
            try {
                byte[] bytes = UrlBeaconUrlCompressor.compress(url);
                if (bytes.length > 17) {
                    return showError("URL too long", "The url will not compress to 17 bytes or less using Eddystonne's compression algorithm.  The current compressed length is "+bytes.length+".  Please shorten the URL.");
                }
            } catch (MalformedURLException e) {
                return showError("URL malformed", "The entered URL is not valid");
            }
        }
        Log.d(TAG, "beacon is valid");

        return true;
    }
    public boolean showError(String title, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
        return false;
    }
}
