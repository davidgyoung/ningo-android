package com.davidgyoungtech.beaconscanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.altbeacon.ningo.AuthenticationClient;


/**
 * Created by dyoung on 11/24/17.
 */

public class NingoLoginActivity extends Activity {

    AuthenticationClient mAuthClient = new AuthenticationClient();
    String mBeaconIdentifier = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ningo_login);
        Settings settings = new Settings(NingoLoginActivity.this);

        ((EditText)findViewById(R.id.email)).setText(settings.getSetting(Settings.NINGO_EMAIL,""));
        ((EditText)findViewById(R.id.password)).setText(settings.getSetting(Settings.NINGO_PASSWORD,""));
        final String password = ((EditText)findViewById(R.id.password)).getText().toString();
        try {
            mBeaconIdentifier = this.getIntent().getStringExtra("ningo_beacon_identifier");
        }
        catch (NullPointerException e) {}
    }

    public void login(View view) {
        final String email = ((EditText)findViewById(R.id.email)).getText().toString();
        final String password = ((EditText)findViewById(R.id.password)).getText().toString();
        findViewById(R.id.progressView).setVisibility(View.VISIBLE);
        mAuthClient.authenticate(email, password, new AuthenticationClient.AuthenticationClientResponseHandler() {
            @Override
            public void onFail(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.progressView).setVisibility(View.GONE);
                    }
                });

            }

            @Override
            public void onResponse(String apiToken, String errorCode, String errorDescription) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.progressView).setVisibility(View.GONE);
                    }
                });
                if (apiToken != null) {
                    Settings settings = new Settings(NingoLoginActivity.this);
                    settings.saveSetting(Settings.NINGO_API_TOKEN, apiToken);
                    settings.saveSetting(Settings.NINGO_EMAIL, email);
                    settings.saveSetting(Settings.NINGO_PASSWORD, password);

                    Intent intent = new Intent(NingoLoginActivity.this, NingoShowBeaconActivity.class);
                    intent.putExtra("ningo_beacon_identifier", mBeaconIdentifier);
                    NingoLoginActivity.this.startActivity(intent);

                    Toast.makeText(NingoLoginActivity.this, "Login succeeded", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(NingoLoginActivity.this, "Login Failed: "+errorDescription, Toast.LENGTH_LONG).show();
                }

            }
        });

    }

}
