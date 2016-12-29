package com.novel.reader;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.ads.MopubAdFragmentActivity;
import com.analytics.AnalyticsName;
import com.analytics.NovelReaderAnalyticsApp;
import com.novel.reader.adapter.GridViewAdapter;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.GameAPP;
import com.novel.reader.entity.Novel;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

public class NovelRecommendActivity extends MopubAdFragmentActivity {

    private int categoryId;
    private String categoryName;
    private GridView gridView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_novel_recommend);

        getBundleExtras();

        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle(categoryName);
        actionbar.setDisplayHomeAsUpEnabled(true);

        gridView = (GridView) findViewById(R.id.gridView1);

        new DownloadRecommendNovelTask().execute();

        trackScreen();
    }

    private void trackScreen() {
        Tracker t = ((NovelReaderAnalyticsApp) getApplication()).getTracker(NovelReaderAnalyticsApp.TrackerName.APP_TRACKER);
        t.setScreenName(AnalyticsName.NovelRecommendActivity);
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    private void getBundleExtras() {
        Bundle mBundle = this.getIntent().getExtras();
        categoryId = mBundle.getInt("RecommendCategoryId");
        categoryName = mBundle.getString("RecommendCategoryName");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DownloadRecommendNovelTask extends AsyncTask {
        private ArrayList<Novel> novels;
        private ArrayList<GameAPP> apps;

        private ProgressDialog progressdialogInit;
        private final DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                finish();
            }
        };

        @Override
        protected void onPreExecute() {
            progressdialogInit = ProgressDialog.show(NovelRecommendActivity.this, "Load", "Loading…");
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.setCancelable(true);
            progressdialogInit.show();
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            novels = NovelAPI.getRecommendCategoryNovels(categoryId);
            apps = NovelAPI.getAppInfo(NovelRecommendActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if(progressdialogInit!=null && progressdialogInit.isShowing())
                progressdialogInit.dismiss();
            GridViewAdapter myGridViewAdapter = new GridViewAdapter(NovelRecommendActivity.this, novels, apps);
            gridView.setAdapter(myGridViewAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    trackRecommendNovelClick(position);

                    Intent intent = new Intent(NovelRecommendActivity.this, NovelIntroduceActivity.class);
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
            });
        }

        private void trackRecommendNovelClick(int position) {
            Tracker t = ((NovelReaderAnalyticsApp)getApplication()).getTracker(NovelReaderAnalyticsApp.TrackerName.APP_TRACKER);
            t.send(new HitBuilders.EventBuilder().setCategory(AnalyticsName.Recommend).setAction(categoryName).setLabel(novels.get(position).getName()).build());
            t.send(new HitBuilders.EventBuilder().setCategory(AnalyticsName.Novel).setAction(novels.get(position).getName()).setLabel(AnalyticsName.NovelIntro).build());
        }
    }
}
