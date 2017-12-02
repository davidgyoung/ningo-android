package com.altbeacon.ningo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by dyoung on 11/30/17.
 */

public class PostBeaconClient {
    public static final String AUTH_ERROR = "AUTH_ERROR";
    public static final String INVALID_DATA = "INVALID_DATA";
    public static final String SERVER_ERROR = "SERVER_ERROR";

    private static final String URL = " https://ningo-api.herokuapp.com/api/public/v1/beacons";
    private String mAuthenticationToken;

    public PostBeaconClient(String authenticationToken) {
        mAuthenticationToken = authenticationToken;
    }

    private PostBeaconClient() {

    }
    // curl -X POST https://ningo-api.herokuapp.com/api/public/v1/beacons -d '{"beacon":{"identifier":"2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6_1_2_ibeacon", "metadata": {"location":{"longitude":38.93,"latitude":-77.22}}}}' -H 'Content-Type: application/json' -H 'Authorization: Token token="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoxLCJleHAiOjE0OTcwMzMxNTJ9.qk0oNd1TWrpQ_1fp4tbO9h4ueIJ2IthHeEaeGLKAYfU"'
    private RestRequest mRestRequest = new RestRequest();

    public void post(Beacon beacon, final com.altbeacon.ningo.PostBeaconClient.PostBeaconClientResponseHandler responseHandler) {
        Map<String, String> headers = mRestRequest.getHeadersForJsonRequestWithBody();
        headers.put("Authorization", "Token token=\"" + mAuthenticationToken + "\"");

        JSONObject bodyJson = null;
        try {
            bodyJson = beacon.toJson();
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
                try {
                    responseJson = new JSONObject(body);
                    if (responseJson.has("error")) {
                        responseBodyError = responseJson.getString("error");
                    }
                    if (responseJson.has("errors")) {
                        //{"errors":{"metadata":["The property '#/' did not contain a required property of 'location' in schema 8b4b94a0-17b0-534f-945c-fe960aa26dd0#"]}}
                        JSONObject errors = responseJson.getJSONObject("errors");
                        String errorKey = errors.keys().next();
                        JSONArray errorValues = errors.getJSONArray(errorKey);
                        String errorValue = (String) errorValues.get(0);
                        responseBodyError = "Error with "+errorKey+": "+errorValue;
                    }

                } catch (JSONException e) { /* ignored */ }

                if (httpStatus >= 200 && httpStatus <= 299) {

                    Beacon beacon = null;
                    try {
                        beacon = Beacon.fromJson(responseJson);
                    }
                    catch (JSONException e) {}

                    if (beacon != null) {
                        responseHandler.onResponse(beacon, null, null);
                    } else {
                        responseHandler.onResponse(null, SERVER_ERROR, "Cannot parse response");
                    }
                } else if (httpStatus == 401) {
                    responseHandler.onResponse(null, AUTH_ERROR, AUTH_ERROR);
                } else if (httpStatus == 422) {
                    responseHandler.onResponse(null, INVALID_DATA, responseBodyError);
                } else {
                    responseHandler.onResponse(null, SERVER_ERROR, "" + httpStatus);
                }
            }
        });
    }

    public interface PostBeaconClientResponseHandler {
        public void onFail(Exception e);

        /**
         * Returns the beacon if successful.  or an errorCode if something went wrong
         *
         * @param beacon
         * @param errorCode
         * @param errorDescription
         */
        public void onResponse(Beacon beacon, String errorCode, String errorDescription);
    }
}
