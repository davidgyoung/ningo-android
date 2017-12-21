# Ningo SDK for Android and Reference app

This repository contains two components:

app - A generic beacon scanning app that serves as a reference for using the Ningo SDK
NingoLibrary - an Android Library that hosts the Ningo SDK.

You can use the app for testing the Ningo SDK, or as a starting point for your own custom application.

# Ningo SDK for Android

The Ningo SDK allows you to easily use beacon data from the Ningo online beacon database in your mobile apps.  This database provides meaning to detected Bluetooth beacons by through read/write access to location (latitude/longitude) and metadata (like building floor, etc.). See the Ningo website for more info on metadata.

If you use the reference app, it will compile the SDK from source.  If you want to download the binary
library aar file for configuration with your own Android app, read below.

## Configuring the Android SDK with Your App

1. Download the ningo-libarry-0.1.aar file from the releases section of this repo (or a newer version as avaialble.)
2. Configure your outermost build.gradle file to reference a flatDir repo with a section like this:

 ```
 allprojects {
     repositories {
         jcenter()
         google()
         flatDir {
             dirs 'libs'
         }
     }
 }
 ```

3. In your innermost build.gradle file, add a dependency to the SDK like this:

 ```
    compile 'org.altbeacon:ningo-library:0+@aar'

 ```

4. Create a libs folder in the same folder as your innmermost build.gradle file (if it does not exist already.)

5. In the libs folder above, download and save the aar file from step 1


One you complete the above steps, you will be able to access the SDK's classes from your app's code.

## Read Only and Read/Write Access

The Android client works in two modes: read only and read/write.

To read data, you need only embed a Ningo auth token string inside your Android app.  You can use this token to read beacon metadata for a specific beacon identifier or a group of beacons within a requested distance of a location.  Here is an example API call:

```
public static final String READONLY_API_TOKEN=...;
public static final int QUERY_RADIUS_METERS = 1000;

// Find all beacons within 1 kilometer of a location
mQueryBeaconClient = new QueryBeaconClient(token);
mQueryBeaconClient.query(location.getLatitude(), location.getLongitude(), QUERY_RADIUS_METERS, new QueryBeaconClient.QueryBeaconClientResponseHandler() {
    @Override
    public void onFail(Exception e) {
        Log.d(TAG, "Failed to query beacons from ningo based on distance", e);
    }
    @Override
        public void onResponse(List<Beacon> beacons, String errorCode, String errorDescription) {
        if (errorCode != null) {
            Log.d(TAG, "Failed to query beacons from ningo based on distance: "+errorCode);
        }
        else {
            mNingoBeacons = beacons;
        }
    }
});
```


Either way, the returned Beacon objects can be accessed like this:

```
   NingoBeacon ningoBeacon = mNingoBeacons.get(0);
   Metadata ningoMetadata = ningoBeacon.getMetadata();
   Location ningoLocation = ningoMetadata.getLocation();
   double latitude = ningoLocation.getLatitude();
```

To write data, app users first need an account on the Ningo system.  Using the email and password for this account, they can get a read write auth token like this:

```
mAuthClient = new AuthenticationClient();
mAuthClient.authenticate(email, password, new AuthenticationClient.AuthenticationClientResponseHandler() {
    @Override
    public void onFail(Exception e) {
        Log.d(TAG, "Failed to make auth call");
    }
    @Override
    public void onResponse(String apiToken, String errorCode, String errorDescription) {
        if (apiToken == null) {
            Log.d(TAG, "Login failed.  Credentials may be invalid.");
        }
        else {
            Log.d(TAG, "Login success.");
            mReadWriteAuthToken = apiToken;
            }
        }
    }
});
```


This auth token can then be used to perform all the read-only mode APIs above as well as the following write API:


```
mPostBeaconClient = new PostBeaconClient(mReadWriteAuthToken);
mPostBeaconClient.post(mBeacon, new PostBeaconClient.PostBeaconClientResponseHandler() {
    @Override
    public void onFail(Exception e) {
        Log.d(TAG, "Error saving changes: "+e.getMessage());
    }

    @Override
    public void onResponse(Beacon beacon, String errorCode, String errorDescription) {
        if (errorCode != null) {
            Log.d(TAG, "Error saving changes: "+e.getMessage());
        }
        else {
            Log.d(TAG, "Changes saved successfully");
        }
    }
});
```

A full reference app, along with the source code to this SDK is available in the root of this repository.

## API Tokens

In order to use this SDK you must obtain an API token.  There are two types of API tokens, both of which require you to create an account on the Ningo server with a username and password.  See above for how to use the SDK in read/write mode.

### Obtaining a Readonly Token

Readonly tokens provide the ablity to query beacon data based on a static token embedded in your mobile app.
As its name implies, you cannot use such a token to write data.  To obtain a readonly token, you may run a command like this from a Linux or Unix workstation:

 ```
 $ curl -i -XPOST https://ningo-api.herokuapp.com/api/public/v1/authentication -H "Content-Type: application/json" -d '{"email":"david@radiusnetworks.com","password":"xxxxxxxx", "readonly": "true"}'
 HTTP/1.1 200 OK
 Server: Cowboy
 Date: Wed, 29 Mar 2017 21:14:20 GMT
 Connection: keep-alive
 X-Frame-Options: SAMEORIGIN
 X-Xss-Protection: 1; mode=block
 X-Content-Type-Options: nosniff
 Content-Type: application/json; charset=utf-8
 Etag: W/"d06d7264a58b218419a95fcccffef456"
 Cache-Control: max-age=0, private, must-revalidate
 X-Request-Id: bf1db7ee-0c09-4fec-90ae-a59fbe9e010b
 X-Runtime: 0.081724
 Transfer-Encoding: chunked
 Via: 1.1 vegur

 {"auth_token":"akL9eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoxLCJleHAiOjE0OTA5MDg0NjB9.bjmSpLI_bV30B_M6brEHLQEUac_fqGJOEOmj6urmJFc", "readonly": true}
 ```

Once you have this token you can put it in your app like this:

```
public static final String API_TOKEN="akL9eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoxLCJleHAiOjE0OTA5MDg0NjB9.bjmSpLI_bV30B_M6brEHLQEUac_fqGJOEOmj6urmJFc";
```
