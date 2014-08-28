package com.novel.reader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ads.AdFragmentActivity;
import com.analytics.AnalyticsName;
import com.analytics.NovelReaderAnalyticsApp;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kosbrother.tool.ChildArticle;
import com.kosbrother.tool.ExpandListDownLoadReadAdapter;
import com.kosbrother.tool.Group;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.Article;
import com.novel.reader.entity.Novel;
import com.novel.reader.util.NovelReaderUtil;
import com.novel.reader.util.Setting;
import com.taiwan.imageload.ImageLoader;

import java.util.ArrayList;
import java.util.TreeMap;

public class MyDownloadArticleActivity extends AdFragmentActivity implements LoaderManager.LoaderCallbacks<ArrayList<Article>>{

    private static final int ID_SETTING = 0;
    private static final int ID_RESPONSE = 1;
    private static final int ID_ABOUT_US = 2;
    private static final int ID_GRADE = 3;
    private static final int ID_DELETE_DOWNLOAD = 4;
    private static final int LOADER_ID = 65;

    private Bundle mBundle;
    private String novelName;
    private int novelId;
    private String novelAuthor;
    private String novelPicUrl;
    private String novelArticleNum;
    private ImageView novelImageView;
    private TextView novelTextName;
    private TextView novelTextAuthor;
    private TextView downloadedCount;
    private ImageLoader mImageLoader;
    private LinearLayout novelLayoutProgress;
    private ArrayList<Article> articleList = new ArrayList<Article>();
    private ExpandableListView novelListView;
    private Novel theNovel;

    private TreeMap<String, ArrayList<Article>> myData = new TreeMap<String, ArrayList<Article>>();
    private static ArrayList<Group> mGroups = new ArrayList<Group>();
    private AlertDialog.Builder deleteDialog;
    private AlertDialog.Builder aboutUsDialog;


    private static MyDownloadArticleActivity myActivity;
    private static ActionMode myMode;
    private static ExpandListDownLoadReadAdapter mAdapter;
    private static boolean actionModeShowing = false;
    private static boolean isDeleteArticles = false;
    private ProgressDialog progressDialog = null;
    private Button updateNovelButton;
    private RelativeLayout novel_layout;

    private RelativeLayout bannerAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Setting.setApplicationActionBarTheme(this);
        setContentView(R.layout.layout_novel_downloaded);

        myActivity = MyDownloadArticleActivity.this;

        final ActionBar ab = getSupportActionBar();
        mBundle = this.getIntent().getExtras();
        novelName = mBundle.getString("NovelName");
        novelId = mBundle.getInt("NovelId");
        novelAuthor = mBundle.getString("NovelAuthor");
        novelPicUrl = mBundle.getString("NovelPicUrl");
        novelArticleNum = mBundle.getString("NovelArticleNum");

        theNovel = new Novel(novelId, novelName, novelAuthor, "", novelPicUrl, 0, novelArticleNum, "", false, false, false);

        ab.setTitle(getResources().getString(R.string.title_my_downloading));
        ab.setDisplayHomeAsUpEnabled(true);

        setViews();
        setAboutUsDialog();

        progressDialog = ProgressDialog.show(MyDownloadArticleActivity.this, "資料處理中...", null);
        LoaderManager lm = getSupportLoaderManager();
        lm.restartLoader(LOADER_ID, null, this).forceLoad();

