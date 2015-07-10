package com.novel.reader;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ads.MopubAdFragmentActivity;
import com.analytics.AnalyticsName;
import com.analytics.NovelReaderAnalyticsApp;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.novel.db.SQLiteNovel;
import com.novel.reader.adapter.RecentSearchAdapter;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.Novel;
import com.novel.reader.util.Setting;
import com.taiwan.imageload.ImageLoader;
import com.taiwan.imageload.LoadMoreGridView;

import java.util.ArrayList;

public class SearchActivity extends MopubAdFragmentActivity {

    private Bundle mBundle;
    private String keyword;
    private ArrayList<Novel> novels;
    private LoadMoreGridView myGrid;
    private MenuItem item;

    private AlertDialog.Builder aboutUsDialog;

    private RelativeLayout bannerAdView;
    private LinearLayout progressLayout;
    private LinearLayout loadmoreLayout;
    private LinearLayout noDataLayout;
    private ActionBar ab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Setting.setApplicationActionBarTheme(this);
        setContentView(R.layout.layout_search);

        ab = getSupportActionBar();
        mBundle = this.getIntent().getExtras();
        keyword = mBundle.getString("SearchKeyword");
        myGrid = (LoadMoreGridView) findViewById(R.id.news_list);
        progressLayout = (LinearLayout) findViewById(R.id.layout_progress);
        loadmoreLayout = (LinearLayout) findViewById(R.id.load_more_grid);
        noDataLayout = (LinearLayout) findViewById(R.id.layout_no_data);

        SQLiteNovel db = new SQLiteNovel(SearchActivity.this);
        db.deleteQueryHistory(keyword);
        db.insertQueryHistory(keyword);

        myGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Novel novel = novels.get(position);
                trackNovelClick(novel);
                Intent newAct = new Intent();
                Bundle bundle = new Bundle();
                bundle.putInt("NovelId", novel.getId());
                bundle.putString("NovelName", novel.getName());
                bundle.putString("NovelAuthor", novel.getAuthor());
                bundle.putString("NovelDescription", novel.getDescription());
                bundle.putString("NovelUpdate", novel.getLastUpdate());
                bundle.putString("NovelPicUrl", novel.getPic());
                bundle.putString("NovelArticleNum", novel.getArticleNum());
                newAct.putExtras(bundle);
                newAct.setClass(SearchActivity.this, NovelIntroduceActivity.class);
                startActivity(newAct);
            }

            private void trackNovelClick(Novel novel) {
                Tracker t = ((NovelReaderAnalyticsApp) getApplication()).getTracker(NovelReaderAnalyticsApp.TrackerName.APP_TRACKER);
                t.send(new HitBuilders.EventBuilder().setCategory(AnalyticsName.Search).setAction(keyword).setLabel(novel.getName()).build());
                t.send(new HitBuilders.EventBuilder().setCategory(AnalyticsName.Novel).setAction(novel.getName()).setLabel(AnalyticsName.NovelIntro).build());
            }

        });

        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(getString(R.string.menu_search) + ":" + keyword);

        setAboutUsDialog();

        bannerAdView = (RelativeLayout) findViewById(R.id.adonView);
        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 0)
            moPubView = setBannerAdView(bannerAdView);

        trackScreen();

    }

    @Override
    protected void onStart() {
        new LoadDataTask().execute();
        super.onStart();
    }

    private void trackScreen() {
        Tracker t = ((NovelReaderAnalyticsApp) getApplication()).getTracker(NovelReaderAnalyticsApp.TrackerName.APP_TRACKER);
        t.setScreenName(AnalyticsName.SearchActivity);
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 1)
            bannerAdView.setVisibility(View.GONE);
    }

    private void fetchData() {
        novels = NovelAPI.searchNovels(keyword);
    }

    public class SearchAdapter extends BaseAdapter {

        private final Context mContext;
        private final ArrayList<Novel> novels;
        private final ImageLoader imageLoader;

        public SearchAdapter(Context mContext, ArrayList<Novel> novels) {
            this.novels = novels;
            this.mContext = mContext;
            imageLoader = new ImageLoader(mContext);
        }

        @Override
        public int getCount() {
            return novels.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater myInflater = LayoutInflater.from(mContext);
            View converView = myInflater.inflate(R.layout.item_gridview_novel, null);
            ImageView pic = (ImageView) converView.findViewById(R.id.grid_item_image);
            TextView name = (TextView) converView.findViewById(R.id.grid_item_name);
            TextView author = (TextView) converView.findViewById(R.id.grid_item_author);
            TextView articleNum = (TextView) converView.findViewById(R.id.grid_item_counts);
            TextView textFinish = (TextView) converView.findViewById(R.id.grid_item_finish);
            TextView textSerialize = (TextView) converView.findViewById(R.id.serializing);

            imageLoader.DisplayImage(novels.get(position).getPic(), pic);
            name.setText(novels.get(position).getName());
            author.setText(novels.get(position).getAuthor());
            articleNum.setText(novels.get(position).getArticleNum());
            textFinish.setText(novels.get(position).getLastUpdate());

            if (novels.get(position).isSerializing()) {
                textSerialize.setText("連載中...");
            } else {
                textSerialize.setText("全本");
            }

            return converView;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.activity_main, menu);
        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 1)
            menu.findItem(R.id.menu_donate).setEnabled(false);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        final MenuItem searchMenuItem = menu.findItem(R.id.search);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if(queryTextFocused) {
                    SQLiteNovel db = new SQLiteNovel(SearchActivity.this);
                    final SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
                    search.setSuggestionsAdapter(new RecentSearchAdapter(SearchActivity.this, db.getLastQueryHistory(100,"")));
                }else{
                    searchMenuItem.collapseActionView();
                    searchView.setQuery("", false);
                }
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {

                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                String query = cursor.getString(1);

                keyword = query;
                ab.setTitle(getString(R.string.menu_search)+":"+keyword);
                new LoadDataTask().execute();
                searchMenuItem.collapseActionView();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                keyword = s;
                ab.setTitle(getString(R.string.menu_search)+":"+keyword);
                new LoadDataTask().execute();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                loadHistory(query);
                return true;
            }

            private void loadHistory(String query) {
                SQLiteNovel db = new SQLiteNovel(SearchActivity.this);
                final SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
                search.setSuggestionsAdapter(new RecentSearchAdapter(SearchActivity.this, db.getLastQueryHistory(100,query)));
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                // Toast.makeText(this, "home pressed", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_settings: // setting
                Intent intent = new Intent(SearchActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_respond: // response
                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.respond_mail_address)});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.respond_mail_title));
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                break;
            case R.id.menu_aboutus:
                aboutUsDialog.show();
                break;
            case R.id.menu_recommend:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.recommend_url)));
                startActivity(browserIntent);
                break;
            case R.id.menu_donate:
                Intent intent1 = new Intent();
                intent1.setClass(this, DonateActivity.class);
                startActivity(intent1);
                break;
        }
        return true;
    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {


        @Override
        protected void onPreExecute() {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

            loadmoreLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
            noDataLayout.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            fetchData();
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            if (novels != null && novels.size() != 0) {
                myGrid.setVisibility(View.VISIBLE);
                myGrid.setAdapter(new SearchAdapter(SearchActivity.this, novels));
                progressLayout.setVisibility(View.GONE);
            } else {
                progressLayout.setVisibility(View.GONE);
                noDataLayout.setVisibility(View.VISIBLE);
                myGrid.setVisibility(View.GONE);
            }
            if (item != null){
                item.collapseActionView();
                EditText search = (EditText) item.getActionView();
                search.setText(keyword);
                search.setSelection(keyword.length());

                trackSearch();
            }
        }

        private void trackSearch() {
            Tracker t = ((NovelReaderAnalyticsApp) getApplication()).getTracker(NovelReaderAnalyticsApp.TrackerName.APP_TRACKER);
            if(novels != null)
                t.send(new HitBuilders.EventBuilder().setCategory(AnalyticsName.Search).setAction(keyword).setLabel(AnalyticsName.Index).setValue(novels.size()).build());
            else {
                t.send(new HitBuilders.EventBuilder().setCategory(AnalyticsName.Search).setAction(keyword).setLabel(AnalyticsName.SearchNull).build());
            }
        }

    }

    private void setAboutUsDialog() {
        // TODO Auto-generated method stub
        aboutUsDialog = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.about_us_string)).setIcon(R.drawable.play_store_icon)
                .setMessage(getResources().getString(R.string.about_us))
                .setPositiveButton(getResources().getString(R.string.yes_string), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
    }

}
