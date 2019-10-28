package com.example.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AsyncStockLoader extends AsyncTask<String, String, String> {

    private MainActivity mainActivity;


    public AsyncStockLoader(MainActivity ma) {
        mainActivity = ma;
    }

    @Override
    protected void onPreExecute() {
        //Toast.makeText(mainActivity, "Loading Country Data...", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onPostExecute(String s) {
        ArrayList<Stock> stockList = parseJSON(s);
        if (stockList != null)
            //Toast.makeText(mainActivity, "Loaded " + stockList.size() + " countries.", Toast.LENGTH_SHORT).show();
        mainActivity.updateData(stockList);
    }


    @Override
    protected String doInBackground(String... params) {

        Uri dataUri = Uri.parse(params[0]);
        String urlToUse = dataUri.toString();


        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }



        } catch (Exception e) {

            return null;
        }


        return sb.toString();
    }


    private ArrayList<Stock> parseJSON(String s) {

        ArrayList<Stock> stockList = new ArrayList<>();
        try {
            JSONArray jArray = new JSONArray(s);

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jstock = (JSONObject) jArray.get(i);
                String symbol = jstock.getString("symbol");
                String name = jstock.getString("name");
                double price = 0.0;
                double change = 0.0;
                double percentage = 0.0;
                String id= jstock.getString("iexId");

                stockList.add(
                        new Stock(name, symbol, price, change, percentage, id));

            }
            //Toast.makeText(mainActivity, "Returns here before null", Toast.LENGTH_SHORT).show();
            return stockList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(mainActivity, "Returns here", Toast.LENGTH_SHORT).show();
        return null;
    }


}
