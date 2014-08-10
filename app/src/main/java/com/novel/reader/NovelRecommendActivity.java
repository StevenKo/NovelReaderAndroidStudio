package com.novel.reader;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.GridView;

import com.ads.AdFragmentActivity;
import com.novel.reader.adapter.GridViewAdapter;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.GameAPP;
import com.novel.reader.entity.Novel;
import com.novel.reader.util.Setting;

import java.util.ArrayList;

public class NovelRecommendActivity extends AdFragmentActivity {

    private int categoryId;
    private String categoryName;
    private GridView gridView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Setting.setApplicationActionBarTheme(this);
        setContentView(R.layout.layout_novel_recommend);

        getBundleExtras();

        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle(categoryName);
        actionbar.setDisplayHomeAsUpEnabled(true);

        gridView = (GridView) findViewById(R.id.gridView1);

        new DownloadRecommendNovelTask().execute();

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
            progressdialogInit.dismiss();
            GridViewAdapter myGridViewAdapter = new GridViewAdapter(NovelRecommendActivity.this, novels, apps);
            gridView.setAdapter(myGridViewAdapter);
        }
    }
}
