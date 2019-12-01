package com.example.inspirationrewards;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    TextView profile_name, profile_username, profile_location, profile_pointsaward, profile_department, profile_position, profile_pointstoaward, your_story, profile_rewardsHistory;
    ImageView profile_image;
    RecyclerView recyclerView;
    ProgressBar progressBar;

    CommentsAdapter mAdapter;
    String login_api_response;
    String first_name, last_name, username, department, story, position, location, imageBytes, passwrd;
    int points_toAward;
    List<Rewards> rewardsList;

    private static final String TAG = "ProfileActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rewardsList = new ArrayList<>();

        profile_name = findViewById(R.id.profile_name);
        profile_username = findViewById(R.id.profile_username);
        profile_location = findViewById(R.id.profile_location);
        profile_pointsaward = findViewById(R.id.profile_pointsaward);
        profile_department = findViewById(R.id.profile_department);
        profile_position = findViewById(R.id.profile_position);
        profile_pointstoaward = findViewById(R.id.profile_pointstoaward);
        your_story = findViewById(R.id.your_story);
        profile_rewardsHistory = findViewById(R.id.profile_rewardsHistory);
        profile_image = findViewById(R.id.profile_image);

        recyclerView = findViewById(R.id.recycler_profile_rewardsHistory);

        progressBar = findViewById(R.id.profile_progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);

        if (getIntent().hasExtra("response_data")) {
            Log.d(TAG, "onCreate: got response data");
            login_api_response = getIntent().getStringExtra("response_data");
            parseJSONData(login_api_response);
        } else if (getIntent().hasExtra("username") && getIntent().hasExtra("password")) {
            String username = getIntent().getStringExtra("username");
            String password = getIntent().getStringExtra("password");
            new LoginAPIAsyncTask(ProfileActivity.this).execute(username, password);
        } else if (preferences != null) {
            String temp_user = preferences.getString("username", "");
            String pass = preferences.getString("password", "");
            new LoginAPIAsyncTask(ProfileActivity.this).execute(temp_user, pass);
        } else {
            somethingWentWrong();
        }

        saveData(username, passwrd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setHasFixedSize(true);
        mAdapter = new CommentsAdapter(rewardsList);
        recyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editprofile: {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("password", passwrd);
                Log.d(TAG, "onOptionsItemSelected: " + username + passwrd);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            break;
            case R.id.leaderboard: {
                Intent intent = new Intent(ProfileActivity.this, LeaderboardActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("password", passwrd);
                intent.putExtra("pointsToAward", points_toAward);
                intent.putExtra("firstName",first_name);
                intent.putExtra("lastName",last_name);
                Log.d(TAG, "onOptionsItemSelected: " + username + passwrd);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveData(String username, String pass) {
        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().apply();
        editor.putString("username", username);
        editor.putString("password", pass);
        editor.apply();
    }

    private int getPoints() {
        int sum = 0;
        for (Rewards content : rewardsList) {
            if (content != null && rewardsList.size() != 0) {
                sum += content.getValue();
            }
        }
        return sum;
    }

    private Bitmap getDecodedProfileBitmap(String imageBytes) {
        byte[] decodedString = Base64.decode(imageBytes, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    private void setData() {
        String names = last_name + ", " + first_name;
        profile_name.setText(names);
        String temp_username = "(" + username + ")";
        profile_username.setText(temp_username);
        profile_location.setText(location);
        profile_pointsaward.setText(Integer.toString(getPoints()));
        profile_department.setText(department);
        profile_position.setText(position);
        profile_pointstoaward.setText(Integer.toString(points_toAward));
        your_story.setText(story);
        profile_rewardsHistory.setText("Reward History(" + mAdapter.getItemCount() + ")");
        profile_image.setImageBitmap(getDecodedProfileBitmap(imageBytes));
    }

    public void sendResult(String result, String response) {
        CustomToast customToast = new CustomToast(ProfileActivity.this);
        login_api_response = response;
        String status = "";
        String message = "";
        if (result.toLowerCase().contains("failed")) {
            try {
                JSONObject json = new JSONObject(login_api_response);
                String s = json.getString("errordetails");
                JSONObject jsonObject = new JSONObject(s);
                status = jsonObject.getString("status");
                message = jsonObject.getString("message");
                if (message.toLowerCase().contains("validation")) {
                    somethingWentWrong();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            customToast.showCustomToast(status + " " + message, Color.RED);
        } else {
//            customToast.showCustomToast("Process: " + result, Color.GREEN);
            Log.d(TAG, "sendResult: " + login_api_response);
            logInSuccessful();
        }
    }

    private void somethingWentWrong() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setIcon(R.drawable.icon)
                .setMessage("Something Went Wrong")
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void logInSuccessful() {
        parseJSONData(login_api_response);
    }

    private void parseJSONData(String login_api_response) {
        try {
            JSONObject jsonObject = new JSONObject(login_api_response);
            first_name = jsonObject.getString("firstName");
            last_name = jsonObject.getString("lastName");
            username = jsonObject.getString("username");
            department = jsonObject.getString("department");
            story = jsonObject.getString("story");
            position = jsonObject.getString("position");
            points_toAward = jsonObject.getInt("pointsToAward");
            imageBytes = jsonObject.getString("imageBytes");
            location = jsonObject.getString("location");
            passwrd = jsonObject.getString("password");
            JSONArray jsonArray = jsonObject.getJSONArray("rewards");
            for (int i = 0; i < jsonArray.length(); i++) {
                Rewards rewards = new Rewards();
                JSONObject object = jsonArray.getJSONObject(i);
                rewards.setUsername(object.getString("username"));
                rewards.setName(object.getString("name"));
                rewards.setDate(object.getString("date"));
                rewards.setNotes(object.getString("notes"));
                rewards.setValue(object.getInt("value"));
                rewardsList.add(rewards);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mAdapter = new CommentsAdapter(rewardsList);
        recyclerView.setAdapter(mAdapter);
        setData();
    }



}
