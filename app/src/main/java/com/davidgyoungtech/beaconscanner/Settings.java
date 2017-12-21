package com.davidgyoungtech.beaconscanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by dyoung on 11/24/17.
 */

public class Settings {
    public static final String NINGO_EMAIL = "NINGO_EMAIL";
    public static final String NINGO_PASSWORD = "NINGO_PASSWORD";
    public static final String NINGO_READWRITE_API_TOKEN = "NINGO_READWRITE_API_TOKEN";
    public static final String NINGO_READONLY_API_TOKEN = "NINGO_READONLY_API_TOKEN";
    public static final String NINGO_LOGIN_TIME_MILLIS = "NINGO_LOGIN_TIME_MILLIS";

    private Context mContext;

    public Settings(Context context) {
        mContext = context;
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public String getSetting(String key, String value) {
        if (getSharedPreferences().contains(key)) {
            return getSharedPreferences().getString(key, "");
        }
        else {
            return null;
        }
    }

    public void saveSetting(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

}
