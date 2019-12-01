package com.example.inspirationrewards;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int ALL_PERMISSIONS = 100;
    private static final int LOCATIONS = 101;
    LocationManager locationManager;
    Location currentLocation;
    Criteria criteria;
    Button createProfile;
    Button login;
    EditText username;
    EditText password;
    CheckBox remember_credentials;
    public ProgressBar progressBar;
    String api_response = "";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        username = findViewById(R.id.editusername_main);
        password = findViewById(R.id.editPassword_main);
        login = findViewById(R.id.login_button_main);
        createProfile = findViewById(R.id.create_account_button);
        remember_credentials = findViewById(R.id.checkBoxRemember_main);
        remember_credentials.setChecked(false);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if (checkFields()) {
                    new LoginAPIAsyncTask(MainActivity.this).execute(username.getText().toString(), password.getText().toString());
               }
            }
        });

        createProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateProfileActivity.class));
            }
        });

        if (check_Permissions()) {
            Log.i(TAG, "onCreate: REQUESTS_OK");
        }

        String temp_username;
        String temp_password;


        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        if (preferences != null) {
            temp_username = preferences.getString("username", "");
            temp_password = preferences.getString("password", "");
            if (temp_username != null && temp_password != null) {
                username.setText(temp_username);
                password.setText(temp_password);
                remember_credentials.setChecked(preferences.getBoolean("remember", false));
            }
        }


    }
    private boolean isPassword() {
        if (password.getText().toString().isEmpty()) {
            password.setError("Enter your password");
            return false;
        }
        return true;
    }

    private boolean isUserName() {
        if (username.getText().toString().isEmpty()) {
            username.setError("Enter your Username");
            return false;
        }
        return true;
    }

    private boolean checkFields() {
        if (isUserName() && isPassword()) {
            return true;
        } else {
            return false;
        }
    }



    private void successfulLogin() {
        Log.d(TAG, "logInSuccessful: Start");
        getSharedPreferences("credentials",MODE_PRIVATE).edit().clear().apply();
        if (remember_credentials.isChecked()) {
            SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("username", username.getText().toString());
            editor.putString("password", password.getText().toString());
            editor.putBoolean("remember", true);
            editor.apply();
        }

        try {
           startActivity(new Intent(MainActivity.this, ProfileActivity.class).putExtra("response_data", api_response));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void sendResult(String result, String response) {
        CustomToast customToast = new CustomToast(MainActivity.this);
        api_response = response;
        String status="";String message="";
        Log.d(TAG, "sendResult: " + response);
        if (result.toLowerCase().contains("failed")) {
            try {
                JSONObject json = new JSONObject(api_response);
                String s = json.getString("errordetails");
                JSONObject jsonObject = new JSONObject(s);
                status = jsonObject.getString("status");
                message = jsonObject.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            customToast.showCustomToast(status + " " + message, Color.RED);
        } else {
            customToast.showCustomToast("Login:" + result, Color.GREEN);
            successfulLogin();
        }

    }

    private boolean check_Permissions() {
        int GPS_FINE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int GPS_COARSE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> permissionRequired = new ArrayList<>();
        if (GPS_FINE != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (GPS_COARSE != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!permissionRequired.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionRequired.toArray(new String[permissionRequired.size()]), ALL_PERMISSIONS);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setLocation();
                }
            }
            case LOCATIONS : {
                if (grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    setLocation();
                }
            }
        }
    }

    private void setLocation() {
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATIONS);
        }
        currentLocation = locationManager.getLastKnownLocation(bestProvider);
    }

    private String getPlace(Location loc) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            return city + ", " + state;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
