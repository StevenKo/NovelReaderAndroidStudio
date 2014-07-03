package com.kosbrother.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.novel.reader.R;
import com.novel.reader.adapter.GridViewAdapter;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.GameAPP;
import com.novel.reader.entity.Novel;
import com.taiwan.imageload.LoadMoreGridView;

public class IndexNovelFragment extends Fragment {

    private ArrayList<Novel> novels = new ArrayList<Novel>();
    private LoadMoreGridView myGrid;
    private GridViewAdapter  myGridViewAdapter;
    private LinearLayout     progressLayout;
    private LinearLayout     loadmoreLayout;
    private LinearLayout     layoutReload;
    private Button           buttonReload;
    private int       myPage     = 1;
    private Boolean          checkLoad  = true;

    public static final int HOT_NOVEL = 1;
    public static final int MONTH_NOVEL = 2;
    public static final int WEEK_NOVEL = 3;
    public static final int LATEST_NOVEL = 4;
	private int novelFragment = 0;
	 private ArrayList<Novel> moreNovels;
	private Activity mActivity;
	 
	 
	 @Override
	  public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    mActivity= activity;
	  }
    
    public static IndexNovelFragment newInstance(int novelFragment) {

        IndexNovelFragment fragment = new IndexNovelFragment();
        Bundle bdl = new Bundle();
        bdl.putInt("NovelFragment", novelFragment);
        fragment.setArguments(bdl);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        novelFragment = getArguments().getInt("NovelFragment");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myFragmentView = inflater.inflate(R.layout.loadmore_grid, container, false);
        progressLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_progress);
        loadmoreLayout = (LinearLayout) myFragmentView.findViewById(R.id.load_more_grid);
        layoutReload = (LinearLayout) myFragmentView.findViewById(R.id.layout_reload);
        buttonReload = (Button) myFragmentView.findViewById(R.id.button_reload);
        myGrid = (LoadMoreGridView) myFragmentView.findViewById(R.id.news_list);
        myGrid.setOnLoadMoreListener(new LoadMoreGridView.OnLoadMoreListener() {
            public void onLoadMore() {
            	if (checkLoad) {
                    myPage = myPage + 1;
                    loadmoreLayout.setVisibility(View.VISIBLE);
                    new LoadMoreTask().execute();
                } else {
                    myGrid.onLoadMoreComplete();
                }

            }
        });

        buttonReload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                progressLayout.setVisibility(View.VISIBLE);
                layoutReload.setVisibility(View.GONE);
                new LoadMoreTask().execute();
            }
        });

        if (myGridViewAdapter != null) {
            progressLayout.setVisibility(View.GONE);
            loadmoreLayout.setVisibility(View.GONE);
            myGrid.setAdapter(myGridViewAdapter);
        } else {
            new LoadMoreTask().execute();
        }

        return myFragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private class LoadMoreTask extends AsyncTask {

		private ArrayList<GameAPP> apps;

		@Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }

        @Override
        protected Object doInBackground(Object... params) {
        	
        	switch (novelFragment) {
	          case HOT_NOVEL:
	        	  moreNovels = NovelAPI.getHotNovels(myPage);
	        	  break;
	          case MONTH_NOVEL:
	        	  moreNovels = NovelAPI.getThisMonthHotNovels(myPage);
	        	  break;
	          case WEEK_NOVEL:
	        	  moreNovels = NovelAPI.getThisWeekHotNovels(myPage);
	        	  break;
	          case LATEST_NOVEL:
	        	  moreNovels = NovelAPI.getLatestUpdateNovels(myPage);
	        	  break;
	        }
        	if (moreNovels != null && moreNovels.size()!=0) {
                for (int i = 0; i < moreNovels.size(); i++) {
                    novels.add(moreNovels.get(i));
                }
            }
        	apps = NovelAPI.getAppInfo(mActivity);

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            progressLayout.setVisibility(View.GONE);
            loadmoreLayout.setVisibility(View.GONE);
            
            if (myPage > 1)
            	setLoadMoreNovels();
            else{
            	setNovesAdapter();
            }

        }
        
        private void setLoadMoreNovels(){
        	if (moreNovels != null && moreNovels.size()!=0) {
        		myGridViewAdapter.addDatas(mActivity,moreNovels,apps);
                myGridViewAdapter.notifyDataSetChanged();
            } else {
                checkLoad = false;
                Toast.makeText(mActivity, "no more data", Toast.LENGTH_SHORT).show();
            }
            myGrid.onLoadMoreComplete();
        }
        
        private void setNovesAdapter(){
        	if (novels != null) {
                try {
                    layoutReload.setVisibility(View.GONE);
                    myGridViewAdapter = new GridViewAdapter(mActivity, novels,apps);
                    myGrid.setAdapter(myGridViewAdapter);
                } catch (Exception e) {

                }
            } else {
                layoutReload.setVisibility(View.VISIBLE);
            }
        }
    }

}
