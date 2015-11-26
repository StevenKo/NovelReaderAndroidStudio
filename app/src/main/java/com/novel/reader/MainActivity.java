package com.novel.reader;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import com.ads.MopubAdFragmentActivity;
import com.analytics.AnalyticsName;
import com.analytics.NovelReaderAnalyticsApp;
import com.android.slidingtab.SlidingTabLayout;
import com.crashlytics.android.Crashlytics;
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
import android.provider.Settings;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends MopubAdFragmentActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final int ID_SETTING = 0;
    private static final int ID_RESPONSE = 1;
    private static final int ID_ABOUT_US = 2;
    private static final int ID_GRADE = 3;
    private static final int ID_SEARCH = 5;
    private static final int ID_Report = 6;

    private String[] CONTENT;
    private MenuItem itemSearch;
    private ViewPager pager;
    private Builder aboutUsDialog;
    MoPubInterstitial mInterstitial;


    //gcm
    public static final String EXTRA_MESSAGE = "message";


    /**
     * Substitute you own sender ID here.
     */
    String SENDER_ID = "1037018589447";
    private Context context;
    String regid;
    GoogleCloudMessaging gcm;

    private RelativeLayout bannerAdView;
    private SlidingTabLayout mSlidingTabLayout;



    private CharSequence mTitle;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        Setting.setApplicationActionBarTheme(this);

        setContentView(R.layout.layout_main);
        setTextLocale();
        setViewPagerAndSlidingTab();
        setAboutUsDialog();
        setNavigationDrawler();
        setLogIn();

        if (Setting.getSettingInt(Setting.keyUpdateAppVersion, this) < Setting.getAppVersion(this)) {
            showUpdateInfoDialog(this);
            Setting.saveSetting(Setting.keyUpdateAppVersion, Setting.getAppVersion(this), this);
        }

        context = getApplicationContext();
        regid = Setting.getRegistrationId(context);
        String device_id = Setting.getDeviceId(context);

        if (regid.length() == 0 || device_id.length() == 0) {
            registerBackground();
        }
        gcm = GoogleCloudMessaging.getInstance(this);
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

    private void setLogIn() {
        TextView logInEmail = (TextView)findViewById(R.id.log_in_email);
        Button logIn = (Button)findViewById(R.id.log_in);
        boolean isLogin = true;
        if(isLogin){
            logInEmail.setText("chunyuko85@gmail.com");
            logIn.setText(getText(R.string.logout));
            navigationView.getMenu().setGroupVisible(R.id.backup_group, true);
        }else{
            logInEmail.setText(getText(R.string.not_login));
            logIn.setText(getText(R.string.login));
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



    private void registerBackground() {
        new AsyncTask() {

            @Override
            protected String doInBackground(Object... params) {

                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration id=" + regid;
                    String deviceId = Settings.Secure.getString(MainActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);

                    boolean isRegistered = NovelAPI.sendRegistrationId(regid, deviceId, Locale.getDefault().getCountry(), getPackageManager().getPackageInfo(getPackageName(), 0).versionCode, "GooglePlay");

                    if (isRegistered)
                        setRegistrationId(context, regid, deviceId);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                } catch (NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return msg;
            }

            private void setRegistrationId(Context context, String regid, String deviceId) {
                final SharedPreferences prefs = Setting.getGCMPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Setting.PROPERTY_REG_ID, regid);
                editor.putString(Setting.PROPERTY_DEVICE_ID, deviceId);
                editor.putInt(Setting.PROPERTY_APP_VERSION, Setting.getAppVersion(context));
                editor.putLong(Setting.PROPERTY_ON_SERVER_EXPIRATION_TIME, Setting.REGISTRATION_EXPIRY_TIME_MS + System.currentTimeMillis());
                editor.commit();

            }

        }.execute(null, null, null);
    }


    private void selectItem(int menuId) {
        switch (menuId) {
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
}
