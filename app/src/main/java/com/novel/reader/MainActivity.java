package com.novel.reader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ads.MopubAdFragmentActivity;
import com.analytics.AnalyticsName;
import com.analytics.NovelReaderAnalyticsApp;
import com.android.slidingtab.SlidingTabLayout;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kosbrother.fragments.CategoryListFragment;
import com.kosbrother.fragments.GridGplayFragment;
import com.kosbrother.fragments.IndexNovelFragment;
import com.kosbrother.tool.RecommendNovelDialog;
import com.kosbrother.tool.Report;
import com.mopub.mobileads.MoPubInterstitial;
import com.novel.db.SQLiteNovel;
import com.novel.navigationdrawler.NavigationListAdapter;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.util.NovelReaderUtil;
import com.novel.reader.util.Setting;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends MopubAdFragmentActivity {

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


    //navigationdrawler
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;


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
            t.setScreenName(AnalyticsName.IndexFragmentMap.get(IndexNovelFragment.WEEK_NOVEL));
        } else if (position == 4) {
            t.setScreenName(AnalyticsName.IndexFragmentMap.get(IndexNovelFragment.MONTH_NOVEL));
        } else if (position == 5) {
            t.setScreenName(AnalyticsName.IndexFragmentMap.get(IndexNovelFragment.HOT_NOVEL));
        }
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    private void setCheckCollectNovelsAlarm() {
        NovelReaderUtil.dailyCollectNovelsAlarmSetup(this);
    }

    private void setNavigationDrawler() {
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setAdapter(new NavigationListAdapter(this));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

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

        Builder a = new Builder(mActivity).setTitle(mActivity.getResources().getString(R.string.update)).setIcon(R.drawable.ic_stat_notify)
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
    public boolean onCreateOptionsMenu(Menu menu) {

        itemSearch = menu.add(0, ID_SEARCH, 4, getResources().getString(R.string.menu_search)).setIcon(R.drawable.ic_search_inverse).setActionView(R.layout.collapsible_edittext)
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
                                    Bundle bundle = new Bundle();
                                    bundle.putString("SearchKeyword", v.getText().toString());
                                    Intent intent = new Intent();
                                    intent.setClass(MainActivity.this, SearchActivity.class);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    itemSearch.collapseActionView();
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
                        // TODO Auto-generated method stub
                        search.setText("");
                        return true;
                    }
                });
        itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
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
                kk = IndexNovelFragment.newInstance(IndexNovelFragment.WEEK_NOVEL);
            } else if (position == 4) {
                kk = IndexNovelFragment.newInstance(IndexNovelFragment.MONTH_NOVEL);
            } else if (position == 5) {
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
        if (pager.getCurrentItem() == 1) {
            finish();
        } else {
            pager.setCurrentItem(1, true);
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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        switch (position) {
            case 0:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case 1:
                Intent bookmarkIntent = new Intent();
                bookmarkIntent.putExtra("IS_RECNET", false);
                bookmarkIntent.setClass(MainActivity.this, BookmarkActivity.class);
                startActivity(bookmarkIntent);
                break;
            case 2:
                Intent recent_intent = new Intent();
                recent_intent.putExtra("IS_RECNET", true);
                recent_intent.setClass(MainActivity.this, BookmarkActivity.class);
                startActivity(recent_intent);
                break;
            case 3:
                Intent collectIntent = new Intent(MainActivity.this, MyNovelActivity.class);
                startActivity(collectIntent);
                break;
            case 4:
                Intent downloadIntent = new Intent(MainActivity.this, MyNovelActivity.class);
                downloadIntent.putExtra("noti", true);
                startActivity(downloadIntent);
                break;
            case 5:
                aboutUsDialog.show();
                break;
            case 6:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.recommend_url)));
                startActivity(browserIntent);
                break;
            case 7:
                Report.createReportDialog(this, this.getResources().getString(R.string.report_not_novel_problem), this.getResources().getString(R.string.report_not_article_problem));
                break;
            case 8:
                RecommendNovelDialog.createReportDialog(this);
                break;
            case 9:
                Intent intent1 = new Intent();
                intent1.setClass(this, DonateActivity.class);
                startActivity(intent1);
                break;
            case 10:
                new CheckUpdateInfoTask().execute();
                break;
            case 11:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://novelking.cc"));
                startActivity(browserIntent);
                break;
        }
        mDrawerLayout.closeDrawer(mDrawerList);

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
