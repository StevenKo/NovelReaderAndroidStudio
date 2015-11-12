package com.kosbrother.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.analytics.AnalyticsName;
import com.analytics.NovelReaderAnalyticsApp;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.novel.reader.CategoryActivity;
import com.novel.reader.NovelIntroduceActivity;
import com.novel.reader.R;
import com.novel.reader.adapter.GridViewAdapter;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.GameAPP;
import com.novel.reader.entity.Novel;
import com.taiwan.imageload.LoadMoreGridView;

import java.util.ArrayList;

public final class CategoryRecommendFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<Novel>> {

    private ArrayList<Novel> novels = new ArrayList<Novel>();
    private ArrayList<Novel> moreNovels = new ArrayList<Novel>();
    private int myPage = 1;
    private LoadMoreGridView myGrid;
    private GridViewAdapter myGridViewAdapter;
    private Boolean checkLoad = true;
    private LinearLayout progressLayout;
    private LinearLayout loadmoreLayout;
    private LinearLayout noDataLayout;
    private LinearLayout layoutReload;
    private int Loader_Id = 40;
    private Button buttonReload;
    private Activity mActivity;
    public ArrayList<GameAPP> apps;
    private String categoryName;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    public static CategoryRecommendFragment newInstance(String categoryName) {
        CategoryRecommendFragment fragment = new CategoryRecommendFragment();
        Bundle args = new Bundle();
        args.putString("CategoryName", categoryName);
        fragment.setArguments(args);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        categoryName = getArguments().getString("CategoryName");

        View myFragmentView = inflater.inflate(R.layout.loadmore_grid, container, false);
        progressLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_progress);
        loadmoreLayout = (LinearLayout) myFragmentView.findViewById(R.id.load_more_grid);
        noDataLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_no_data);
        layoutReload = (LinearLayout) myFragmentView.findViewById(R.id.layout_reload);
        buttonReload = (Button) myFragmentView.findViewById(R.id.button_reload);
        myGrid = (LoadMoreGridView) myFragmentView.findViewById(R.id.news_list);

        return myFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        myGrid.setOnLoadMoreListener(new LoadMoreGridView.OnLoadMoreListener() {
            public void onLoadMore() {
                // Do the work to load more items at the end of list

                if (checkLoad) {
                    myPage = myPage + 1;
                    loadmoreLayout.setVisibility(View.VISIBLE);
                    LoaderManager lm = getLoaderManager();
                    lm.restartLoader(Loader_Id, null, CategoryRecommendFragment.this).forceLoad();
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
                lm.restartLoader(Loader_Id, null, CategoryRecommendFragment.this).forceLoad();
            }
        });

        if (myGridViewAdapter == null) {
            LoaderManager lm = getLoaderManager();
            lm.restartLoader(Loader_Id, null, CategoryRecommendFragment.this).forceLoad();
        }
    }

    private void setLoadMoreNovels() {
        if (moreNovels != null && moreNovels.size() != 0) {
            myGridViewAdapter.addDatas(mActivity, moreNovels, NovelAPI.getAppInfo(mActivity));
            myGridViewAdapter.notifyDataSetChanged();
        } else {
            checkLoad = false;
            Toast.makeText(mActivity, "no more data", Toast.LENGTH_SHORT).show();
        }
        myGrid.onLoadMoreComplete();
    }

    private void setNovesAdapter() {
        if (novels != null) {
            try {
                layoutReload.setVisibility(View.GONE);
                myGridViewAdapter = new GridViewAdapter(mActivity, novels, NovelAPI.getAppInfo(mActivity));
                myGrid.setAdapter(myGridViewAdapter);
            } catch (Exception e) {

            }
            myGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    trackNovelClick(position);

                    Intent intent = new Intent( getActivity(), NovelIntroduceActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("NovelId", novels.get(position).getId());
                    bundle.putString("NovelName", novels.get(position).getName());
                    bundle.putString("NovelAuthor", novels.get(position).getAuthor());
                    bundle.putString("NovelDescription", novels.get(position).getDescription());
                    bundle.putString("NovelUpdate", novels.get(position).getLastUpdate());
                    bundle.putString("NovelPicUrl", novels.get(position).getPic());
                    bundle.putString("NovelArticleNum", novels.get(position).getArticleNum());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

                private void trackNovelClick(int position) {
                    Tracker t = ((NovelReaderAnalyticsApp) getActivity().getApplication()).getTracker(NovelReaderAnalyticsApp.TrackerName.APP_TRACKER);
                    t.send(new HitBuilders.EventBuilder().setCategory(categoryName).setAction(AnalyticsName.CategoryRecommendFragment).setLabel(novels.get(position).getName()).build());
                    t.send(new HitBuilders.EventBuilder().setCategory(AnalyticsName.Novel).setAction(novels.get(position).getName()).setLabel(AnalyticsName.NovelIntro).build());
                }
            });
        } else {
            layoutReload.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<ArrayList<Novel>> onCreateLoader(int i, Bundle bundle) {
        return new NovelLoader(getActivity(), myPage);
    }


    @Override
    public void onLoadFinished(Loader<ArrayList<Novel>> arrayListLoader, ArrayList<Novel> getNovels) {
        moreNovels = getNovels;

        if (moreNovels != null && moreNovels.size() != 0) {
            for (int i = 0; i < moreNovels.size(); i++) {
                novels.add(moreNovels.get(i));
            }
        }

        progressLayout.setVisibility(View.GONE);
        loadmoreLayout.setVisibility(View.GONE);

        if (myPage > 1)
            setLoadMoreNovels();
        else {
            setNovesAdapter();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Novel>> arrayListLoader) {

    }

    public static class NovelLoader extends AsyncTaskLoader<ArrayList<Novel>> {

        private int myPage;

        public NovelLoader(Context context, int myPage) {
            super(context);
            this.myPage = myPage;
        }

        @Override
        public ArrayList<Novel> loadInBackground() {

            ArrayList<Novel> moreNovels = NovelAPI.getCategoryRecommendNovels(CategoryActivity.categoryId, myPage);
            return moreNovels;
        }
    }

}
