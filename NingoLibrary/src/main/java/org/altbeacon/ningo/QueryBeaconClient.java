package org.altbeacon.ningo;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dyoung on 12/11/17.
 */

public class QueryBeaconClient {
    private static final String TAG = GetBeaconClient.class.getSimpleName();
    public static final String AUTH_ERROR = "AUTH_ERROR";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String SERVER_ERROR = "SERVER_ERROR";

    private static final String URL = " https://ningo-api.herokuapp.com/api/public/v1/beacons/query";
    private String mAuthenticationToken;

    public QueryBeaconClient(String authenticationToken) {
        mAuthenticationToken = authenticationToken;
    }

    private QueryBeaconClient() {

    }

    //curl -i -X POST https://ningo-api.herokuapp.com/api/public/v1/beacons/query -d '{"latitude":38, "longitude":-77, "radius_meters": 100000}' -H 'Content-Type: Application/json' -H 'Authorization: Token token="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoxLCJleHAiOjE0OTIyODQ2MDl9.CMfGArSZ_rQw0C6sduVSF9kQfiCU0z9bH69A4C_6x5Y"'
    //{"beacons":[{"identifier":"8aefb031-6c32-486f-825b-e26fa193487d_40_1_ibeacon","first_identifier":"8aefb031-6c32-486f-825b-e26fa193487d","beacon_type":"ibeacon","wikibeacon_datum":{"latitude":"37.6056685","longitude":"-77.527089","country":"United States of America","country_code":"us","postcode":"23226","state":"Virginia","state_district":null,"city":null,"suburb":"Tuckahoe","road":"Forest Avenue","house_number":null,"first_detected_at":"2014-01-21T15:56:28.365Z","last_detected_at":"2014-01-21T15:56:28.365Z"}}, ...}
    private RestRequest mRestRequest = new RestRequest();

    /**
     * Queries for a list of beacons within a radius of a location
     * @param latitude (degrees)
     * @param longitude (degrees)
     * @param radiusMeters
     * @param responseHandler
     */
    public void query(double latitude, double longitude, double radiusMeters, final QueryBeaconClient.QueryBeaconClientResponseHandler responseHandler) {
        Map<String,String> headers = mRestRequest.getHeadersForJsonRequestWithBody();
        headers.put("Authorization", "Token token=\""+mAuthenticationToken+"\"");

        JSONObject bodyJson = null;
        try {
            bodyJson = new JSONObject();
            bodyJson.put("latitude", latitude);
            bodyJson.put("longitude", longitude);
            bodyJson.put("radius_meters", radiusMeters);
        }
        catch (JSONException e) {
            responseHandler.onFail(e);
        }

        mRestRequest.makeRequest(URL, "POST", bodyJson.toString(), headers, new RestRequest.RestResponseHandler() {
            @Override
            public void onFail(Exception e) {
                responseHandler.onFail(e);
            }

            @Override
            public void onResponse(int httpStatus, Map<String, List<String>> headers, String body) {
                JSONObject responseJson = null;
                String responseBodyError = "";
                String authToken = null;
                try {
                    responseJson = new JSONObject(body);
                    if (responseJson.has("error")) {
                        responseBodyError = responseJson.getString("error");
                    }
                }
                catch (JSONException e) { /* ignored */ }

                if (httpStatus == 200) {
                    ArrayList<Beacon> beacons = new ArrayList<>();
                    try {
                        JSONArray beaconsJson = responseJson.getJSONArray("beacons");
                        for (int i = 0; i < beaconsJson.length(); i++) {
                            Beacon beacon = Beacon.fromJson((JSONObject)beaconsJson.get(i));
                            beacons.add(beacon);
                        }
                    }
                    catch (JSONException e) {
                        Log.w(TAG, "json cannot be pasrsed", e);
                        responseHandler.onResponse(null, SERVER_ERROR, "Cannot parse response");
                        return;
                    }
                    responseHandler.onResponse(beacons, null, null);
                }
                else if (httpStatus == 401) {
                    responseHandler.onResponse(null, AUTH_ERROR, AUTH_ERROR);
                }
                else if (httpStatus == 404) {
                    responseHandler.onResponse(null, NOT_FOUND, NOT_FOUND);
                }
                else {
                    responseHandler.onResponse(null, SERVER_ERROR, ""+httpStatus);
                }
            }
        });
    }
    public interface QueryBeaconClientResponseHandler {
        public void onFail(Exception e);

        /**
         * Returns the matching beacon list if successful.  or an errorCode if something went wrong
         * @param beacons
         * @param errorCode
         * @param errorDescription
         */
        public void onResponse(List<Beacon> beacons, String errorCode, String errorDescription);
    }
}
