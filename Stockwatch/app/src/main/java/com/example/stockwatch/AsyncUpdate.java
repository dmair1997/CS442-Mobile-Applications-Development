package com.example.stockwatch;

import android.os.AsyncTask;

import java.util.ArrayList;

public class AsyncUpdate extends AsyncTask<ArrayList<Stock>, Integer, ArrayList<Stock>> {

    MainActivity context;

    AsyncUpdate (MainActivity context){
        this.context = context;
    }

    @Override
    protected ArrayList<Stock> doInBackground(ArrayList<Stock>... arrayLists) {
        new AsyncData(context).execute(new ArrayList<Stock>());
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Stock> stocks) {
        super.onPostExecute(stocks);
    }
}
