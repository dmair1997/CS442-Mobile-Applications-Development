package com.example.inspirationrewards;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.MyViewHolder> {

    LeaderboardActivity leaderboardActivity;
    List<AllProfiles> profilesData;
    String currentUser;

    public LeaderboardAdapter(LeaderboardActivity leaderboardActivity, List<AllProfiles> profilesContent, String currentUser) {
        this.leaderboardActivity = leaderboardActivity;
        this.profilesData = profilesContent;
        this.currentUser = currentUser;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LeaderboardAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_recyclerview, parent, false);
        v.setOnClickListener(leaderboardActivity);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardAdapter.MyViewHolder holder, int position) {
        AllProfiles data = profilesData.get(position);
        setData(holder, data, currentUser);
    }

    private void setData(MyViewHolder holder, AllProfiles data, String currentUser) {
        if (data.getUsername().equals(currentUser)) {
            holder.name.setTextColor(Color.MAGENTA);
            holder.rewardPoints.setTextColor(Color.MAGENTA);
            holder.position.setTextColor(Color.MAGENTA);
        }
        String s = data.getLastName() + ", " + data.getFirstName();
        holder.name.setText(s);
        holder.position.setText(data.getPosition());
        holder.profile_Image.setImageBitmap(getImageBitmap(data.getImageByteEncoded()));
        holder.rewardPoints.setText(Integer.toString(getRewardPoints(data.rewardsContents)));
    }

    private int getRewardPoints(List<Rewards> rewardsContents) {
        int sum = 0;
        for (Rewards content : rewardsContents) {
            sum += content.getValue();
        }
        return sum;
    }

    private Bitmap getImageBitmap(String imageByteEncoded) {
        byte[] decodedString = Base64.decode(imageByteEncoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    public int getItemCount() {
        return profilesData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView position;
        public TextView name;
        public TextView rewardPoints;
        public ImageView profile_Image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            position = itemView.findViewById(R.id.leaderboard_position);
            name = itemView.findViewById(R.id.leaderboard_name);
            rewardPoints = itemView.findViewById(R.id.leaderboard_points);
            profile_Image = itemView.findViewById(R.id.leaderboard_profilepicture);


        }
    }
}
