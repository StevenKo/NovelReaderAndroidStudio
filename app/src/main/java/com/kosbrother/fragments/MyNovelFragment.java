package com.kosbrother.fragments;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.novel.db.SQLiteNovel;
import com.novel.reader.BookmarkActivity;
import com.novel.reader.CategoryActivity;
import com.novel.reader.ClassicNovelsActivity;
import com.novel.reader.MyNovelActivity;
import com.novel.reader.R;
import com.novel.reader.SettingActivity;
import com.novel.reader.adapter.GridViewAdapter;
import com.novel.reader.adapter.GridViewAppAdapter;
import com.novel.reader.adapter.GridViewIndexBookmarkAdapter;
import com.novel.reader.adapter.GridViewIndexNovelAdapter;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.costum.view.ExpandableHeightGridView;
import com.novel.reader.entity.Bookmark;
import com.novel.reader.entity.GameAPP;
import com.novel.reader.entity.Novel;
import com.novel.reader.util.NovelReaderUtil;
import com.taiwan.imageload.ImageLoader;

public final class MyNovelFragment extends Fragment {

    public static MyNovelFragment newInstance() {
        MyNovelFragment fragment = new MyNovelFragment();
        return fragment;
    }

    private View         myFragmentView;
    private RelativeLayout myBookmarks;
    private LinearLayout recentRead;
    private RelativeLayout myBookcase;
    private LinearLayout mySetting;
    private RelativeLayout classicKongFu;
    private RelativeLayout classicNovel;
    private ExpandableHeightGridView novelGridView;
    private ExpandableHeightGridView bookmarkGridView;
    private GridViewIndexNovelAdapter novelAdapter;
    private GridViewIndexBookmarkAdapter bookmarkAdapter;
	private LinearLayout noNovelInBookcase;
	private LinearLayout noBookmarkInBookmarks;
	private LinearLayout progressLayout;
	private ScrollView scrollView;
	private Activity mActivity;
	public ArrayList<GameAPP> apps;
	private ImageLoader imageLoader;
	
	@Override
	  public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    mActivity= activity;
	  }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.my_novel, container, false);
        findViews();
        return myFragmentView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        getData();
    	setViews();
    	new DownloadAppsTask().execute();
        progressLayout.setVisibility(View.GONE);
        scrollView.scrollTo(0, 0);
    }

    private void getData() {
    	novelAdapter = new GridViewIndexNovelAdapter(mActivity);
    	bookmarkAdapter = new GridViewIndexBookmarkAdapter(mActivity);
    	if(novelAdapter.getCount() > 0){
    		noNovelInBookcase.setVisibility(View.GONE);
    		novelGridView.setVisibility(View.VISIBLE);
    	}else{
    		novelGridView.setVisibility(View.GONE);
    		noNovelInBookcase.setVisibility(View.VISIBLE);
    	}
    	
    	if(bookmarkAdapter.getCount() > 0){
    		noBookmarkInBookmarks.setVisibility(View.GONE);
    		bookmarkGridView.setVisibility(View.VISIBLE);
    	}else{
    		bookmarkGridView.setVisibility(View.GONE);
    		noBookmarkInBookmarks.setVisibility(View.VISIBLE);
    	}
	}

	@Override
    public void onStart() {
        super.onStart();
    }

    private void setViews() {

        myBookmarks.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("IS_RECNET", false);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(mActivity, BookmarkActivity.class);
                startActivity(intent);
            }
        });
        
        noBookmarkInBookmarks.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("IS_RECNET", false);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(mActivity, BookmarkActivity.class);
                startActivity(intent);
            }
        });
        
        recentRead.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("IS_RECNET", true);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(mActivity, BookmarkActivity.class);
                startActivity(intent);
            }
        });

        myBookcase.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, MyNovelActivity.class);
                startActivity(intent);
            }
        });
        
        noNovelInBookcase.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, MyNovelActivity.class);
                startActivity(intent);
            }
        });

        mySetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SettingActivity.class);
                startActivity(intent);
            }
        });

        classicKongFu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, ClassicNovelsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("ClassTitle", "經典武俠");
                bundle.putInt("ClassicId", 0);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        classicNovel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, ClassicNovelsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("ClassTitle", "經典小說");
                bundle.putInt("ClassicId", 1);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        novelGridView.setExpanded(true);
        bookmarkGridView.setExpanded(true);
        novelGridView.setAdapter(novelAdapter);
        bookmarkGridView.setAdapter(bookmarkAdapter);
        
        
    }

    private void findViews() {
        myBookmarks = (RelativeLayout) myFragmentView.findViewById(R.id.my_bookmarks);
        recentRead = (LinearLayout) myFragmentView.findViewById(R.id.my_recent_read_bookmarks);
        myBookcase = (RelativeLayout) myFragmentView.findViewById(R.id.my_bookcase);
        mySetting = (LinearLayout) myFragmentView.findViewById(R.id.my_setting);
        classicKongFu = (RelativeLayout) myFragmentView.findViewById(R.id.classic_kongfu);
        classicNovel = (RelativeLayout) myFragmentView.findViewById(R.id.classic_novel);
        novelGridView = (ExpandableHeightGridView) myFragmentView.findViewById(R.id.my_bookcase_grid);
        bookmarkGridView = (ExpandableHeightGridView) myFragmentView.findViewById(R.id.my_bookmarks_grid);
        noNovelInBookcase = (LinearLayout) myFragmentView.findViewById(R.id.no_novel_in_bookcase);
        noBookmarkInBookmarks = (LinearLayout) myFragmentView.findViewById(R.id.no_bookmark_in_my_bookmarks);
        progressLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_progress);
        scrollView = (ScrollView) myFragmentView.findViewById(R.id.scrollerView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    private class DownloadAppsTask extends AsyncTask {


		@Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Object doInBackground(Object... params) {
            apps = NovelAPI.getAppInfo(mActivity);
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
        	GridView gridApp = (GridView) myFragmentView.findViewById(R.id.app_grid);
        	if (apps != null)
        		gridApp.setAdapter(new GridViewAppAdapter(mActivity, apps));
            super.onPostExecute(result);
        }
    }
    

}
