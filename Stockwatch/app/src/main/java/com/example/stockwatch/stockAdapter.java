package com.example.stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class stockAdapter extends RecyclerView.Adapter<stockViewHolder> {

    private List<Stock> stockList;
    private MainActivity mainActivity;


    public stockAdapter(List<Stock> empList, MainActivity ma){
        this.stockList = empList;
        mainActivity = ma;
    }

    @NonNull
    @Override
    public stockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_list_view,parent,false);

        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);

        return new stockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull stockViewHolder holder, int position) {
        Stock stock = stockList.get(position);

        holder.name.setText(stock.getName());
        holder.symbol.setText(stock.getSymbol());
        holder.price.setText(String.valueOf(stock.getPrice()));

        if(stock.getChange() > 0){
            holder.change.setText("▲ "+String.valueOf(stock.getChange())+"("+String.valueOf(stock.getPercentage())+"%)");
            holder.name.setTextColor(Color.GREEN);
            holder.price.setTextColor(Color.GREEN);
            holder.symbol.setTextColor(Color.GREEN);
            holder.change.setTextColor(Color.GREEN);
        } else {
            holder.change.setText("▼ "+String.valueOf(stock.getChange())+"("+String.valueOf(stock.getPercentage())+"%)");
            holder.name.setTextColor(Color.RED);
            holder.price.setTextColor(Color.RED);
            holder.symbol.setTextColor(Color.RED);
            holder.change.setTextColor(Color.RED);
        }


    }


    @Override
    public int getItemCount() {
        return stockList.size();
    }

}
