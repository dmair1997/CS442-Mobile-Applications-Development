package com.example.newsgateway;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class NewsService extends Service {

    private static final String TAG = "NewsService";
    boolean running = true;

    ServiceReceiver serviceReceiver;
    ArrayList<Article> articles = new ArrayList<Article>();

    public NewsService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceReceiver = new ServiceReceiver();
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_MSG_TO_SERVICE);
        registerReceiver(serviceReceiver, filter);

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (running) {
                    while(articles.isEmpty()){
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Intent intent = new Intent();
                    intent.setAction(MainActivity.ACTION_NEWS_STORY);
                    intent.putExtra(MainActivity.ARTICLE_LIST, articles);
                    sendBroadcast(intent);
                    articles.clear();
                }
                Log.i(TAG, "NewsService was properly stopped");
            }
        }).start();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
    }

    public void setArticles(ArrayList<Article> list){
        articles.clear();
        articles.addAll(list);
    }

    class ServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case MainActivity.ACTION_MSG_TO_SERVICE:
                    String sourceId="";
                    String temp="";
                    if (intent.hasExtra(MainActivity.SOURCE_ID)) {
                        sourceId = intent.getStringExtra(MainActivity.SOURCE_ID);
                        temp=sourceId.replaceAll(" ","-");
                    }
                    new ArticleAPIAsyncTask(NewsService.this, temp).execute();
                    break;
            }
        }
    }
}
