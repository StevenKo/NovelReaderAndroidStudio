package com.novel.reader.adapter;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.novel.reader.NovelIntroduceActivity;
import com.novel.reader.R;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.GameAPP;
import com.novel.reader.entity.Novel;
import com.novel.reader.util.NovelReaderUtil;
import com.taiwan.imageload.ImageLoader;

public class GridViewAppAdapter extends BaseAdapter {

    private Activity         activity;
    private ArrayList<GameAPP> data = new ArrayList<GameAPP>();
    private static LayoutInflater  inflater = null;
    public ImageLoader             imageLoader;

    public GridViewAppAdapter(Activity a, ArrayList<GameAPP> apps) {
        activity = a;
        
        TelephonyManager telephonyManager = (TelephonyManager)a.getSystemService(Context.TELEPHONY_SERVICE);
    	String device_id = telephonyManager.getDeviceId();
    	if (device_id != null)
    		data = apps;
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
    	return getAppGridView(position,convertView,parent, (GameAPP)data.get(position));
    }
    
    private View getAppGridView(final int position, View convertView, ViewGroup parent,final GameAPP app){
    	View vi = convertView;
    	Display display = activity.getWindowManager().getDefaultDisplay();
        int width = display.getWidth(); // deprecated
        int height = display.getHeight(); // deprecated

        if (width > 480) {
            vi = inflater.inflate(R.layout.item_app, null);
        } else {
            vi = inflater.inflate(R.layout.item_app_small, null);
        }
        
        vi.setClickable(true);
        vi.setFocusable(true);
        vi.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	showRecommendAppDialog(app);
            }

        });
        ImageView image = (ImageView) vi.findViewById(R.id.grid_item_image);
        TextView textName = (TextView) vi.findViewById(R.id.grid_item_name);
        TextView description = (TextView) vi.findViewById(R.id.description);
        
        image.setImageResource(R.drawable.andromoney_icon);
        
        textName.setText(NovelReaderUtil.translateTextIfCN(activity,(app.title)));
        if (app.title.length() > 6)
            textName.setTextSize(12);
        description.setText("推薦優質APP");
        if (app.description.length() > 6)
        	description.setTextSize(12);
       
        
    	return vi;
    }
    
    protected void showRecommendAppDialog(final GameAPP app) {
    	
    	LayoutInflater inflater = activity.getLayoutInflater();
    	LinearLayout recomendLayout = (LinearLayout) inflater.inflate(R.layout.dialog_recommend_app,null);

    	ImageView image = (ImageView) recomendLayout.findViewById(R.id.grid_item_image);
        TextView textName = (TextView) recomendLayout.findViewById(R.id.grid_item_name);
        TextView description = (TextView) recomendLayout.findViewById(R.id.description);
    	
        
        image.setImageResource(R.drawable.andromoney_icon);
        
        textName.setText(NovelReaderUtil.translateTextIfCN(activity,(app.title)));
        if (app.title.length() > 6)
            textName.setTextSize(12);
        description.setText(NovelReaderUtil.translateTextIfCN(activity,(app.description)));
        if (app.description.length() > 6)
        	description.setTextSize(12);
    	
    	Builder a = new Builder(activity).setTitle("推薦優質APP")
        .setPositiveButton("前往下載", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
	            	Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(app.appStoreUrl));
	            	activity.startActivity(browseIntent);
	            	new AsyncTask() {
						@Override
						protected Object doInBackground(Object... arg0) {
							NovelAPI.sendClickInfo(activity, app.appid);
							return null;
					}}.execute();
            	
            }
        });
    	a.setView(recomendLayout);
    	AlertDialog dialog = a.create();
    	dialog.show();
		
	}

	private View getNovelGridView(final int position, View convertView, ViewGroup parent,final Novel novel){
    	View vi = convertView;
        // if (convertView == null)
        // vi = inflater.inflate(R.layout.item_gridview_novel, null);

        Display display = activity.getWindowManager().getDefaultDisplay();
        int width = display.getWidth(); // deprecated
        int height = display.getHeight(); // deprecated

        if (width > 480) {
            vi = inflater.inflate(R.layout.item_gridview_novel, null);
        } else {
            vi = inflater.inflate(R.layout.item_gridview_novel_small, null);
        }

        vi.setClickable(true);
        vi.setFocusable(true);
        // vi.setBackgroundResource(android.R.drawable.menuitem_background);
        vi.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Toast.makeText(activity, "tt", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity, NovelIntroduceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("NovelId", novel.getId());
                bundle.putString("NovelName", novel.getName());
                bundle.putString("NovelAuthor", novel.getAuthor());
                bundle.putString("NovelDescription", novel.getDescription());
                bundle.putString("NovelUpdate", novel.getLastUpdate());
                bundle.putString("NovelPicUrl", novel.getPic());
                bundle.putString("NovelArticleNum", novel.getArticleNum());
                intent.putExtras(bundle);
                activity.startActivity(intent);

            }

        });

        TextView textName = (TextView) vi.findViewById(R.id.grid_item_name);
        ImageView image = (ImageView) vi.findViewById(R.id.grid_item_image);
        TextView textAuthor = (TextView) vi.findViewById(R.id.grid_item_author);
        TextView textCounts = (TextView) vi.findViewById(R.id.grid_item_counts);
        TextView textFinish = (TextView) vi.findViewById(R.id.grid_item_finish);
        TextView textSerialize = (TextView) vi.findViewById(R.id.serializing);

        textName.setText(NovelReaderUtil.translateTextIfCN(activity,(novel.getName())));
        if (novel.getName().length() > 6)
            textName.setTextSize(12);
        textAuthor.setText(NovelReaderUtil.translateTextIfCN(activity,novel.getAuthor()));
        if (novel.getAuthor().length() > 14) {
            textAuthor.setTextSize(8);
        }
        textCounts.setText(novel.getArticleNum());
        textFinish.setText(novel.getLastUpdate());

        if (NovelReaderUtil.isDisplayDefaultBookCover(novel.getPic())) {
            image.setImageResource(R.drawable.bookcover_default);
        } else {
            imageLoader.DisplayImage(novel.getPic(), image);
        }

        if (novel.isSerializing()) {
        	textSerialize.setText(activity.getResources().getString(R.string.serializing));
        } else {
            textSerialize.setText("全本");
        }

        return vi;
    }
}
