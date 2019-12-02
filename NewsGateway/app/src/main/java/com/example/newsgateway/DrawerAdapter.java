package com.example.newsgateway;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DrawerAdapter extends BaseAdapter {

    Context context;
    ArrayList<DrawerContents> current_list;

    public DrawerAdapter(Context context, ArrayList<DrawerContents> list){
        this.context = context;
        current_list = list;
    }

    @Override
    public int getCount() {
        return current_list.size();
    }

    @Override
    public Object getItem(int i) {
        return current_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        if (v == null){
            v = (LayoutInflater.from(context).inflate(R.layout.list_element, viewGroup, false));
        }
        DrawerContents drawerContents = current_list.get(i);
        TextView tv = v.findViewById(R.id.tv1);
        tv.setTextColor(drawerContents.getColor());
        tv.setText(drawerContents.getName());

        return v;
    }
}
