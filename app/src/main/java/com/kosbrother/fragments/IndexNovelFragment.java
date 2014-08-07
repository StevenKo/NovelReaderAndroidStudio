package com.kosbrother.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.novel.reader.R;
import com.novel.reader.adapter.GridViewAdapter;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.Novel;
import com.taiwan.imageload.LoadMoreGridView;

import java.util.ArrayList;


public class IndexNovelFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<Novel>>{

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
        return myFragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        myGrid.setOnLoadMoreListener(new LoadMoreGridView.OnLoadMoreListener() {
            public void onLoadMore() {
                if (checkLoad) {
                    myPage = myPage + 1;
                    loadmoreLayout.setVisibility(View.VISIBLE);
                    LoaderManager lm = getLoaderManager();
                    lm.restartLoader(novelFragment, null, IndexNovelFragment.this).forceLoad();
                } else {
                    myGrid.onLoadMoreComplete();
                }

            }
        });

        buttonReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                progressLayout.setVisibility(View.VISIBLE);
                layoutReload.setVisibility(View.GONE);
                LoaderManager lm = getLoaderManager();
                lm.restartLoader(novelFragment, null, IndexNovelFragment.this).forceLoad();
            }
        });

        if (myGridViewAdapter != null) {
            progressLayout.setVisibility(View.GONE);
            loadmoreLayout.setVisibility(View.GONE);
            myGrid.setAdapter(myGridViewAdapter);
        } else {
            LoaderManager lm = getLoaderManager();
            lm.restartLoader(novelFragment, null, this).forceLoad();
        }
    }

    private void setLoadMoreNovels(){
        if (moreNovels != null && moreNovels.size()!=0) {
            myGridViewAdapter.addDatas(mActivity,moreNovels,NovelAPI.getAppInfo(mActivity));
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
                myGridViewAdapter = new GridViewAdapter(mActivity, novels,NovelAPI.getAppInfo(mActivity));
                myGrid.setAdapter(myGridViewAdapter);
            } catch (Exception e) {

            }
        } else {
            layoutReload.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<ArrayList<Novel>> onCreateLoader(int i, Bundle bundle) {
        return new NovelLoader(getActivity(),novelFragment,myPage);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Novel>> arrayListLoader, ArrayList<Novel> getNovels) {

        moreNovels = getNovels;

        if (moreNovels != null && moreNovels.size()!=0) {
            for (int i = 0; i < moreNovels.size(); i++) {
                novels.add(moreNovels.get(i));
            }
        }

        progressLayout.setVisibility(View.GONE);
        loadmoreLayout.setVisibility(View.GONE);

        if (myPage > 1)
            setLoadMoreNovels();
        else{
            setNovesAdapter();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Novel>> arrayListLoader) {
    }

    public static class NovelLoader extends AsyncTaskLoader<ArrayList<Novel>> {

        private int novelFragment;
        private int myPage;

        public NovelLoader(Context context,int novelFragment, int myPage) {
            super(context);
            this.novelFragment = novelFragment;
            this.myPage = myPage;
        }

        @Override
        public ArrayList<Novel> loadInBackground() {

            ArrayList<Novel> moreNovels = null;

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
            return moreNovels;
        }
    }

}
