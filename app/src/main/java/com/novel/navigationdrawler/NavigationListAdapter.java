package com.novel.navigationdrawler;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.novel.reader.R.drawable;
import com.novel.reader.R.id;
import com.novel.reader.R.layout;
import com.novel.reader.R.string;

import java.util.ArrayList;

public class NavigationListAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<NavigationItem> data;
    private static LayoutInflater inflater = null;

    public NavigationListAdapter(Activity a) {
        activity = a;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        data = new ArrayList<NavigationItem>();
        data.add(new NavigationItem(activity.getResources().getString(string.my_read_setting), drawable.navigation_setting));
        data.add(new NavigationItem(activity.getResources().getString(string.my_bookmark), drawable.navigation_bookmark));
        data.add(new NavigationItem(activity.getResources().getString(string.my_recent_reading), drawable.navigation_recent));
        data.add(new NavigationItem(activity.getResources().getString(string.my_collect), drawable.navigation_collect));
        data.add(new NavigationItem(activity.getResources().getString(string.my_download), drawable.navigation_download));
        data.add(new NavigationItem(activity.getResources().getString(string.menu_aboutus), drawable.navigation_profile));
        data.add(new NavigationItem(activity.getResources().getString(string.menu_recommend), drawable.navigation_like));
        data.add(new NavigationItem(activity.getResources().getString(string.menu_report), drawable.navigation_voice));
        data.add(new NavigationItem(activity.getResources().getString(string.menu_recommend_novel), drawable.navigation_recommend_novel));
        data.add(new NavigationItem(activity.getResources().getString(string.buy_year_subscription), drawable.navigation_trophy));
        data.add(new NavigationItem(activity.getResources().getString(string.check_update), drawable.navigation_cloud_update));
        data.add(new NavigationItem(activity.getResources().getString(string.official_website), drawable.navigation_official_website));
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(layout.item_drawer_list, null);
        TextView text = (TextView) vi.findViewById(id.text);
        ImageView icon = (ImageView) vi.findViewById(id.imgIcon);
        text.setText(data.get(position).title);
        icon.setImageResource(data.get(position).iconRecourceId);


        return vi;
    }
}