        bannerAdView = (RelativeLayout) findViewById(R.id.adonView);
        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 0)
            mAdView = setBannerAdView(bannerAdView);

        trackScreen();
    }

    private void trackScreen() {
        Tracker t = ((NovelReaderAnalyticsApp) getApplication()).getTracker(NovelReaderAnalyticsApp.TrackerName.APP_TRACKER);
        t.setScreenName(AnalyticsName.MyDownloadArticleActivity);
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 1)
            bannerAdView.setVisibility(View.GONE);
    }


    private void setViews() {
        novelImageView = (ImageView) findViewById(R.id.novel_image);
        novelTextName = (TextView) findViewById(R.id.novel_name);
        novelTextAuthor = (TextView) findViewById(R.id.novel_author);
        novelListView = (ExpandableListView) findViewById(R.id.novel_download_artiles_list);
        downloadedCount = (TextView) findViewById(R.id.text_downloaded_count);
        novelLayoutProgress = (LinearLayout) findViewById(R.id.novel_layout_progress);
        updateNovelButton = (Button) findViewById(R.id.update_novel_button);
        novel_layout = (RelativeLayout) findViewById(R.id.novel_layout);

        novelTextName.setText(novelName + "(" + novelArticleNum + ")");
        novelTextAuthor.setText(getResources().getString(R.string.novel_author) + novelAuthor);

        mImageLoader = new ImageLoader(MyDownloadArticleActivity.this, 70);

        if (NovelReaderUtil.isDisplayDefaultBookCover(novelPicUrl)) {
            novelImageView.setImageResource(R.drawable.bookcover_default);
        } else {
            mImageLoader.DisplayImage(novelPicUrl, novelImageView);
        }

        deleteDialog = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.delete_title))
                .setMessage(getResources().getString(R.string.delete_message))
                .setPositiveButton(getResources().getString(R.string.delete_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Novel myNovel = new Novel(novelId, novelName, novelAuthor, "", novelPicUrl, 0, novelArticleNum, "", false, false, true);
                        NovelAPI.removeNovelFromDownload(myNovel, MyDownloadArticleActivity.this);
                        MyDownloadArticleActivity.this.finish();
                    }
                }).setNegativeButton(getResources().getString(R.string.delete_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        updateNovelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_to_download = new Intent(MyDownloadArticleActivity.this, DownloadActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("NovelId", novelId);
                bundle.putString("NovelName", novelName);
                intent_to_download.putExtras(bundle);
                startActivity(intent_to_download);
            }
        });
        novel_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activity_intent2 = new Intent(MyDownloadArticleActivity.this, NovelIntroduceActivity.class);
                activity_intent2.putExtra("NovelName", novelName);
                activity_intent2.putExtra("NovelAuthor", novelAuthor);
                activity_intent2.putExtra("NovelDescription", "");
                activity_intent2.putExtra("NovelUpdate", "");
                activity_intent2.putExtra("NovelPicUrl", novelPicUrl);
                activity_intent2.putExtra("NovelArticleNum", novelPicUrl);
                activity_intent2.putExtra("NovelId", novelId);
                startActivity(activity_intent2);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.activity_main, menu);

        menu.add(0, ID_SETTING, 0, "設定").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_RESPONSE, 1, "意見回餽").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_ABOUT_US, 2, "關於我們").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_GRADE, 3, "為App評分").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_DELETE_DOWNLOAD, 5, "刪除").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
                Intent intent = new Intent(MyDownloadArticleActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case ID_RESPONSE: // response
                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"service@kosbrother.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "意見回餽 from 小說王");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                break;
            case ID_ABOUT_US: // response
                aboutUsDialog.show();
                break;
            case ID_GRADE: // response
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=KosBrother"));
                startActivity(browserIntent);
                break;
            case ID_DELETE_DOWNLOAD: // response
                deleteDialog.show();
                break;

        }
        return true;
    }

    private void reverseMGroups() {
        // TODO Auto-generated method stub
        ArrayList<Group> aGroups = new ArrayList<Group>(mGroups.size());
        for (int i = 0; i < mGroups.size(); i++) {
            int groupInt = mGroups.size() - i - 1;
            aGroups.add(mGroups.get(groupInt));
            ArrayList<ChildArticle> theChildren = new ArrayList<ChildArticle>(mGroups.get(groupInt).getChildrenCount());
            for (int j = 0; j < mGroups.get(groupInt).getChildrenCount(); j++) {
                theChildren.add(mGroups.get(groupInt).getChildItem(mGroups.get(groupInt).getChildrenCount() - j - 1));
            }
            aGroups.get(i).setChilds(theChildren);
        }
        mGroups.clear();
        mGroups = aGroups;
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

    public static ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            // Assumes that you have "contexual.xml" menu resources
            inflater.inflate(R.menu.contextual, menu);
            myMode = mode;
            isDeleteArticles = false;
            return true;
        }

        // Called each time the action mode is shown. Always called after
        // onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete_articles:
                    isDeleteArticles = true;
                    myMode = mode;
                    actionModeShowing = false;
                    myMode.finish();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        public void onDestroyActionMode(ActionMode mode) {
            // mActionMode = null;

        }
    };

    public static void showCallBackAction() {
        boolean show = checkSelectOrNot();
        if (show && !actionModeShowing) {
            myActivity.startSupportActionMode(MyDownloadArticleActivity.mActionModeCallback);
            actionModeShowing = true;
        } else if (!show) {
            actionModeShowing = false;
            myMode.finish();
        }
    }

    private static boolean checkSelectOrNot() {
        for (int i = 0; i < mGroups.size(); i++) {
            int groupSize = mGroups.get(i).getChildrenCount();
            for (int j = groupSize; j > 0; j--) {
                ChildArticle aChildArticle = mGroups.get(i).getChildItem(j - 1);
                if (aChildArticle.getChecked() && aChildArticle.isDownload()) {
                    return true;
                }
            }
        }
        return false;
    }

    // 如果有按刪除鍵, 就刪除並重整Data
    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        actionModeShowing = false;
        if (isDeleteArticles) {
            progressDialog = ProgressDialog.show(MyDownloadArticleActivity.this, "資料處理中...", null);
            LoaderManager lm = getSupportLoaderManager();
            lm.restartLoader(LOADER_ID, null, this).forceLoad();
        }
    }

    private static void removeArticles() {
        // TODO Auto-generated method stub
        for (int i = 0; i < mGroups.size(); i++) {
            ArrayList<Article> articlesList = new ArrayList<Article>();
            int groupSize = mGroups.get(i).getChildrenCount();
            for (int j = groupSize; j > 0; j--) {
                ChildArticle aChildArticle = mGroups.get(i).getChildItem(j - 1);
                if (aChildArticle.getChecked() && aChildArticle.isDownload()) {
                    Article theArticle = new Article(aChildArticle.getId(), aChildArticle.getNovelId(), "", aChildArticle.getTitle(),
                            aChildArticle.getSubject(), true, aChildArticle.getNum());
                    // NovelAPI.removeArticle(theArticle, myActivity);
                    articlesList.add(theArticle);
                    mGroups.get(i).removeChild(j - 1);
                }
            }
            NovelAPI.removeArticles(articlesList, myActivity);
        }
    }

    @Override
    public Loader<ArrayList<Article>> onCreateLoader(int i, Bundle bundle) {
        if (isDeleteArticles) {
            removeArticles();
            isDeleteArticles = false;
        }

        return new ArticleLoader(getBaseContext(),novelId);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Article>> arrayListLoader, ArrayList<Article> getArticles) {
        articleList = getArticles;

        if (progressDialog.isShowing())
            progressDialog.cancel();

        myData = new TreeMap<String, ArrayList<Article>>();
        mGroups = new ArrayList<Group>();

        if (articleList != null) {

            // use HashMap || TreeMap to make a parent key
            for (int i = 0; i < articleList.size(); i++) {
                if (myData.containsKey(articleList.get(i).getSubject())) {
                    myData.get(articleList.get(i).getSubject()).add(articleList.get(i));
                } else {
                    // groupTitleList.add(articleList.get(i).getSubject());
                    mGroups.add(new Group(articleList.get(i).getSubject()));
                    myData.put(articleList.get(i).getSubject(), new ArrayList<Article>());
                    myData.get(articleList.get(i).getSubject()).add(articleList.get(i));
                }
            }

            for (int i = 0; i < mGroups.size(); i++) {
                ArrayList<Article> articles = myData.get(mGroups.get(i).getTitle());
                for (int j = 0; j < articles.size(); j++) {
                    mGroups.get(i).addChildrenItem(
                            new ChildArticle(articles.get(j).getId(), articles.get(j).getNovelId(), "", articles.get(j).getTitle(), articles.get(j)
                                    .getSubject(), articles.get(j).isDownload(), articles.get(j).getNum())
                    );
                }
            }

            reverseMGroups();
            // ExpandListAdapter mAdapter = new ExpandListAdapter(MyDownloadArticleActivity.this, mGroups, theNovel, -1);
            mAdapter = new ExpandListDownLoadReadAdapter(MyDownloadArticleActivity.this, mGroups, theNovel);

            novelListView.setAdapter(mAdapter);

        }

        String txtCount = Integer.toString(articleList.size());
        downloadedCount.setText("共 " + txtCount + " 個下載");

        novelLayoutProgress.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Article>> arrayListLoader) {

    }

    public static class ArticleLoader extends AsyncTaskLoader<ArrayList<Article>> {

        int novelId;

        public ArticleLoader(Context context, int novelId) {
            super(context);
            this.novelId = novelId;
        }

        @Override
        public ArrayList<Article> loadInBackground() {
            return NovelAPI.getDownloadedNovelArticles(novelId, false, getContext());
        }
    }
}
