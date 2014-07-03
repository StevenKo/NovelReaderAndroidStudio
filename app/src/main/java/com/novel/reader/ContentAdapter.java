package com.novel.reader;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.novel.reader.R;
import com.novel.reader.entity.Article;
import com.novel.reader.util.NovelReaderUtil;

public class ContentAdapter extends BaseAdapter {

    private final Activity            activity;
    private final ArrayList<Article> data;
    private static LayoutInflater     inflater = null;
    private int selectId;

    public ContentAdapter(Activity a, ArrayList<Article> d, int selectArticleId) {
        activity = a;
        data = d;
        selectId = selectArticleId;
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
        vi = inflater.inflate(R.layout.item_contents_list, null);
        TextView text = (TextView) vi.findViewById(R.id.text_category_name);
        
        text.setText(NovelReaderUtil.translateTextIfCN(activity,data.get(position).getTitle()));
        if(data.get(position).getId() == selectId)
        	text.setTextColor(Color.RED);

        return vi;
    }
}