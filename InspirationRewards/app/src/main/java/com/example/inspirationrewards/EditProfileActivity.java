package com.example.inspirationrewards;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    ImageView profilePicture;
    EditText editprofile_username, editprofile_password, editprofile_firstName, editprofile_lastName, editprofile_department, editprofile_position, editprofile_story;
    CheckBox admin_or_not;
    TextView char_num;
    ProgressBar progressBar;
    private LocationManager locationManager;
    private Criteria criteria;
    private static final int MAX_CHARS = 360;
    private int REQUEST_IMAGE_GALLERY = 1;
    private int REQUEST_IMAGE_CAPTURE = 2;
    private final static int ALL_PERMISSIONS = 100;
    private static final String TAG = "EditProfileActivity";
    Location currentLocation;

    String currentUser, password;
    Bitmap profileImageBitmap;
    String temp_user, temp_pass;
    String imageFileName;
    boolean admin;
    String first_name, last_name, username, department, position, story, imageBytes, location;
    int pointsToAward;
    Bitmap bitmap;
    private String api_response;
    List<Rewards> rewardsContentList;
    private File currentImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Toolbar toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        rewardsContentList = new ArrayList<>();

        if (check_Permissions()) {
            Log.i(TAG, "onCreate: REQUESTS_OK");

        }
        if (getIntent().hasExtra("username") && getIntent().hasExtra("password")) {
            currentUser = getIntent().getStringExtra("username");
            password = getIntent().getStringExtra("password");
        } else if (preferences != null) {
            temp_user = preferences.getString("username", "");
            temp_pass = preferences.getString("password", "");
            if (temp_user != null && temp_pass != null) {
                currentUser = temp_user;
                password = temp_pass;
            }
        } else {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setTitle("Error")
                    .setIcon(R.drawable.icon)
                    .setMessage("Something Went Wrong")
                    .setCancelable(false)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                        }
                    });
            AlertDialog alertDialog = builder1.create();
            alertDialog.show();
        }

        profilePicture = findViewById(R.id.profile_image_edit);
        editprofile_username = findViewById(R.id.editprofile_username);
        editprofile_password = findViewById(R.id.editprofile_password);
        admin_or_not = findViewById(R.id.editprofile_admincheck);
        editprofile_firstName = findViewById(R.id.editprofile_firstname);
        editprofile_lastName = findViewById(R.id.editprofile_lastname);
        editprofile_department = findViewById(R.id.editprofile_department);
        editprofile_position = findViewById(R.id.editprofile_position);
        editprofile_story = findViewById(R.id.editprofile_story);
        char_num = findViewById(R.id.story_chars_edit_profile);
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.INVISIBLE);

        editprofile_username.setClickable(false);
        editprofile_story.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_CHARS)});

        editprofile_story.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String char_text = "(" + s.toString().length() + " of " + MAX_CHARS + ")";
                char_num.setText(char_text);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);

        if (currentLocation == null) {
            setLocation();
        }

        new LoginAPIAsyncTask(EditProfileActivity.this, false).execute(currentUser, password);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsDialog();
            }
        });

        /*final View arrowLogo = toolbar.getChildAt(0);
        arrowLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class)
                        .putExtra("username",currentUser)
                        .putExtra("password",password));
                Log.d(TAG, "logo onClick: " + currentUser + password);
                finish();
            }
        });*/

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_save:

                saveData();
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean check_Permissions() {
        int Camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int WriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int ReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int GPS_FINE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int GPS_COARSE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int INTERNET = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        List<String> permissionRequired = new ArrayList<>();
        if (Camera != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.CAMERA);
        }
        if (WriteStorage != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ReadStorage != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (GPS_FINE != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (GPS_COARSE != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (INTERNET != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.INTERNET);
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
                    Log.d(TAG, "onRequestPermissionsResult: Permissions Granted");
                    setLocation();
                }
            }
        }
    }
    private void optionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.icon);
        builder.setTitle("Profile Picture");
        builder.setMessage("Take Picture From:");
        builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fromCamera();
            }
        }).setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fromGallery();
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void fromCamera() {
        currentImageFile = new File(getExternalCacheDir(), "appimage_" + System.currentTimeMillis() + ".jpg");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentImageFile));
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_temp";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        //this.imageFileName = image.getAbsolutePath();
        return image;
    }

    private void fromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
    }

    private void saveData() {
        Log.d(TAG, "saveData:saved data ");
        if (checkFields()) {
            new UpdateProfileAPIAsyncTask(this, rewardsContentList).execute(
                    editprofile_username.getText().toString(),
                    editprofile_password.getText().toString(),
                    admin_or_not.isChecked() ? "1" : "0",
                    editprofile_firstName.getText().toString(),
                    editprofile_lastName.getText().toString(),
                    editprofile_department.getText().toString(),
                    editprofile_position.getText().toString(),
                    editprofile_story.getText().toString(),
                    getlocay(currentLocation),
                    getEncodedImage(bitmap)
            );
        } else {
            new CustomToast(EditProfileActivity.this).showCustomToast("Something Went Wrong", Color.RED);
        }
    }

    private String getEncodedImage(Bitmap bitmap) {
        if (bitmap == null) {
            return "";
        }
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArray);
        byte[] imageBytes = byteArray.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private boolean isStoryValid() {
        if (editprofile_story.getText().toString().isEmpty() || editprofile_story.getText().toString().length() > 360) {
            editprofile_story.setError("Please Enter Story");
            return false;
        } else {
            return true;
        }
    }

    private boolean isPositionValid() {
        if (editprofile_position.getText().toString().isEmpty()) {
            editprofile_position.setError("Please Enter Position");
            return false;
        } else {
            return true;
        }
    }

    private boolean isDepartmentValid() {
        if (editprofile_department.getText().toString().isEmpty()) {
            editprofile_department.setError("Please Enter Department Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean isLastNameValid() {
        if (editprofile_lastName.getText().toString().isEmpty()) {
            editprofile_lastName.setError("Please Enter Last Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean isFirstNameValid() {
        if (editprofile_firstName.getText().toString().isEmpty()) {
            editprofile_firstName.setError("Please Enter First Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean isPasswordValid() {
        if (editprofile_password.getText().toString().isEmpty()) {
            editprofile_password.setError("Please Enter Valid Password");
            return false;
        } else {
            return true;
        }
    }

    private boolean isUserNameValid() {
        if (editprofile_username.getText().toString().isEmpty()) {
            editprofile_username.setError("Please Enter Valid UserName");
            return false;
        } else {
            return true;
        }
    }

    private boolean checkFields() {
        return isUserNameValid() && isPasswordValid()
                && isFirstNameValid() && isLastNameValid()
                && isDepartmentValid() && isPositionValid()
                && isStoryValid();
    }

    private void galleryData(Intent data) {
        Uri imageUri = data.getData();
        if (imageUri == null) {
            return;
        }
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            profilePicture.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cameraData() {

        Uri selectedImage = Uri.fromFile(currentImageFile);
        profilePicture.setImageURI(selectedImage);
        bitmap = ((BitmapDrawable) profilePicture.getDrawable()).getBitmap();

    }
    public void sendResult(String result, String response) {
        CustomToast customToast = new CustomToast(EditProfileActivity.this);
        api_response = response;
        String status = "";
        String message = "";
        if (result.toLowerCase().contains("failed")) {
            try {
                JSONObject json = new JSONObject(response);
                String s = json.getString("errordetails");
                JSONObject jsonObject = new JSONObject(s);
                status = jsonObject.getString("status");
                message = jsonObject.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            customToast.showCustomToast(status + ": " + message, Color.GREEN);
        } else {
            customToast.showCustomToast("Process: " + result, Color.GREEN);
            logInSuccessful();
        }
    }

    private void logInSuccessful() {
        parseJSONData(api_response);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                try {
                    cameraData();
                } catch (Exception e) {
                    //Toast.makeText(this, "onActivityResult: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
        if (requestCode == REQUEST_IMAGE_GALLERY) {
            if (resultCode == RESULT_OK) {
                try {
                    galleryData(data);
                } catch (Exception e) {
                    //Toast.makeText(this, "onActivityResult: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void setLocation() {
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        currentLocation = locationManager.getLastKnownLocation(bestProvider);
    }

    private String getlocay(Location loc) {
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

    private void parseJSONData(String api_response) {
        try {
            JSONObject jsonObject = new JSONObject(api_response);
            first_name = jsonObject.getString("firstName");
            last_name = jsonObject.getString("lastName");
            username = jsonObject.getString("username");
            department = jsonObject.getString("department");
            story = jsonObject.getString("story");
            admin = jsonObject.getBoolean("admin");
            position = jsonObject.getString("position");
            pointsToAward = jsonObject.getInt("pointsToAward");
            imageBytes = jsonObject.getString("imageBytes");
            location = jsonObject.getString("position");
            password = jsonObject.getString("password");
            JSONArray jsonArray = jsonObject.getJSONArray("rewards");
            for (int i = 0; i < jsonArray.length(); i++) {
                Rewards rewardsContent = new Rewards();
                JSONObject object = jsonArray.getJSONObject(i);
                rewardsContent.setUsername(object.getString("username"));
                rewardsContent.setName(object.getString("name"));                   //full name
                rewardsContent.setDate(object.getString("date"));
                rewardsContent.setNotes(object.getString("notes"));                 // comments
                rewardsContent.setValue(object.getInt("value"));                    // reward value
                rewardsContentList.add(rewardsContent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setCurrentData();
    }

    private void setCurrentData() {
        editprofile_username.setText(username);
        editprofile_firstName.setText(first_name);
        editprofile_lastName.setText(last_name);
        editprofile_password.setText(password);
        editprofile_position.setText(position);
        admin_or_not.setChecked(admin);
        editprofile_department.setText(department);
        editprofile_story.setText(story);
        profilePicture.setImageBitmap(getDecodedProfileBitmap(imageBytes));
    }

    private Bitmap getDecodedProfileBitmap(String imageBytes) {
        byte[] decodedString = Base64.decode(imageBytes, Base64.DEFAULT);
        profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return profileImageBitmap;
    }

    public void sendResultPostUpdate(String result, String response) {
        Log.d(TAG, "sendResultPostUpdate: " + response);
        CustomToast customToast = new CustomToast(EditProfileActivity.this);
        String status = "";
        String message = "";
        if (result.toLowerCase().contains("failed")) {
            try {
                JSONObject json = new JSONObject(response);
                String s = json.getString("errordetails");
                JSONObject jsonObject = new JSONObject(s);
                status = jsonObject.getString("status");
                message = jsonObject.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            customToast.showCustomToast(status + " " + message, Color.RED);
        } else {
            customToast.showCustomToast("Process: " + result, Color.GREEN);
            new LoginAPIAsyncTask(EditProfileActivity.this, true).execute(currentUser, password);

        }
    }

    public void postUpdate() {
        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        intent.putExtra("response_data", api_response);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class)
                .putExtra("username",currentUser)
                .putExtra("password",password));
        Log.d(TAG, "onBackPressed: " + currentUser + password);
        finish();
    }



}
