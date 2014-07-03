package com.novel.reader.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.novel.db.SQLiteNovel;
import com.novel.reader.MyDownloadArticleActivity;
import com.novel.reader.NovelIntroduceActivity;
import com.novel.reader.R;
import com.novel.reader.entity.Novel;
import com.novel.reader.util.NovelReaderUtil;
import com.taiwan.imageload.ImageLoader;

public class GridViewIndexNovelAdapter extends BaseAdapter {

    private final Activity         activity;
    private final ArrayList<Novel> data;
    private static LayoutInflater  inflater = null;
    public ImageLoader             imageLoader;
	private int collect_nove_size;

    public GridViewIndexNovelAdapter(Activity a) {
        activity = a;
    	SQLiteNovel db = new SQLiteNovel(activity);
    	ArrayList<Novel> novels = db.getLastCollectNovels(3);
    	collect_nove_size = novels.size();
    	novels.addAll(db.getLastDownloadNovels(3));
        data = novels;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext(), 70);

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

    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
       
        vi = inflater.inflate(R.layout.item_gridview_index_novel, null);
        
        vi.setClickable(true);
        vi.setFocusable(true);

        TextView textName = (TextView) vi.findViewById(R.id.grid_item_name);
        ImageView image = (ImageView) vi.findViewById(R.id.grid_item_image);
        TextView textAuthor = (TextView) vi.findViewById(R.id.grid_item_author);
        TextView textCounts = (TextView) vi.findViewById(R.id.grid_item_counts);
        TextView textFinish = (TextView) vi.findViewById(R.id.grid_item_finish);
        TextView textSerialize = (TextView) vi.findViewById(R.id.serializing);

        textName.setText(NovelReaderUtil.translateTextIfCN(activity,data.get(position).getName()));
        textAuthor.setText(NovelReaderUtil.translateTextIfCN(activity,data.get(position).getAuthor()));
        textCounts.setText(data.get(position).getArticleNum());
        textFinish.setText(data.get(position).getLastUpdate());

        if (NovelReaderUtil.isDisplayDefaultBookCover(data.get(position).getPic())) {
            image.setImageResource(R.drawable.bookcover_default);
        } else {
            imageLoader.DisplayImage(data.get(position).getPic(), image);
        }

        if (position < collect_nove_size) {
            textSerialize.setText("收藏小說");
            
            vi.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, NovelIntroduceActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("NovelId", data.get(position).getId());
                    bundle.putString("NovelName", data.get(position).getName());
                    bundle.putString("NovelAuthor", data.get(position).getAuthor());
                    bundle.putString("NovelDescription", data.get(position).getDescription());
                    bundle.putString("NovelUpdate", data.get(position).getLastUpdate());
                    bundle.putString("NovelPicUrl", data.get(position).getPic());
                    bundle.putString("NovelArticleNum", data.get(position).getArticleNum());
                    intent.putExtras(bundle);
                    activity.startActivity(intent);

                }

            });
            
        } else {
            textSerialize.setText("下載小說");
            
            vi.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	 Intent intent = new Intent(activity, MyDownloadArticleActivity.class);
                     Bundle bundle = new Bundle();
                     bundle.putInt("NovelId", data.get(position).getId());
                     bundle.putString("NovelName", data.get(position).getName());
                     bundle.putString("NovelAuthor", data.get(position).getAuthor());
                     bundle.putString("NovelPicUrl", data.get(position).getPic());
                     bundle.putString("NovelArticleNum", data.get(position).getArticleNum());
                     intent.putExtras(bundle);
                     activity.startActivity(intent);

                }

            });
        }

        return vi;
    }
}
