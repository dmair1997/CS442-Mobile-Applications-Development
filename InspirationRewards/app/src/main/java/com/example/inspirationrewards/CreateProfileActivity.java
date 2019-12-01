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
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateProfileActivity extends AppCompatActivity {
    ImageView profilePicture;
    EditText username, create_password, first_name, last_name, department, position, story;
    CheckBox admin_or_not;
    TextView tex;
    ProgressBar progressBar;

    String api_response = "";


    private LocationManager locationManager;
    Location currentLocation;
    private Criteria criteria;
    public static int MAX_CHARS = 360;
    private static final String TAG = "CreateProfileActivity";
    //private int OPEN_CAMERA_GALLERY = 1;
    //private int OPEN_CAMERA_CAPTURE = 2;
    private int REQUEST_IMAGE_GALLERY = 1;
    private int REQUEST_IMAGE_CAPTURE = 2;
    private final static int ALL_PERMISSIONS = 100;
    private File currentImageFile;
    Bitmap bitmap;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Toolbar toolbar2 = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (check_Permissions()) {
            Log.i(TAG, "onCreate: REQUESTS_OK");

        }

        profilePicture = findViewById(R.id.create_profile_picture);
        username = findViewById(R.id.create_username);
        create_password = findViewById(R.id.create_password);
        admin_or_not = findViewById(R.id.admin_create_profile);
        first_name = findViewById(R.id.firstName_create_profile);
        last_name = findViewById(R.id.lastName_create_profile);
        department = findViewById(R.id.department_create_profile);
        position = findViewById(R.id.position_create_profile);
        story = findViewById(R.id.editStory_create_profile);
        tex = findViewById(R.id.story_chars_edit_profile);
        progressBar = findViewById(R.id.progressBar_create_profile);

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsDialog();
            }
        });

        story.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_CHARS)});
        story.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String char_text = "(" + s.toString().length() + " of " + MAX_CHARS + ")";
                tex.setText(char_text);
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
        setLocation();
        Log.e(TAG, "onCreate: " + getlocay(currentLocation));
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_profile:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.logo);
                builder.setTitle("Save Changes?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveData();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

        }
        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        Log.d(TAG, "saveData:saved data ");
        if (checkFields()) {
            new CreateProfileAsyncTask(this).execute(
                    username.getText().toString(),
                    create_password.getText().toString(),
                    admin_or_not.isChecked() ? "1" : "0",
                    first_name.getText().toString(),
                    last_name.getText().toString(),
                    department.getText().toString(),
                    position.getText().toString(),
                    story.getText().toString(),
                    getlocay(currentLocation),
                    getEncodedImage(bitmap)
            );
        } else {
            new CustomToast(CreateProfileActivity.this).showCustomToast("Something Went Wrong", Color.RED);
        }
    }

    private boolean checkFields() {
        return isUserNameValid() && isPasswordValid()
                && isFirstNameValid() && isLastNameValid()
                && isDepartmentValid() && isPositionValid()
                && isStoryValid();
    }

    private boolean isStoryValid() {
        if (story.getText().toString().isEmpty() || story.getText().toString().length() > 360) {
            story.setError("Please Enter Story");
            return false;
        } else {
            return true;
        }
    }

    private boolean isPositionValid() {
        if (position.getText().toString().isEmpty()) {
            position.setError("Please Enter Position");
            return false;
        } else {
            return true;
        }
    }

    private boolean isDepartmentValid() {
        if (department.getText().toString().isEmpty()) {
            department.setError("Please Enter Department Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean isLastNameValid() {
        if (last_name.getText().toString().isEmpty()) {
            last_name.setError("Please Enter Last Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean isFirstNameValid() {
        if (first_name.getText().toString().isEmpty()) {
            first_name.setError("Please Enter First Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean isPasswordValid() {
        if (create_password.getText().toString().isEmpty()) {
            create_password.setError("Please Enter Valid Password");
            return false;
        } else {
            return true;
        }
    }

    private boolean isUserNameValid() {
        if (username.getText().toString().isEmpty()) {
            username.setError("Please Enter Valid UserName");
            return false;
        } else {
            return true;
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

    private String getEncodedImage(Bitmap bitmap) {
        if (bitmap == null) {
            return "";
        }
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArray);
        byte[] imageBytes = byteArray.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
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
        /*bitmap = BitmapFactory.decodeFile(imageFileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,70,byteArrayOutputStream);
        Bitmap final_image = BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        profilePicture.setImageBitmap(final_image);*/

        Uri selectedImage = Uri.fromFile(currentImageFile);
        profilePicture.setImageURI(selectedImage);
        bitmap = ((BitmapDrawable) profilePicture.getDrawable()).getBitmap();

    }

    private void successfullogin(){
        try {
            startActivity(new Intent(CreateProfileActivity.this, ProfileActivity.class).putExtra("response_data", api_response));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendResult(String result, String response) {
        Log.d(TAG, "sendResult: " + response);
        api_response = response;
        String status = "";
        String message = "";
        CustomToast customToast = new CustomToast(CreateProfileActivity.this);
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
            successfullogin();
            //finish();
        }
    }
}
