package com.altbeacon.ningo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dyoung on 12/1/17.
 */

public class Location {
    private Double mLongitude;
    private Double mLatitude;
    private Double mAltitude;
    private Double mHaccuracy;
    private Double mVaccuracy;
    private String mType;

    public Double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(Double longitude) {
        mLongitude = longitude;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(Double latitude) {
        mLatitude = latitude;
    }

    public Double getAltitude() {
        return mAltitude;
    }

    public void setAltitude(Double altitude) {
        mAltitude = altitude;
    }

    public Double getHaccuracy() {
        return mHaccuracy;
    }

    public void setHaccuracy(Double haccuracy) {
        mHaccuracy = haccuracy;
    }

    public Double getVaccuracy() {
        return mVaccuracy;
    }

    public void setVaccuracy(Double vaccuracy) {
        mVaccuracy = vaccuracy;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    static Location fromJson(JSONObject json) throws JSONException {
        Location location = new Location();
        if (json.has("latitude")) {
            location.mLatitude = json.getDouble("latitude");
        }
        if (json.has("longitude")) {
            location.mLongitude = json.getDouble("longitude");
        }
        if (json.has("altitude")) {
            location.mHaccuracy = json.getDouble("altitude");
        }
        if (json.has("haccuracy")) {
            location.mVaccuracy = json.getDouble("haccuracy");
        }
        if (json.has("type")) {
            location.mType = json.getString("type");
        }
        return location;
    }

    JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (mLatitude != null) {
            json.put("latitude", mLatitude);
        }
        if (mLongitude != null) {
            json.put("longitude", mLongitude);
        }
        if (mHaccuracy != null) {
            json.put("haccuracy", mHaccuracy);
        }
        if (mVaccuracy != null) {
            json.put("vaccuracy", mVaccuracy);
        }
        if (mVaccuracy != null) {
            json.put("vaccuracy", mVaccuracy);
        }
        if (mAltitude != null) {
            json.put("altitude", mAltitude);
        }
        if (mType != null) {
            json.put("type", mType);
        }
        return json;
    }

}
