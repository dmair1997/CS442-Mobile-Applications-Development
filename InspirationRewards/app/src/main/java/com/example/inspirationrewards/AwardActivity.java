package com.example.inspirationrewards;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

public class AwardActivity extends AppCompatActivity {

    TextView award_name, award_pointsReceived, award_department, award_position, award_story, char_num;
    EditText pointsToSend, comments;
    ImageView award_profilePicture;

    AllProfiles data;
    String currentUser, passwrd;
    String currentUser_FirstName;
    String currentUser_LastName;
    String name;
    int pointsToAward;
    private static final int MAX_CHARS = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_award);

        Toolbar toolbar = findViewById(R.id.toolbar5);
        setSupportActionBar(toolbar);

        if (getIntent().hasExtra("profile_data")) {
            data = (AllProfiles) getIntent().getSerializableExtra("profile_data");
        }
        if (getIntent().hasExtra("username") && getIntent().hasExtra("password")) {
            currentUser = getIntent().getStringExtra("username");
            passwrd = getIntent().getStringExtra("password");
        }
        if(getIntent().hasExtra("pointsToAward")){
            pointsToAward = getIntent().getIntExtra("pointsToAward",0);
        }

        if(getIntent().hasExtra("firstName") && getIntent().hasExtra("lastName")){
            currentUser_FirstName = getIntent().getStringExtra("firstName");
            currentUser_LastName = getIntent().getStringExtra("lastName");
        }
        name = data.getFirstName() + "," + data.getLastName();
        getSupportActionBar().setTitle(name);

        award_name = findViewById(R.id.award_names);
        award_pointsReceived = findViewById(R.id.award_pointsawarded);
        award_department = findViewById(R.id.award_departments);
        award_position = findViewById(R.id.award_positions);
        award_story = findViewById(R.id.award_stories);
        char_num = findViewById(R.id.comment_char_award);

        award_profilePicture = findViewById(R.id.award_profilepicture);

        pointsToSend = findViewById(R.id.award_points_Sent);
        comments = findViewById(R.id.award_comment);
        comments.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_CHARS)});
        comments.addTextChangedListener(new TextWatcher() {
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

        award_profilePicture.setImageBitmap(getBitmapImage(data.getImageByteEncoded()));
        String s = data.getLastName() + " ," + data.getFirstName();
        award_name.setText(s);
        award_pointsReceived.setText(Integer.toString(data.pointsToAward));
        award_department.setText(data.getDepartment());
        award_position.setText(data.getPosition());
        award_story.setText(data.getStory());


    }

    private Bitmap getBitmapImage(String imageByteEncoded) {
        byte[] decodedString = Base64.decode(imageByteEncoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.award_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.award_save:
                if (checkField()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AwardActivity.this);
                    name = name.replace(",", " ");
                    builder.setTitle("Add Reward Points?")
                            .setIcon(R.drawable.icon)
                            .setMessage("Add rewards for " + name)
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new RewardsAPIAsyncTask(
                                            AwardActivity.this,
                                            data,
                                            Integer.parseInt(pointsToSend.getText().toString()),
                                            comments.getText().toString()).execute(currentUser, passwrd,currentUser_FirstName,currentUser_LastName);
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkField() {
        return arePointsEntered() && isCommentAvailable();
    }

    private boolean isCommentAvailable() {
        if (comments.getText().toString().isEmpty()) {
            new CustomToast(AwardActivity.this).showCustomToast("Please enter some comment", Color.RED);
            comments.setError("Enter Some Comment");
            return false;
        }
        return true;
    }

    private boolean arePointsEntered() {
        if (pointsToSend.getText().toString().isEmpty()) {
            pointsToSend.setError("Value cannot be empty");
            return false;
        } else if (Integer.parseInt(pointsToSend.getText().toString()) < 0
                || Integer.parseInt(pointsToSend.getText().toString()) > pointsToAward) {
            pointsToSend.setError("Enter values according to range");
            new CustomToast(AwardActivity.this).showCustomToast("Points have to be between 0 and "
                    + pointsToAward, Color.RED);
            return false;
        } else {
            return true;
        }

    }

    public void sendResult(String result, String response) {
        CustomToast customToast = new CustomToast(AwardActivity.this);
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
            customToast.showCustomToast(response, Color.GREEN);
//            getProfile_api_response = response;
            rewardsSent();
        }
    }

    private void rewardsSent() {
        startActivity(new Intent(AwardActivity.this, LeaderboardActivity.class)
                .putExtra("username", currentUser)
                .putExtra("password", passwrd)
                .putExtra("pointsToAward",pointsToAward));
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AwardActivity.this,LeaderboardActivity.class)
                .putExtra("username",currentUser)
                .putExtra("password",passwrd));
        finish();
    }
}
