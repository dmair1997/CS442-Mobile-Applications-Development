package com.example.inspirationrewards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyViewHolder>{

    List<Rewards> rewardsContentList;
    public CommentsAdapter(List<Rewards> rewardsContentList) {
        this.rewardsContentList = rewardsContentList;
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reward_recyclerview,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Rewards rewardsContent = rewardsContentList.get(position);
        setReward(holder,rewardsContent);

    }
    private void setReward(MyViewHolder holder, Rewards rewardsContent) {
        holder.first_last_names.setText(rewardsContent.getName());
        holder.date.setText(rewardsContent.getDate());
        holder.points.setText(Integer.toString(rewardsContent.getValue()));
        holder.comments.setText(rewardsContent.getNotes());
    }

    @Override
    public int getItemCount() {
        return rewardsContentList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView date;
        public TextView first_last_names;
        public TextView points;
        public TextView comments;
        public TextView rewardsNumber;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.reward_date);
            first_last_names = itemView.findViewById(R.id.reward_name);
            points = itemView.findViewById(R.id.reward_points);
            comments = itemView.findViewById(R.id.reward_comment);
            rewardsNumber = itemView.findViewById(R.id.profile_rewardsHistory);
        }
    }

}
