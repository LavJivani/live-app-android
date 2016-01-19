package io.hypertrack.meta;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.model.Verification;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.util.HTConstants;

public class Verify extends AppCompatActivity {

    @Bind(R.id.verificationCode)
    public EditText verificationCodeView;

    @Bind(R.id.register)
    public Button registerButtonView;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.register)
    public void registerPhoneNumber() {

        String verificationCode = verificationCodeView.getText().toString();

        if (!TextUtils.isEmpty(verificationCode) && verificationCode.length() == 4) {
            verifyNumber(verificationCode);
        } else {
            verificationCodeView.setError("Please enter valid verification code");
        }
    }

    public void verifyNumber(String number) {

        SharedPreferences settings = getSharedPreferences("io.hypertrack.meta", Context.MODE_PRIVATE);
        int userId =  settings.getInt(HTConstants.USER_ID, -1);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("verifying code");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String url = "https://meta-api-staging.herokuapp.com/api/v1/users/"+ userId + "/verify_phone_number/";

        Verification verification = new Verification(number);
        Gson gson = new Gson();
        String jsonObjectBody = gson.toJson(verification);

        HTCustomPostRequest<User> request = new HTCustomPostRequest<User>(
                1,
                url,
                jsonObjectBody,
                User.class,
                new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {
                        mProgressDialog.dismiss();
                        Log.d("response", response.toString());

                        SharedPreferences settings = getSharedPreferences("io.hypertrack.meta", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(HTConstants.USER_AUTH_TOKEN, response.getToken());
                        HTConstants.setPublishableApiKey(response.getToken());
                        editor.commit();
                        startActivity(new Intent(Verify.this, Profile.class));

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mProgressDialog.dismiss();
                        Log.d("Response", "Inside OnError");
                    }
                }
        );
        /*
        JsonObjectRequest  request = new JsonObjectRequest(
                1,
                url,
                jsonObjectBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mProgressDialog.dismiss();
                        Log.d("response", response.toString());
                        Toast.makeText(Verify.this, "Registered", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Verify.this, Profile.class));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mProgressDialog.dismiss();
                        Toast.makeText(Verify.this, "Inside OnError", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        */

        MetaApplication.getInstance().addToRequestQueue(request);

    }

}
