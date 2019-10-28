package com.example.stockwatch;

import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class AsyncData extends AsyncTask<ArrayList<Stock>, Void, ArrayList<Stock>> {


    MainActivity mainActivity;
    private ArrayList<Stock> stocksList;
    private final String APIKEY = "sk_9af1a976128249baa0d29b0f5dffa960";

    public AsyncData ( MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }


    @Override
    protected ArrayList<Stock> doInBackground(ArrayList<Stock>... arrayLists) {
        String json = "";

        try {
            for (int i = 0; i < arrayLists[0].size(); i++) {
                URL url = new URL("https://cloud.iexapis.com/stable/stock/" + arrayLists[0].get(i).getSymbol() + "/quote?token=" + APIKEY);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
                String line;
                while ((line = reader.readLine()) != null) {
                    json = line;
                }
                try {
                    JSONObject jStock = new JSONObject(json);
                    double price;
                    double change;
                    double percentage;

                    price = jStock.getDouble("latestPrice");
                    change = jStock.getDouble("change");
                    percentage = jStock.getDouble("changePercent");

                    arrayLists[0].get(i).setPrice(price);
                    arrayLists[0].get(i).setChange(change);
                    arrayLists[0].get(i).setPercentage(percentage);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            return null;
        }
        return arrayLists[0];

    }


    @Override
    protected void onPostExecute(ArrayList<Stock> stocks) {
        mainActivity.onResume();
        super.onPostExecute(stocks);
    }
}
