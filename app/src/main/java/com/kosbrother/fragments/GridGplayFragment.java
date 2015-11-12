/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Gabriele Mariotti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package com.kosbrother.fragments;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.analytics.AnalyticsName;
import com.analytics.NovelReaderAnalyticsApp;
import com.novel.reader.NovelRecommendActivity;
import com.novel.reader.R;
import com.novel.reader.adapter.GridViewAdapter;
import com.novel.reader.adapter.GridViewDownloadAdapter;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.Category;
import com.novel.reader.entity.Novel;
import com.novel.reader.util.NovelReaderUtil;

import android.app.ProgressDialog;
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
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.view.CardGridView;

/**
 * Grid as Google Play example
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class GridGplayFragment extends Fragment {

    protected ListView mListView;
    private static final int LOADER_ID = 1000;
    ProgressDialog progressDialog = null;
    private boolean isNetworkConnect;

    private GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(NovelReaderUtil.isNetworkConnected(getActivity()))
            isNetworkConnect = true;
        else
            isNetworkConnect = false;


        if(isNetworkConnect) {
            View contentView = inflater.inflate(R.layout.demo_fragment_grid_gplay, container, false);
            mListView = (ListView) contentView.findViewById(R.id.list_view);
            return contentView;
        }else{
            View contentView = inflater.inflate(R.layout.layout_no_network_index, container, false);
            mGridView = (GridView) contentView.findViewById(R.id.gridView1);
            TextView indexCategoryName = (TextView) contentView.findViewById(R.id.category_name);
            indexCategoryName.setText(getString(R.string.my_download));
            return contentView;
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        if(isNetworkConnect) {
            progressDialog = ProgressDialog.show(getActivity(), "", getResources().getString(R.string.toast_novel_downloading));
            progressDialog.setCancelable(true);

            LoaderManager lm = getLoaderManager();
            lm.initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<ArrayList<Category>>() {

                @Override
                public Loader<ArrayList<Category>> onCreateLoader(int i, Bundle bundle) {
                    return new DownloadRecommnedLoader(getActivity());
                }

                @Override
                public void onLoadFinished(Loader<ArrayList<Category>> arrayListLoader, ArrayList<Category> categories) {
                    ListItemAdapter listItemAdapter = new ListItemAdapter(getActivity(), categories);
                    mListView.setAdapter(listItemAdapter);
                    progressDialog.cancel();
                }

                @Override
                public void onLoaderReset(Loader<ArrayList<Category>> arrayListLoader) {

                }
            }).forceLoad();
        }else{
            final ArrayList<Novel> novels = NovelAPI.getDownloadedNovels(getActivity());
            GridViewDownloadAdapter myGridViewAdapter = new GridViewDownloadAdapter(getActivity(), novels);
            mGridView.setAdapter(myGridViewAdapter);
        }
    }

    public Fragment newInstance() {
        return new GridGplayFragment();
    }

    public static class DownloadRecommnedLoader extends AsyncTaskLoader<ArrayList<Category>> {

        public DownloadRecommnedLoader(Context context) {
            super(context);
        }

        @Override
        public ArrayList<Category> loadInBackground() {
            ArrayList<Category> categories = NovelAPI.getRecommendCategoryWithNovels();
            return categories;
        }
    }


    private void initCards(CardGridView gridView, ArrayList<Novel> novels, String cateName) {

        GridViewAdapter mCardArrayAdapter = new GridViewAdapter(getActivity(), novels,NovelAPI.getAppInfo(getActivity()));

        if (gridView != null) {
            gridView.setAdapter(mCardArrayAdapter);
        }
    }

    public class ListItemAdapter extends BaseAdapter {
        ArrayList<Category> categories;
        Context mContext;

        public ListItemAdapter(Context mContext, ArrayList<Category> categories) {
            this.categories = categories;
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View gridLayout = layoutInflater.inflate(R.layout.carddemo_scrollview_ltem, null);
            TextView categoryName = (TextView) gridLayout.findViewById(R.id.category_name);
            categoryName.setText(categories.get(i).getCateName());
            RelativeLayout cateogyLayout = (RelativeLayout) gridLayout.findViewById(R.id.recommend_title);

            final int finalI = i;
            cateogyLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    trackRecommendCategory();

                    Intent intent = new Intent();
                    intent.putExtra("RecommendCategoryId", categories.get(finalI).getId());
                    intent.putExtra("RecommendCategoryName", categories.get(finalI).getCateName());
                    intent.setClass(getActivity(), NovelRecommendActivity.class);
                    startActivity(intent);
                }

                private void trackRecommendCategory() {
                    Tracker t = ((NovelReaderAnalyticsApp)getActivity().getApplication()).getTracker(
                            NovelReaderAnalyticsApp.TrackerName.APP_TRACKER);
                    t.send(new HitBuilders.EventBuilder().setCategory(AnalyticsName.Recommend).setAction(categories.get(finalI).getCateName()).setLabel(AnalyticsName.RecommendIndex).build());
                }
            });

            CardGridView groupGridView = (CardGridView) gridLayout.findViewById(R.id.carddemo_grid_base1);
            initCards(groupGridView, categories.get(i).novels, categories.get(finalI).getCateName());
            return gridLayout;
        }
    }


}
