package org.altbeacon.ningo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by dyoung on 11/16/17.
 */

public class Beacon {
    private String mJsonString;
    private String mIdentifier;
    private MetadataV1 mMetadata;
    private Double mWikiBeaconLatitude;
    private Double mWikiBeaconLongitude;

    public MetadataV1 getMetadata() {
        return mMetadata;
    }

    public void setMetadata(MetadataV1 metadata) {
        mMetadata = metadata;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public Double getWikiBeaconLatitude() {
        return mWikiBeaconLatitude;
    }

    public Double getWikiBeaconLongitude() {
        return mWikiBeaconLongitude;
    }

    public void setIdentifier(String identifier) {
        mIdentifier = identifier;
    }

    public static Beacon fromJson(JSONObject json) throws JSONException {

        Beacon beacon = new Beacon();
        try {
            beacon.mJsonString = json.toString(2);
        }
        catch (JSONException e) {

        }
        beacon.parseJson(json);
        return beacon;
    }

    JSONObject toJson() throws JSONException {
        JSONObject json = null;
        try {
            String jsonString = getJsonString();
            if (jsonString != null) {
                json = new JSONObject(jsonString);
            }
        }
        catch (JSONException e) {}

        if (json == null) {
            json = new JSONObject();
            json.put("beacon", new JSONObject());
        }
        JSONObject beacon = json.getJSONObject("beacon");
        beacon.put("identifier", mIdentifier);
        if (mMetadata != null) {
            beacon.put("metadata", mMetadata.toJson());
        }
        return json;
    }

    public String getJsonString() {
        return mJsonString;
    }

    public void setJsonString(String rawJson) throws  JSONException {
        parseJson(rawJson);
        mJsonString = rawJson;
    }

    private void parseJson(JSONObject json) throws JSONException {
        if (json.has("beacon")) {
            json = json.getJSONObject("beacon");
        }
        if (json.has("identifier")) {
            mIdentifier = json.getString("identifier");
        }
        if (json.has("wikibeacon_datum") && !json.isNull("wikibeacon_datum")) {
            JSONObject wikibeaconDatum = json.getJSONObject("wikibeacon_datum");
            if (wikibeaconDatum.has("latitude") && !wikibeaconDatum.isNull("latitude")) {

                mWikiBeaconLatitude = wikibeaconDatum.getDouble("latitude");
            }
            if (wikibeaconDatum.has("longitude") && !wikibeaconDatum.isNull("longitude")) {
                mWikiBeaconLongitude = wikibeaconDatum.getDouble("longitude");
            }
        }
        if (json.has("metadata") && !json.isNull("metadata")) {
            mMetadata = MetadataV1.fromJson(json.getJSONObject("metadata"));
        }
    }

    private void parseJson(String json) throws JSONException {
        parseJson(new JSONObject(json));
    }
}


/*

 {"beacon":{"identifier":"2f234454-cf6d-4a0f-adf2-f4911ba9ffa6_1_1_ibeacon","first_identifier":"2f234454-cf6d-4a0f-adf2-f4911ba9ffa6","beacon_type":"ibeacon","wikibeacon_datum":{"latitude":"38.93","longitude":"-77.0","country":"United States","country_code":null,"postcode":null,"state":"Nebraska","state_district":null,"city":"Omaha","suburb":null,"road":null,"house_number":null,"first_detected_at":"2015-09-29T17:52:35.341Z","last_detected_at":"2015-10-01T17:52:43.407Z"}}}

 */

