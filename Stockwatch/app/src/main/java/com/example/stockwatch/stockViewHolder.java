package com.example.stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class stockViewHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public TextView symbol;
    public TextView price;
    public  TextView change;
    public  TextView percentage;

    public stockViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.name);
        symbol = itemView.findViewById(R.id.symbol);
        price = itemView.findViewById(R.id.price);
        change = itemView.findViewById(R.id.change);
        percentage = itemView.findViewById(R.id.percentage);
    }

}
