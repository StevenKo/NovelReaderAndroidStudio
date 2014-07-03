package com.novel.reader.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.novel.reader.R;
import com.novel.reader.entity.Category;
import com.novel.reader.util.NovelReaderUtil;

public class ListAdapter extends BaseAdapter {

    private final Activity            activity;
    private final ArrayList<Category> data;
    private static LayoutInflater     inflater = null;

    public ListAdapter(Activity a, ArrayList<Category> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
            vi = inflater.inflate(R.layout.item_list, null);
        TextView text = (TextView) vi.findViewById(R.id.text_category_name);
        TextView testId = (TextView) vi.findViewById(R.id.text_category_id);
        text.setText(NovelReaderUtil.translateTextIfCN(activity,data.get(position).getCateName()));
        testId.setText(Integer.toString(data.get(position).getId()));

        return vi;
    }
}