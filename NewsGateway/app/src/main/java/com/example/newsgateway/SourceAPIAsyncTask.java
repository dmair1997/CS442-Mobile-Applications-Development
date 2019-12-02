package com.example.newsgateway;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SourceAPIAsyncTask extends AsyncTask<String, Integer, String> {

    StringBuilder s;
    boolean noData = false;
    boolean isNoData = true;
    MainActivity ma;
    String category;
    Uri.Builder buildURL = null;
    ArrayList<Source> sources1 = new ArrayList<Source>();
    ArrayList<String> categories = new ArrayList<String>();
    private static final String TAG = "SourceAPIAsyncTask";

    String API_KEY = "37c1c2b484f44289b805af8eb8f21f7f";
    String NewsAPI;

    public SourceAPIAsyncTask (MainActivity main, String category){
        ma = main;
        if (category.equalsIgnoreCase("all") || category.equalsIgnoreCase("")) {
            this.category = "";
            NewsAPI = "https://newsapi.org/v2/sources?language=en&country=us&apiKey=" + API_KEY;
        } else {
            String urls = "https://newsapi.org/v2/sources?language=en&country=us&category=";
            String keys = "&apiKey=" + API_KEY;
            NewsAPI = urls + category + keys;
            this.category = category;
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        buildURL = Uri.parse(NewsAPI).buildUpon();
        connectToAPI();
        if (!isNoData){
            parseJSON(s.toString());
        }
        return null;
    }

    public void connectToAPI() {

        String urlToUse = buildURL.build().toString();
        s = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                noData = true;
            } else {
                conn.setRequestMethod("GET");
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

                String line;
                while ((line = reader.readLine()) != null) {
                    s.append(line).append('\n');
                }
                isNoData = false;

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "FileNotFoundException ");
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void parseJSON(String s) {
        try {
            if (!noData) {
                JSONObject jObjMain = new JSONObject(s);
                JSONArray sources = jObjMain.getJSONArray("sources");
                for (int i = 0; i < sources.length(); i++) {
                    JSONObject src = (JSONObject) sources.get(i);
                    Source srcObj = new Source();
                    srcObj.setId(src.getString("id"));
                    srcObj.setCategory(src.getString("category"));
                    srcObj.setName(src.getString("name"));
                    srcObj.setSourceurl(src.getString("url"));
                    sources1.add(srcObj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        for (int j = 0; j < sources1.size(); j++) {
            String temp = sources1.get(j).getCategory();
            if (!categories.contains(temp))
                categories.add(temp);
        }
        ma.setSources(sources1, categories);
    }



}
