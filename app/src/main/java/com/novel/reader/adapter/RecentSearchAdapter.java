package com.novel.reader.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.novel.reader.R;

public class RecentSearchAdapter extends CursorAdapter {

    private TextView text;

    public RecentSearchAdapter(Context context, Cursor cursor) {

        super(context, cursor, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        text = (TextView)view.findViewById(R.id.item);
        text.setText(cursor.getString(1));

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_recent_search, parent, false);

        return view;

    }

}