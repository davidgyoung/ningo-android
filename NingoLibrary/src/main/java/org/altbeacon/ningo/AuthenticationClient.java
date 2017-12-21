package org.altbeacon.ningo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Retreives an auth token from the Ningo server needed to read and write beacon records.
 * A String username and password are supplied.  If authentication is successful, a String
 * auth token is returned.
 *
 * Created by dyoung on 11/16/17.
 */

public class AuthenticationClient {
    /**
     * Returned as the errorCode in the response if the email/password combo is invalid
     */
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";

    /**
     * Returned as the errorCode in the response if the server returns an unexpected error response.
     */
    public static final String SERVER_ERROR = "SERVER_ERROR";

    private static final String URL = "https://ningo-api.herokuapp.com/api/public/v1/authentication";

    // curl -i -XPOST https://ningo-api.herokuapp.com/api/public/v1/authentication -H 'Content-Type: application/json' -d '{"email":"d2@test.com","password":"abc123"}'
    private RestRequest mRestRequest = new RestRequest();

    /**
     * Authenticates with the Ningo server, returning an authToken in the responseHandler if successful.
     *
     * @param email
     * @param password
     * @param responseHandler
     */
    public void authenticate(String email, String password, final AuthenticationClientResponseHandler responseHandler) {
        Map<String,String> headers = mRestRequest.getHeadersForJsonRequestWithBody();

        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("email", email);
            bodyJson.put("password", password);
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
                JSONObject responseJson;
                String responseBodyError = "";
                String authToken = null;
                try {
                    responseJson = new JSONObject(body);
                    if (responseJson.has("auth_token")) {
                        authToken = responseJson.getString("auth_token");
                    }
                    if (responseJson.has("error")) {
                        responseBodyError = responseJson.getString("error");
                    }
                }
                catch (JSONException e) { /* ignored */ }

                if (httpStatus == 200) {
                    if (authToken != null) {
                        responseHandler.onResponse(authToken, INVALID_CREDENTIALS, INVALID_CREDENTIALS);
                    }
                    else {
                        responseHandler.onResponse(null, SERVER_ERROR, "auth token missing from server response");
                    }
                }
                else if (httpStatus == 401) {
                    responseHandler.onResponse(null, INVALID_CREDENTIALS, INVALID_CREDENTIALS);
                }
                else {
                    responseHandler.onResponse(null, SERVER_ERROR, ""+httpStatus);
                }
            }
        });
    }

    public interface AuthenticationClientResponseHandler {
        /**
         * Called if the request to the server cannot be made at all, or no response is obtained.
         * @param e
         */
        public void onFail(Exception e);

        /**
         * Returns the apiToken if successful.  or an errorCode if something went wrong.  If the
         * errorCode is `INVALID_CREDENTIALS` then the email/password combo was invalid.
         * @param authToken
         * @param errorCode
         * @param errorDescription
         */
        public void onResponse(String authToken, String errorCode, String errorDescription);
    }
}
