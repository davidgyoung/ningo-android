package org.altbeacon.ningo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyoung on 12/1/17.
 */

public class MetadataV1 {
    private String mLoctype;
    private Double mRadius;
    private Location mLocation;
    private List<InfoRecord> mInfo;
    private List<String> mCategories;

    public String getLoctype() {
        return mLoctype;
    }

    public void setLoctype(String loctype) {
        mLoctype = loctype;
    }

    public Double getRadius() {
        return mRadius;
    }

    public void setRadius(Double radius) {
        mRadius = radius;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public List<InfoRecord> getInfo() {
        return mInfo;
    }

    public void setInfo(List<InfoRecord> info) {
        mInfo = info;
    }

    public List<String> getCategories() {
        return mCategories;
    }

    public void setCategories(List<String> categories) {
        mCategories = categories;
    }

    static MetadataV1 fromJson(JSONObject json) throws JSONException {
        MetadataV1 metadata = new MetadataV1();
        if (json.has("location") && !json.isNull("location")) {
            metadata.mLocation = Location.fromJson(json.getJSONObject("location"));
        }
        if (json.has("loctype") && !json.isNull("loctype")) {
            metadata.mLoctype = json.getString("loctype");
        }
        if (json.has("radius") && !json.isNull("radius")) {
            metadata.mRadius = json.getDouble("radius");
        }
        if (json.has("categories") && !json.isNull("categories")) {
            JSONArray jsonArray = json.getJSONArray("categories");
            ArrayList<String> categories = new ArrayList<>();
            metadata.mCategories = categories;
            for (int i = 0; i < jsonArray.length(); i++) {
                categories.add(jsonArray.getString(i));
            }
        }
        if (json.has("info") && !json.isNull("info")) {
            ArrayList<InfoRecord> infoRecords = new ArrayList<>();
            metadata.setInfo(infoRecords);
            JSONArray infoArray = json.getJSONArray("info");
            for (int i = 0; i < infoArray.length(); i++) {
                JSONObject infoJson = infoArray.getJSONObject(i);
                InfoRecord infoRecord = InfoRecord.fromJson(infoJson);
                infoRecords.add(infoRecord);
            }
        }
        return metadata;
    }

    JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (mLocation != null) {
            json.put("location", mLocation.toJson());
        }
        if (mLoctype != null) {
            json.put("loctype", mLoctype);
        }
        if (mRadius != null) {
            json.put("radius", mRadius);
        }
        if (mInfo != null) {
            JSONArray jsonArray = new JSONArray();
            json.put("info", jsonArray);
            for (InfoRecord infoRecord: mInfo) {
                jsonArray.put(infoRecord.toJson());
            }
        }
        return json;
    }

}