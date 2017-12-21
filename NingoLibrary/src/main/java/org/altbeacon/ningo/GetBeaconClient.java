package org.altbeacon.ningo;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by dyoung on 11/16/17.
 */

public class GetBeaconClient {
    private static final String TAG = GetBeaconClient.class.getSimpleName();
    public static final String AUTH_ERROR = "AUTH_ERROR";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String SERVER_ERROR = "SERVER_ERROR";

    private static final String URL = " https://ningo-api.herokuapp.com/api/public/v1/beacons/";
    private String mAuthenticationToken;

    public GetBeaconClient(String authenticationToken) {
        mAuthenticationToken = authenticationToken;
    }

    private GetBeaconClient() {

    }

    // curl -i -XGET https://ningo-api.herokuapp.com/api/public/v1/beacons/2f234454-cf6d-4a0f-adf2-f4911ba9ffa6_1_1_ibeacon -H 'Authorization: Token token="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoxLCJleHAiOjE0OTA5MDg0NjB9.bjmSpLI_bV30B_M6brEHLQEUac_fqGJOEOmj6urmJKg"'    private RestRequest mRestRequest = new RestRequest();
    private RestRequest mRestRequest = new RestRequest();

    public void get(String identifier, final GetBeaconClient.GetBeaconClientResponseHandler responseHandler) {
        Map<String,String> headers = mRestRequest.getHeadersForJsonRequestWithBody();
        headers.put("Authorization", "Token token=\""+mAuthenticationToken+"\"");
        mRestRequest.makeRequest(URL+identifier, "GET", null, headers, new RestRequest.RestResponseHandler() {
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
                    Beacon beacon = null;
                    try {
                        beacon = Beacon.fromJson(responseJson);
                    }
                    catch (JSONException e) {
                        Log.w(TAG, "json cannot be pasrsed", e);
                    }
                    if (beacon != null) {
                        responseHandler.onResponse(beacon, null, null);
                    }
                    else {
                        responseHandler.onResponse(null, SERVER_ERROR, "Cannot parse response");
                    }
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
    public interface GetBeaconClientResponseHandler {
        public void onFail(Exception e);

        /**
         * Returns the beacon if successful.  or an errorCode if something went wrong
         * @param beacon
         * @param errorCode
         * @param errorDescription
         */
        public void onResponse(Beacon beacon, String errorCode, String errorDescription);
    }
}
