package com.novel.reader;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import com.ads.AdInterstitialManager;
import com.ads.MopubAdFragmentActivity;
import com.analytics.AnalyticsName;
import com.analytics.NovelReaderAnalyticsApp;
import com.android.slidingtab.SlidingTabLayout;
import com.kosbrother.fragments.CategoryListFragment;
import com.kosbrother.fragments.GridGplayFragment;
import com.kosbrother.fragments.IndexNovelFragment;
import com.kosbrother.tool.RecommendNovelDialog;
import com.kosbrother.tool.Report;
import com.mopub.mobileads.MoPubInterstitial;
import com.novel.db.SQLiteNovel;
import com.novel.reader.adapter.RecentSearchAdapter;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.util.NovelReaderUtil;
import com.novel.reader.util.Setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends MopubAdFragmentActivity implements NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.OnConnectionFailedListener{

    private static final int ID_SETTING = 0;
    private static final int ID_RESPONSE = 1;
    private static final int ID_ABOUT_US = 2;
    private static final int ID_GRADE = 3;
    private static final int ID_SEARCH = 5;
    private static final int ID_Report = 6;

    private static final int RC_SIGN_IN = 9001;
    private String[] CONTENT;
    private MenuItem itemSearch;
    private ViewPager pager;
    private Builder aboutUsDialog;
    MoPubInterstitial mInterstitial;


    //gcm
    public static final String EXTRA_MESSAGE = "message";

    private RelativeLayout bannerAdView;
    private SlidingTabLayout mSlidingTabLayout;



    private CharSequence mTitle;
    private NavigationView navigationView;
    private TextView logInEmail;
    private String email;
    private SignInButton signInBtn;
    private Button signOutBtn;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        setContentView(R.layout.layout_main);
        setTextLocale();
        setViewPagerAndSlidingTab();
        setAboutUsDialog();
        setNavigationDrawler();
        setSignInSignOut();

        if (Setting.getSettingInt(Setting.keyUpdateAppVersion, this) < Setting.getAppVersion(this)) {
            showUpdateInfoDialog(this);
            Setting.saveSetting(Setting.keyUpdateAppVersion, Setting.getAppVersion(this), this);
        }

        checkDB();

        bannerAdView = (RelativeLayout) findViewById(R.id.adonView);
        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 0) {
            moPubView = setBannerAdView(bannerAdView);
        }

        setCheckCollectNovelsAlarm();
        trackScreen();

        Random r = new Random();
        int i1 = r.nextInt(20);
        if(i1 == 15)
            new AppOpenCheckUpdateTask().execute();
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            Log.d(NovelAPI.TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.pull_to_refresh_refreshing_label));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void setSignInSignOut() {
        View headLayout = navigationView.getHeaderView(0);
        logInEmail = (TextView)headLayout.findViewById(R.id.log_in_email);
        signInBtn = (SignInButton)headLayout.findViewById(R.id.sign_in_button);
        signOutBtn = (Button)headLayout.findViewById(R.id.sign_out_button);

        signInBtn.setSize(SignInButton.SIZE_WIDE);

        signInBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        signOutBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        drawNavigationServerPartAndSignInBtn(false);
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void drawNavigationServerPartAndSignInBtn(boolean signedIn) {
        if(signedIn){
            logInEmail.setText(email);
            signInBtn.setVisibility(View.GONE);
            signOutBtn.setVisibility(View.VISIBLE);
            navigationView.getMenu().setGroupVisible(R.id.backup_group, true);
        }else{
            logInEmail.setText(getText(R.string.not_login));
            signInBtn.setVisibility(View.VISIBLE);
            signOutBtn.setVisibility(View.GONE);
            navigationView.getMenu().setGroupVisible(R.id.backup_group, false);
        }
    }

    private void trackScreen() {

        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                trackFragment(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        trackFragment(pager.getCurrentItem());
    }

    private void trackFragment(int position) {
        Tracker t = ((NovelReaderAnalyticsApp) getApplication()).getTracker(NovelReaderAnalyticsApp.TrackerName.APP_TRACKER);
        if (position == 0) {
            t.setScreenName(AnalyticsName.IndexCatogry);
        } else if (position == 1) {
            t.setScreenName(AnalyticsName.IndexRecommendGrid);
        } else if (position == 2) {
            t.setScreenName(AnalyticsName.IndexFragmentMap.get(IndexNovelFragment.LATEST_NOVEL));
        } else if (position == 3) {
            t.setScreenName(AnalyticsName.IndexFragmentMap.get(IndexNovelFragment.NEW_UPLOADED));
        } else if (position == 4) {
            t.setScreenName(AnalyticsName.IndexFragmentMap.get(IndexNovelFragment.WEEK_NOVEL));
        } else if (position == 5) {
            t.setScreenName(AnalyticsName.IndexFragmentMap.get(IndexNovelFragment.MONTH_NOVEL));
        } else if (position == 6) {
            t.setScreenName(AnalyticsName.IndexFragmentMap.get(IndexNovelFragment.HOT_NOVEL));
        }
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    private void setCheckCollectNovelsAlarm() {
        NovelReaderUtil.dailyCollectNovelsAlarmSetup(this);
    }

    private void setNavigationDrawler() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.ic_toobar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setViewPagerAndSlidingTab() {
        Resources res = getResources();
        CONTENT = res.getStringArray(R.array.sections);
        FragmentPagerAdapter adapter = new NovelPagerAdapter(getSupportFragmentManager());

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(pager);

        pager.setCurrentItem(1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new AdInterstitialManager(this);

        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 1)
            bannerAdView.setVisibility(View.GONE);
    }

    private void showUpdateInfoDialog(Activity mActivity) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        LinearLayout recomendLayout = (LinearLayout) inflater.inflate(R.layout.dialog_update_info, null);
        TextView update_text = (TextView) recomendLayout.findViewById(R.id.update_tip);
        update_text.setText(Html.fromHtml(getResources().getString(R.string.update_info)));

        Builder a = new Builder(mActivity).setTitle(mActivity.getResources().getString(R.string.update)).setIcon(R.drawable.app_icon)
                .setPositiveButton(mActivity.getResources().getString(R.string.yes_string), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });
        a.setView(recomendLayout);
        a.show();
    }


    private void setTextLocale() {
        Locale current = getResources().getConfiguration().locale;
        String country = current.getCountry();
        SharedPreferences sharePreference = getSharedPreferences(Setting.keyPref, 0);
        int text_setting_value = sharePreference.getInt(Setting.keyTextLanguage, 1000);
        if (text_setting_value == 1000 && country.toLowerCase().contains("cn"))
            Setting.saveSetting(Setting.keyTextLanguage, Setting.TEXT_CHINA, this);
    }

    void checkDB() {
        File cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "kosnovel");
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        File sdcardDB = new File(cacheDir, SQLiteNovel.DB_NAME);
        if (!sdcardDB.exists()) {
            ProgressDialog progressdialogInit;
            progressdialogInit = ProgressDialog.show(MainActivity.this, "Load", "Loading…");
            progressdialogInit.setTitle("初始化DB");
            progressdialogInit.setMessage("初始化DB中…(原先下載過小說的用戶，會將資料轉至 SD卡）");
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.setCancelable(false);
            progressdialogInit.show();
            SQLiteNovel db = new SQLiteNovel(MainActivity.this);
            progressdialogInit.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        final MenuItem searchMenuItem = menu.findItem(R.id.search);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if(queryTextFocused) {
                    SQLiteNovel db = new SQLiteNovel(MainActivity.this);
                    final SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
                    search.setSuggestionsAdapter(new RecentSearchAdapter(MainActivity.this, db.getLastQueryHistory(100,"")));
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

                searchMenuItem.collapseActionView();

                Bundle bundle = new Bundle();
                bundle.putString("SearchKeyword", query);
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SearchActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Bundle bundle = new Bundle();
                bundle.putString("SearchKeyword", s);
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SearchActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                loadHistory(query);
                return true;
            }

            private void loadHistory(String query) {
                SQLiteNovel db = new SQLiteNovel(MainActivity.this);
                final SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
                search.setSuggestionsAdapter(new RecentSearchAdapter(MainActivity.this, db.getLastQueryHistory(100,query)));
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        selectItem(id);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(NovelAPI.TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(MainActivity.this, getString(R.string.login_fail), Toast.LENGTH_LONG).show();
    }


    class NovelPagerAdapter extends FragmentPagerAdapter {
        public NovelPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment kk = new Fragment();
            if (position == 0) {
                kk = CategoryListFragment.newInstance(MainActivity.this);
            } else if (position == 1) {
                kk = new GridGplayFragment().newInstance();
            } else if (position == 2) {
                kk = IndexNovelFragment.newInstance(IndexNovelFragment.LATEST_NOVEL);
            } else if (position == 3) {
                kk = IndexNovelFragment.newInstance(IndexNovelFragment.NEW_UPLOADED);
            }else if (position == 4) {
                kk = IndexNovelFragment.newInstance(IndexNovelFragment.WEEK_NOVEL);
            } else if (position == 5) {
                kk = IndexNovelFragment.newInstance(IndexNovelFragment.MONTH_NOVEL);
            } else if (position == 6) {
                kk = IndexNovelFragment.newInstance(IndexNovelFragment.HOT_NOVEL);
            }
            return kk;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length];
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (pager.getCurrentItem() == 1) {
                finish();
            } else {
                pager.setCurrentItem(1, true);
            }
        }
    }

    private void setAboutUsDialog() {
        // TODO Auto-generated method stub
        aboutUsDialog = new Builder(this).setTitle(getResources().getString(R.string.about_us_string)).setIcon(R.drawable.play_store_icon)
                .setMessage(getResources().getString(R.string.about_us))
                .setPositiveButton(getResources().getString(R.string.yes_string), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
    }

    private void selectItem(int menuId) {
        switch (menuId) {
            case R.id.navigation_backup:
                new BackupInfoTask(BackupInfoTask.BACKUP).execute();
                break;
            case R.id.navigation_restore:
                new BackupInfoTask(BackupInfoTask.RESTORE).execute();
                break;
            case R.id.navigation_setting:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.navigation_bookmark:
                Intent bookmarkIntent = new Intent();
                bookmarkIntent.putExtra("IS_RECNET", false);
                bookmarkIntent.setClass(MainActivity.this, BookmarkActivity.class);
                startActivity(bookmarkIntent);
                break;
            case R.id.navigation_recent:
                Intent recent_intent = new Intent();
                recent_intent.putExtra("IS_RECNET", true);
                recent_intent.setClass(MainActivity.this, BookmarkActivity.class);
                startActivity(recent_intent);
                break;
            case R.id.nav_setting:
                Intent collectIntent = new Intent(MainActivity.this, MyNovelActivity.class);
                startActivity(collectIntent);
                break;
            case R.id.navigation_download:
                Intent downloadIntent = new Intent(MainActivity.this, MyNovelActivity.class);
                downloadIntent.putExtra("noti", true);
                startActivity(downloadIntent);
                break;
            case R.id.navigation_profile:
                aboutUsDialog.show();
                break;
            case R.id.navigation_like:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.recommend_url)));
                startActivity(browserIntent);
                break;
            case R.id.navigation_voice:
                Report.createReportDialog(this, this.getResources().getString(R.string.report_not_novel_problem), this.getResources().getString(R.string.report_not_article_problem));
                break;
            case R.id.navigation_recommend_novel:
                RecommendNovelDialog.createReportDialog(this);
                break;
            case R.id.buy_year_subscription:
                Intent intent1 = new Intent();
                intent1.setClass(this, DonateActivity.class);
                startActivity(intent1);
                break;
            case R.id.navigation_cloud_update:
                new CheckUpdateInfoTask().execute();
                break;
            case R.id.navigation_official_website:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://novelking.cc"));
                startActivity(browserIntent);
                break;
        }

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    public class CreateUserTask extends AsyncTask{

        private boolean result = false;
        private ProgressDialog progressdialogInit;
        private final DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                finish();
            }
        };
        @Override
        protected void onPreExecute() {
            progressdialogInit = ProgressDialog.show(MainActivity.this, "Load", "Loading…");
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
            result = NovelAPI.sendUserEmail(email);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                if (progressdialogInit != null && progressdialogInit.isShowing())
                    progressdialogInit.dismiss();
            }catch (Exception e){

            }
            if(result == false) {
                signOut();
                Toast.makeText(MainActivity.this, getString(R.string.login_fail), Toast.LENGTH_LONG).show();
            }else {
                Setting.saveSetting("email", email, MainActivity.this);
                drawNavigationServerPartAndSignInBtn(true);
            }
        }
    }

    public class UpdateInfo{
        public String updateLink = "";
        public int newest_version;
    }

    public class CheckUpdateInfoTask extends AsyncTask{

        boolean needUpdate = false;
        int device_version;
        UpdateInfo info = new UpdateInfo();

        private ProgressDialog progressdialogInit;
        private final DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                finish();
            }
        };
        @Override
        protected void onPreExecute() {
            progressdialogInit = ProgressDialog.show(MainActivity.this, "Load", "Loading…");
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
            device_version = getDeviceVersionCode();
            NovelAPI.getNewestVersionAndLink(info);
            needUpdate = isNeedUpdate(info, device_version);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                if (progressdialogInit != null && progressdialogInit.isShowing())
                    progressdialogInit.dismiss();

                if (needUpdate)
                    showUpdateDialog(device_version, info);
                else
                    showAlreadyUpdateDialog();
            }catch (Exception e){

            }
        }
    }

    public class AppOpenCheckUpdateTask extends AsyncTask{

        boolean needUpdate = false;
        int device_version;
        UpdateInfo info = new UpdateInfo();


        @Override
        protected Object doInBackground(Object[] objects) {
            device_version = getDeviceVersionCode();
            NovelAPI.getNewestVersionAndLink(info);
            needUpdate = isNeedUpdate(info, device_version);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                if (needUpdate)
                    showUpdateDialog(device_version, info);
            }catch (Exception e){

            }
        }

    }

    private boolean isNeedUpdate(UpdateInfo info, int device_version) {
        if(info.newest_version > device_version)
            return true;
        else
            return false;
    }

    private int getDeviceVersionCode() {
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo.versionCode;
    }

    private void showUpdateDialog(int device_version, final UpdateInfo info) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.check_update));
        alertDialogBuilder.setMessage(getString(R.string.need_updated,device_version,info.newest_version)).setPositiveButton(getString(R.string.download_update_app), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.updateLink));
                startActivity(browserIntent);
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showAlreadyUpdateDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.check_update));
        alertDialogBuilder.setMessage(getString(R.string.already_updated)).setPositiveButton(getString(R.string.yes_string),new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showRestoreDialog(String title, NovelAPI.BackupInfo result, DialogInterface.OnClickListener positiveListener, boolean setNegative) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.backupinfo_dialog, null);
        dialogBuilder.setView(dialogView);

        final TextView emailText = (TextView) dialogView.findViewById(R.id.email);
        final TextView collectNovelsText = (TextView) dialogView.findViewById(R.id.collect_novels);
        final TextView downloadNovelsText = (TextView) dialogView.findViewById(R.id.download_novels);
        final TextView updateText = (TextView) dialogView.findViewById(R.id.updated);

        int textLanguage = Setting.getSettingInt(Setting.keyTextLanguage, MainActivity.this);
        if (textLanguage == 1) {
            try {
                result.collects = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(result.collects);
                result.downloads = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(result.downloads);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        emailText.setText(result.email);
        collectNovelsText.setText(result.collects);
        downloadNovelsText.setText(result.downloads);
        updateText.setText(result.updated);

        dialogBuilder.setTitle(title);
        dialogBuilder.setPositiveButton(getString(R.string.yes_string), positiveListener);

        if(setNegative)
            dialogBuilder.setNegativeButton(getString(R.string.report_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //pass
                }
            });

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void showRestoreFinishDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setMessage(getString(R.string.restore_finish)).setPositiveButton(getString(R.string.yes_string), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public class BackupTask extends AsyncTask{

        private boolean result = false;
        private ProgressDialog progressdialogInit;
        private final DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                finish();
            }
        };
        @Override
        protected void onPreExecute() {
            progressdialogInit = ProgressDialog.show(MainActivity.this, "Load", "Loading…");
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
            result = NovelAPI.BackupToServer(email, MainActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                if (progressdialogInit != null && progressdialogInit.isShowing())
                    progressdialogInit.dismiss();
            }catch (Exception e){

            }
            if(result == false)
                Toast.makeText(MainActivity.this, getString(R.string.backup_fail), Toast.LENGTH_LONG).show();
            else {
                new BackupInfoTask(BackupInfoTask.BACKUP_FINISH).execute();
            }
        }
    }

    public class BackupInfoTask extends AsyncTask{


        public BackupInfoTask(int dialogCase) {
            super();
            this.dialogCase = dialogCase;
        }
        private static final int RESTORE = 1;
        private static final int BACKUP = 2;
        private static final int BACKUP_FINISH = 3;

        private int dialogCase;
        private ProgressDialog progressdialogInit;
        private final DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                finish();
            }
        };
        private NovelAPI.BackupInfo result;

        @Override
        protected void onPreExecute() {
            progressdialogInit = ProgressDialog.show(MainActivity.this, "Load", "Loading…");
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
            result = NovelAPI.getUserBackupInfo(MainActivity.this,email);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                if (progressdialogInit != null && progressdialogInit.isShowing())
                    progressdialogInit.dismiss();
            }catch (Exception e){

            }
            switch (dialogCase){
                case RESTORE:
                    if(result.collects.equals("尚未備份"))
                        showRestoreDialog(getString(R.string.restore),result,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        },false);
                    else
                        showRestoreDialog(getString(R.string.restore),result,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                new RestoreTask().execute();
                                dialog.cancel();
                            }
                        },true);
                    break;
                case BACKUP:
                    showRestoreDialog(getString(R.string.backup),result,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            new BackupTask().execute();
                            dialog.cancel();
                        }
                    },true);
                    break;
                case BACKUP_FINISH:
                    showRestoreDialog(getString(R.string.backup_finish),result,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    },false);
                    break;
            }
        }
    }

    public class RestoreTask extends AsyncTask{


        private ProgressDialog progressdialogInit;
        private final DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                finish();
            }
        };
        private NovelAPI.RestoreResult result;

        @Override
        protected void onPreExecute() {
            progressdialogInit = ProgressDialog.show(MainActivity.this, "Load", "Loading…");
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
            result = NovelAPI.restoreFromUserBackup(email, MainActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                if (progressdialogInit != null && progressdialogInit.isShowing())
                    progressdialogInit.dismiss();
            }catch (Exception e){

            }
            if(result.result == false)
                Toast.makeText(MainActivity.this, result.message, Toast.LENGTH_LONG).show();
            else {
                showRestoreFinishDialog();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(NovelAPI.TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            email = acct.getEmail();
            String saveEmail = Setting.getSettingString("email",MainActivity.this);
            if(saveEmail!=null && saveEmail.equals(email)){
                Log.d(NovelAPI.TAG, "Do not run CreateUserTask:");
                drawNavigationServerPartAndSignInBtn(true);
            }else{
                Log.d(NovelAPI.TAG, "CreateUserTask:");
                new CreateUserTask().execute();
            }
        } else {
            // Signed out, show unauthenticated UI.
            drawNavigationServerPartAndSignInBtn(false);
        }
    }
}
