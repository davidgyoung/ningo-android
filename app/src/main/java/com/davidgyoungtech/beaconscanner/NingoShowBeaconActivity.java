package com.davidgyoungtech.beaconscanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.altbeacon.ningo.Beacon;
import com.altbeacon.ningo.GetBeaconClient;
import com.altbeacon.ningo.Location;
import com.altbeacon.ningo.MetadataV1;
import com.altbeacon.ningo.PostBeaconClient;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dyoung on 11/30/17.
 */

public class NingoShowBeaconActivity extends Activity {

    Beacon mBeacon = new Beacon();
    String mBeaconIdentifier = null;
    boolean mJsonEdited = false;
    boolean mLocationEdited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GetBeaconClient getBeaconClient = null;
        setContentView(R.layout.activity_ningo_show_beacon);
        Settings settings = new Settings(this);

        ((EditText)findViewById(R.id.raw_json)).addTextChangedListener(mJsonChangedWatcher);
        ((EditText)findViewById(R.id.latitude)).addTextChangedListener(mLocationChangedWatcher);
        ((EditText)findViewById(R.id.longitude)).addTextChangedListener(mLocationChangedWatcher);

        String authToken = settings.getSetting(Settings.NINGO_API_TOKEN,null);
        getBeaconClient = new GetBeaconClient(authToken);
        if (authToken == null) {
            Intent intent = new Intent(this, NingoLoginActivity.class);
            startActivity(intent);
            return;
        }
        try {
            mBeaconIdentifier = this.getIntent().getStringExtra("ningo_beacon_identifier");
            mBeacon.setIdentifier(mBeaconIdentifier);
        }
        catch (NullPointerException e) {}
        ((TextView)findViewById(R.id.identifier)).setText(mBeaconIdentifier);

        findViewById(R.id.progressView).setVisibility(View.VISIBLE);
        getBeaconClient.get(mBeaconIdentifier, new GetBeaconClient.GetBeaconClientResponseHandler() {
            @Override
            public void onFail(Exception e) {
                findViewById(R.id.progressView).setVisibility(View.GONE);
                Toast.makeText(NingoShowBeaconActivity.this, "Failed to fetch beacon data: "+e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Beacon beacon, String errorCode, String errorDescription) {
                findViewById(R.id.progressView).setVisibility(View.GONE);
                if (errorCode == GetBeaconClient.AUTH_ERROR) {
                    Intent intent = new Intent(NingoShowBeaconActivity.this, NingoLoginActivity.class);
                    intent.putExtra("ningo_beacon_identifier", mBeaconIdentifier);
                    startActivity(intent);
                }
                else if (errorCode != null) {
                    Toast.makeText(NingoShowBeaconActivity.this, "Failed to fetch beacon data: "+errorDescription, Toast.LENGTH_LONG).show();
                }
                else {
                    mBeacon = beacon;
                    String rawJson = beacon.getJsonString();
                    ((EditText)findViewById(R.id.raw_json)).setText(rawJson);
                    Double latitude = beacon.getWikiBeaconLatitude();
                    Double longitude = beacon.getWikiBeaconLongitude();
                    if (beacon.getMetadata() != null && beacon.getMetadata().getLocation() != null) {
                        if (beacon.getMetadata().getLocation().getLatitude() != null) {
                            latitude = beacon.getMetadata().getLocation().getLatitude();
                        }
                        if (beacon.getMetadata().getLocation().getLongitude() != null) {
                            longitude = beacon.getMetadata().getLocation().getLongitude();
                        }

                    }
                    if (latitude != null) {
                        ((EditText)findViewById(R.id.latitude)).setText(""+latitude);
                    }
                    if (longitude != null) {
                        ((EditText)findViewById(R.id.longitude)).setText(""+longitude);
                    }
                }
            }
        });

    }

    private TextWatcher mJsonChangedWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mJsonEdited = true;

        }
    };

    private TextWatcher mLocationChangedWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mLocationEdited = true;

        }
    };

    public void saveChanges(View view) {
        String jsonString = ((EditText)findViewById(R.id.raw_json)).getText().toString();
        Settings settings = new Settings(this);
        String authToken = settings.getSetting(Settings.NINGO_API_TOKEN,null);
        PostBeaconClient postBeaconClient = new PostBeaconClient(authToken);
        try {
            if (mJsonEdited) {
                mBeacon.setJsonString(jsonString);
            }
            if (mLocationEdited) {
                MetadataV1 metadata = mBeacon.getMetadata();
                if (metadata == null) {
                    metadata = new MetadataV1();
                    mBeacon.setMetadata(metadata);
                }
                Location location = metadata.getLocation();
                if (location == null) {
                    location = new Location();
                    metadata.setLocation(location);
                }
                String latitudeString = ((EditText)findViewById(R.id.latitude)).getText().toString();
                String longitudeString = ((EditText)findViewById(R.id.longitude)).getText().toString();
                try {
                    Double latitude = Double.parseDouble(latitudeString);
                    Double longitude = Double.parseDouble(longitudeString);
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);
                }
                catch (NumberFormatException e) {
                }
            }
        }
        catch (JSONException e) {
            Toast.makeText(this, "JSON is invalid.", Toast.LENGTH_LONG).show();
            return;
        }
        findViewById(R.id.progressView).setVisibility(View.VISIBLE);
        postBeaconClient.post(mBeacon, new PostBeaconClient.PostBeaconClientResponseHandler() {

            @Override
            public void onFail(Exception e) {
                findViewById(R.id.progressView).setVisibility(View.GONE);
                Toast.makeText(NingoShowBeaconActivity.this, "Error saving changes: "+e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Beacon beacon, String errorCode, String errorDescription) {
                findViewById(R.id.progressView).setVisibility(View.GONE);

                if (errorCode != null) {
                    Toast.makeText(NingoShowBeaconActivity.this, "Error saving changes: "+errorDescription, Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(NingoShowBeaconActivity.this, "Changes saved successfully", Toast.LENGTH_LONG).show();
                    ((EditText)findViewById(R.id.raw_json)).setText(beacon.getJsonString());
                }
            }
        });





    }
}
