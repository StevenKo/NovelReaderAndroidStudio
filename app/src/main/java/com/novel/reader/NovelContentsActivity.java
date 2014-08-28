package com.novel.reader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.analytics.AnalyticsName;
import com.analytics.NovelReaderAnalyticsApp;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.novel.reader.adapter.ContentAdapter;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.Article;
import com.novel.reader.util.Setting;

import java.util.ArrayList;

public class NovelContentsActivity extends NovelReaderBaseActivity {
    private Bundle mBundle;
    private ListView novelListView;
    private String novelName;
    private int novelId;
    private LinearLayout novelLayoutProgress;
    private int articleId;
    private ArrayList<Article> articleList = new ArrayList<Article>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Setting.setApplicationActionBarTheme(this);
        setContentView(R.layout.layout_contents);

        getNovel();

        ActionBar ab = getSupportActionBar();
        ab.setTitle(novelName);
        ab.setDisplayHomeAsUpEnabled(true);

        setViews();
        new DownloadArticlesTask().execute();

        trackScreen();
    }

    private void trackScreen() {
        Tracker t = ((NovelReaderAnalyticsApp) getApplication()).getTracker(NovelReaderAnalyticsApp.TrackerName.APP_TRACKER);
        t.setScreenName(AnalyticsName.NovelContentsActivity);
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    private void setViews() {
        novelListView = (ListView) findViewById(R.id.artiles_list);
        novelLayoutProgress = (LinearLayout) findViewById(R.id.novel_layout_progress);
    }

    private void getNovel() {
        mBundle = this.getIntent().getExtras();
        novelName = mBundle.getString("NovelName");
        novelId = mBundle.getInt("NovelId");
        articleId = mBundle.getInt("ArticleId");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private class DownloadArticlesTask extends AsyncTask {
        private ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(NovelContentsActivity.this, "", getResources().getString(R.string.toast_novel_downloading));
            progressDialog.setCancelable(true);
        }

        @Override
        protected Object doInBackground(Object... params) {
            // TODO Auto-generated method stub
            articleList = NovelAPI.getNovelArticles(novelId, true, NovelContentsActivity.this);
            if (articleList == null)
                articleList = new ArrayList<Article>();
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            novelListView.setAdapter(new ContentAdapter(NovelContentsActivity.this, articleList, articleId));

            novelListView.post(new Runnable() {
                @Override
                public void run() {
                    novelListView.smoothScrollToPosition(getArticlePosition());
                }
            });

            novelListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("SelectArticleId", articleList.get(position).getId());
                    returnIntent.putExtra("SelectArticlePosition", position);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            });
            novelLayoutProgress.setVisibility(View.GONE);
            progressDialog.cancel();

        }

        private int getArticlePosition() {
            for (int i = 0; i < articleList.size(); i++) {
                if (articleList.get(i).getId() == articleId) {
                    return i;
                }
            }
            return 0;
        }


    }
}
