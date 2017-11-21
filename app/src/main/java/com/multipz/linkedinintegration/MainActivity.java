package com.multipz.linkedinintegration;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView img_login, img_profile;
    TextView txt_detail;
    Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        computePakageHash();


        img_login = (ImageView) findViewById(R.id.img_login);
        img_login.setOnClickListener(this);
        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(this);
        img_profile = (ImageView) findViewById(R.id.img_profile);
        txt_detail = (TextView) findViewById(R.id.txt_detail);

        img_login.setVisibility(View.VISIBLE);
        logout.setVisibility(View.GONE);
        img_profile.setVisibility(View.GONE);
        txt_detail.setVisibility(View.GONE);


    }

    private void computePakageHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.multipz.linkedinintegration",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_login:
                HandledLogin();
                break;
            case R.id.logout:
                HandleLogout();
                break;
        }
    }

    private void HandledLogin() {
        LISessionManager.getInstance(getApplicationContext()).init(this, buildScope(), new AuthListener() {
            @Override
            public void onAuthSuccess() {
                // Authentication was successful.  You can now do
                // other calls with the SDK.
                img_login.setVisibility(View.GONE);
                logout.setVisibility(View.VISIBLE);
                img_profile.setVisibility(View.VISIBLE);

                txt_detail.setVisibility(View.VISIBLE);

                FetchProfileInfo();
            }

            @Override
            public void onAuthError(LIAuthError error) {
                // Handle authentication errors

                Log.e("Paresh", error.toString());
            }
        }, true);
    }

    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.W_SHARE, Scope.R_EMAILADDRESS);
    }

    private void HandleLogout() {
        LISessionManager.getInstance(getApplicationContext()).clearSession();
        img_login.setVisibility(View.VISIBLE);
        logout.setVisibility(View.GONE);
        img_profile.setVisibility(View.GONE);
        txt_detail.setVisibility(View.GONE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Add this line to your existing onActivityResult() method
        LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
    }

    private void FetchProfileInfo() {
        String url = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,public-profile-url,picture-url,email-address,picture-urls::(original))";

        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(this, url, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse apiResponse) {
                // Success!
                JSONObject jsonObject = apiResponse.getResponseDataAsJson();
                try {
                    String firstName = jsonObject.getString("firstName");
                    String lastName = jsonObject.getString("lastName");
                    String pictureUrl = jsonObject.getString("pictureUrl");
                    String emailAddress = jsonObject.getString("emailAddress");

                    Picasso.with(getApplicationContext()).load(pictureUrl).into(img_profile);
                    StringBuilder sb = new StringBuilder();
                    sb.append("First Name:" + firstName);
                    sb.append("\n\n");
                    sb.append("Last Name:" + lastName);
                    sb.append("\n\n");
                    sb.append("Email:" + emailAddress);
                    sb.append("\n\n");
                    txt_detail.setText(sb);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onApiError(LIApiError liApiError) {
                // Error making GET request!
                Log.e("Paresh", liApiError.getMessage());
            }
        });
    }
}
