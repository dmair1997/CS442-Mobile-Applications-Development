package com.example.newsgateway;

import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ArticleAPIAsyncTask extends AsyncTask <String, Integer, String>{

    String SOURCE = "https://newsapi.org/v2/everything?sources=";
    String API_KEY = "37c1c2b484f44289b805af8eb8f21f7f";
    String POINT = "&apiKey=" + API_KEY;

    String sourceId;
    NewsService service;
    Uri.Builder buildURL = null;
    StringBuilder s;
    boolean noData = false;
    boolean isNoData = true;
    ArrayList<Article> articleArrayList = new ArrayList<Article>();


    public ArticleAPIAsyncTask(NewsService service, String sourceId){
        this.service = service;
        this.sourceId = sourceId;

    }


    @Override
    protected String doInBackground(String... strings) {
        String query = "";

        query = SOURCE + sourceId + POINT;
        buildURL = Uri.parse(query).buildUpon();
        connectToAPI();
        if (!isNoData) {
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
        } catch (FileNotFoundException fe) {
            fe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private void parseJSON(String s) {
        try {
            if (!noData) {
                JSONObject jObjMain = new JSONObject(s);
                JSONArray articles = jObjMain.getJSONArray("articles");
                for (int i = 0; i < articles.length(); i++) {
                    JSONObject art = (JSONObject) articles.get(i);
                    Article artObj = new Article();
                    artObj.setAuthor(art.getString("author"));
                    artObj.setDescription(art.getString("description"));
                    artObj.setPublishedAt(art.getString("publishedAt"));
                    artObj.setTitle(art.getString("title"));
                    artObj.setUrlToImage(art.getString("urlToImage"));
                    artObj.setUrl(art.getString("url"));
                    articleArrayList.add(artObj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        service.setArticles(articleArrayList);
    }
}
