package com.novel.reader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.Novel;
import com.novel.reader.util.Setting;
import com.taiwan.imageload.ImageLoader;
import com.taiwan.imageload.LoadMoreGridView;

import java.util.ArrayList;

public class SearchActivity extends MopubAdFragmentActivity {

    private static final int ID_SETTING = 0;
    private static final int ID_RESPONSE = 1;
    private static final int ID_ABOUT_US = 2;
    private static final int ID_GRADE = 3;
    private static final int ID_SEARCH = 5;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.activity_main, menu);

        menu.add(0, ID_SETTING, 0, getResources().getString(R.string.menu_settings)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_RESPONSE, 1, getResources().getString(R.string.menu_respond)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_ABOUT_US, 2, getResources().getString(R.string.menu_aboutus)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_GRADE, 3, getResources().getString(R.string.menu_recommend)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 0)
            menu.add(0, 7, 7, getResources().getString(R.string.buy_year_subscription)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        item = menu.add(0, ID_SEARCH, 4, getResources().getString(R.string.menu_search)).setIcon(R.drawable.ic_search_inverse)
                .setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    private EditText search;

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        search = (EditText) item.getActionView();
                        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
                        search.setInputType(InputType.TYPE_CLASS_TEXT);
                        search.requestFocus();
                        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                                    keyword = v.getText().toString();
                                    ab.setTitle(getString(R.string.menu_search)+":"+keyword);
                                    new LoadDataTask().execute();

                                    return true;
                                }

                                return false;
                            }
                        });
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return true;
                    }
                }).setActionView(R.layout.collapsible_edittext);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

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
            case ID_SETTING: // setting
                Intent intent = new Intent(SearchActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case ID_RESPONSE: // response
                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.respond_mail_address)});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.respond_mail_title));
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                break;
            case ID_ABOUT_US:
                aboutUsDialog.show();
                break;
            case ID_GRADE:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.recommend_url)));
                startActivity(browserIntent);
                break;
            case ID_SEARCH: // response
                this.item = item;
                break;
            case 7:
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
