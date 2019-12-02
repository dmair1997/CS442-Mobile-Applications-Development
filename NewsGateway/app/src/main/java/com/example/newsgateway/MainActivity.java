package com.example.newsgateway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    ViewPager viewPager;
    Menu mMenu;
    DrawerAdapter drawerAdapter;
    PagerAdapter pagerAdapter;

    boolean flag;
    boolean service = false;

    static final String ACTION_MSG_TO_SERVICE = "ACTION_MSG_TO_SERVICE";
    static final String ACTION_NEWS_STORY = "ACTION_NEWS_STORY";
    static final String ARTICLE_LIST = "ARTICLE_LIST";
    static final String SOURCE_ID = "SOURCE_ID";


    ArrayList<String> sourceListm = new ArrayList<String>();
    ArrayList<Source> sourceArrayList = new ArrayList<Source>();
    HashMap<String, Source> sourceMap = new HashMap<>();
    ArrayList<DrawerContents> drawerContents = new ArrayList<>();
    ArrayList<String> categoryListm = new ArrayList<String>();
    ArrayList<Article> articleArrayList = new ArrayList<Article>();
    List<Fragment> fragments;

    String newsSelected;


    NewsReceiver newsReceiver;

    int cSourcePointer;

    //private String[] items;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!service && savedInstanceState == null){
            Intent intent = new Intent(MainActivity.this, NewsService.class);
            startService(intent);
            service = true;
        }

        /*items = new String[15];
        for (int i = 0; i < items.length; i++)
            items[i] = "Drawer Item " + (i+1);*/

        newsReceiver = new NewsReceiver();

        IntentFilter filter = new IntentFilter(MainActivity.ACTION_NEWS_STORY);
        registerReceiver(newsReceiver, filter);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);

        drawerAdapter = new DrawerAdapter(this, drawerContents);

        mDrawerList.setAdapter(drawerAdapter);

        mDrawerList.setOnItemClickListener(
                new ListView.OnItemClickListener(){

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        viewPager.setBackgroundResource(0);
                        cSourcePointer = i;
                        selectItem(i);
                    }
                }
        );

        mDrawerToggle = new ActionBarDrawerToggle( this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        fragments = new ArrayList<>();

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);

        if(sourceMap.isEmpty() && savedInstanceState == null){
            new SourceAPIAsyncTask(this, "").execute();
        }


    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m_menu, menu);
        mMenu = menu;
        if (flag) {
            mMenu.add("All");
            for (String s : categoryListm) {
                mMenu.add(s);
            }
        }
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)){
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item);
            return true;
        }

        new SourceAPIAsyncTask(this, item.getTitle().toString()).execute();
        colorMenu(item);
        mDrawerLayout.openDrawer(mDrawerList);
        return super.onOptionsItemSelected(item);
    }

    private void colorMenu(MenuItem item) {
        switch (item.getTitle().toString()) {
            case "business":
                setColor(item, Color.BLUE);
                break;
            case "entertainment":
                setColor(item, Color.GREEN);
                break;
            case "sports":
                setColor(item, Color.YELLOW);
                break;
            case "science":
                setColor(item, Color.MAGENTA);
                break;
            case "technology":
                setColor(item, Color.RED);
                break;
            case "general":
                setColor(item, Color.CYAN);
                break;
            case "health":
                setColor(item, Color.LTGRAY);

        }
    }

    private void setColor(MenuItem item, int color) {
        SpannableString spannableString = new SpannableString(item.getTitle());
        spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(), 0);
        item.setTitle(spannableString);
    }

    private void selectItem(int position){
        newsSelected = sourceListm.get(position);
        Intent intent = new Intent (MainActivity.ACTION_MSG_TO_SERVICE);
        intent.putExtra(SOURCE_ID, newsSelected);
        sendBroadcast(intent);
        mDrawerLayout.closeDrawer(mDrawerList);

    }

    class NewsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_NEWS_STORY:
                    ArrayList<Article> artList;
                    if (intent.hasExtra(ARTICLE_LIST)) {
                        artList = (ArrayList<Article>) intent.getSerializableExtra(ARTICLE_LIST);
                        setFragments(artList);
                    }
                    break;
            }
        }
    }

    public void setSources(ArrayList<Source> sourceList, ArrayList<String> categoryList) {
        sourceMap.clear();
        sourceListm.clear();
        sourceArrayList.clear();
        drawerContents.clear();
        sourceArrayList.addAll(sourceList);

        for (int i = 0; i < sourceList.size(); i++) {
            sourceListm.add(sourceList.get(i).getName());
            sourceMap.put(sourceList.get(i).getName(), sourceList.get(i));
        }

        if (!mMenu.hasVisibleItems()) {
            categoryListm.clear();
            categoryListm = categoryList;
            mMenu.add("All");
            Collections.sort(categoryList);
            for (String s : categoryList)
                mMenu.add(s);
        }

        for (Source s : sourceList) {
            DrawerContents drawerContent = new DrawerContents();
            switch (s.getCategory()) {
                case "business":
                    drawerContent.setColor(Color.BLUE);
                    drawerContent.setName(s.getName());
                    drawerContents.add(drawerContent);
                    break;
                case "entertainment":
                    drawerContent.setColor(Color.GREEN);
                    drawerContent.setName(s.getName());
                    drawerContents.add(drawerContent);
                    break;
                case "sports":
                    drawerContent.setColor(Color.YELLOW);
                    drawerContent.setName(s.getName());
                    drawerContents.add(drawerContent);
                    break;
                case "science":
                    drawerContent.setColor(Color.MAGENTA);
                    drawerContent.setName(s.getName());
                    drawerContents.add(drawerContent);
                    break;
                case "technology":
                    drawerContent.setColor(Color.RED);
                    drawerContent.setName(s.getName());
                    drawerContents.add(drawerContent);
                    break;
                case "general":
                    drawerContent.setColor(Color.CYAN);
                    drawerContent.setName(s.getName());
                    drawerContents.add(drawerContent);
                    break;
                case "health":
                    drawerContent.setColor(Color.LTGRAY);
                    drawerContent.setName(s.getName());
                    drawerContents.add(drawerContent);
            }
        }
        drawerAdapter.notifyDataSetChanged();
        Log.d(TAG, "setSources: ");
    }

    private class PagerAdapter extends FragmentPagerAdapter{

        private long baseid = 0;


        PagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public long getItemId(int position) {
            return baseid + position;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void notifyPositionChange (int position){
            baseid += getCount() + position;
        }


    }


    private void setFragments(ArrayList<Article> articles) {
        setTitle(newsSelected);
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            pagerAdapter.notifyPositionChange(i);
        }
        fragments.clear();
        for (int i = 0; i < articles.size(); i++) {
            Article a = articles.get(i);
            fragments.add(ArticlesFragment.newInstance(a, i, articles.size()));
        }
        pagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(0);
        articleArrayList = articles;
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(newsReceiver);
        Intent intent = new Intent(MainActivity.this, NewsReceiver.class);
        stopService(intent);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        RestoreLayout restoreLayout = new RestoreLayout();
        restoreLayout.setCategories(categoryListm);
        restoreLayout.setSourceList(sourceArrayList);
        restoreLayout.setcArticle(viewPager.getCurrentItem());
        restoreLayout.setcSource(cSourcePointer);
        restoreLayout.setArticleList(articleArrayList);
        outState.putSerializable("state", restoreLayout);
        super.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        RestoreLayout restoreLayout = (RestoreLayout) savedInstanceState.getSerializable("state");
        flag = true;
        articleArrayList = restoreLayout != null ? restoreLayout.getArticleList() : null;
        categoryListm = restoreLayout != null ? restoreLayout.getCategories() : null;
        sourceArrayList = restoreLayout != null ? restoreLayout.getSourceList() : null;
        for (int i = 0; i < (sourceArrayList != null ? sourceArrayList.size() : 0); i++) {
            sourceListm.add(sourceArrayList.get(i).getName());
            sourceMap.put(sourceArrayList.get(i).getName(), sourceArrayList.get(i));
        }
        mDrawerList.clearChoices();
        drawerAdapter.notifyDataSetChanged();
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
                                               @Override
                                               public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                   viewPager.setBackgroundResource(0);
                                                   cSourcePointer = position;
                                                   selectItem(position);

                                               }
                                           }
        );
        setTitle("News Gateway");
    }
}
