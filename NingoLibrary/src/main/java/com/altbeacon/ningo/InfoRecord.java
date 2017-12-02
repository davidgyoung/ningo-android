package com.altbeacon.ningo;

import android.icu.text.IDNA;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dyoung on 12/1/17.
 */

public class InfoRecord {
    private String mLang;
    private String mValue;
    private String mDescription;

    public String getLang() {
        return mLang;
    }

    public void setLang(String lang) {
        mLang = lang;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    static InfoRecord fromJson(JSONObject json) throws JSONException {
        InfoRecord info = new InfoRecord();
        if (json.has("lang") && !json.isNull("lang")) {
            info.mLang = json.getString("lang");
        }
        if (json.has("value") && !json.isNull("value")) {
            info.mLang = json.getString("lang");
        }
        if (json.has("description") && !json.isNull("description")) {
            info.mLang = json.getString("description");
        }

        return info;
    }

    JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (mLang != null) {
            json.put("lang", mLang);
        }
        if (mValue != null) {
            json.put("value", mLang);
        }
        if (mDescription != null) {
            json.put("description", mLang);
        }
        return json;
    }
}
