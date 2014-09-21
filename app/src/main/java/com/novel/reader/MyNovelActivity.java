package com.novel.reader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.ads.AdFragmentActivity;
import com.analytics.AnalyticsName;
import com.analytics.NovelReaderAnalyticsApp;
import com.android.slidingtab.SlidingTabLayout;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kosbrother.fragments.MyBookcaseFragment;
import com.kosbrother.fragments.MyDownloadFragment;
import com.kosbrother.tool.Report;
import com.novel.reader.util.Setting;

public class MyNovelActivity extends AdFragmentActivity {

    private static final int ID_SETTING = 0;
    private static final int ID_RESPONSE = 1;
    private static final int ID_ABOUT_US = 2;
    private static final int ID_GRADE = 3;
    private static final int ID_Report = 6;
    private String[] CONTENT;
    private AlertDialog.Builder aboutUsDialog;

    private ViewPager pager;
    private FragmentStatePagerAdapter adapter;

    private RelativeLayout bannerAdView;
    private ActionBar actionbar;
    private SlidingTabLayout mSlidingTabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Setting.setApplicationActionBarTheme(this);
        setContentView(R.layout.simple_titles);

        actionbar = getSupportActionBar();
        actionbar.setTitle(getResources().getString(R.string.title_my_bookcase));
        actionbar.setDisplayHomeAsUpEnabled(true);

        Resources res = getResources();
        CONTENT = res.getStringArray(R.array.collections);

        adapter = new NovelPagerAdapter(getSupportFragmentManager());

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("noti")) {
                pager.setCurrentItem(1);
            }
        }

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(pager);


        setAboutUsDialog();

        bannerAdView = (RelativeLayout) findViewById(R.id.adonView);
        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 0)
            mAdView = setBannerAdView(bannerAdView);

        trackScreen();
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
            t.setScreenName(AnalyticsName.MyNovelCollectedBookcaseFragment);
        } else if (position == 1) {
            t.setScreenName(AnalyticsName.MyNovelDownloadBookcaseFragment);
        }
        t.send(new HitBuilders.AppViewBuilder().build());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.activity_main, menu);

        menu.add(0, ID_SETTING, 0, getResources().getString(R.string.menu_settings)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_RESPONSE, 1, getResources().getString(R.string.menu_respond)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_ABOUT_US, 2, getResources().getString(R.string.menu_aboutus)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_GRADE, 3, getResources().getString(R.string.menu_recommend)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_Report, 6, getResources().getString(R.string.menu_report)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 0)
            menu.add(0, 7, 7, getResources().getString(R.string.buy_year_subscription)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);


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
                Intent intent = new Intent(MyNovelActivity.this, SettingActivity.class);
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
            case ID_Report:
                Report.createReportDialog(this, getResources().getString(R.string.report_not_novel_problem), getResources().getString(R.string.report_not_article_problem));
                break;
            case 7:
                Intent intent1 = new Intent();
                intent1.setClass(this, DonateActivity.class);
                startActivity(intent1);
                break;
        }
        return true;
    }

    class NovelPagerAdapter extends FragmentStatePagerAdapter {
        public NovelPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // 這邊要寫兩個不同的Fragment, 分別取書櫃資 & 下載資料
        @Override
        public Fragment getItem(int position) {
            Fragment kk = new Fragment();
            if (position == 0) {
                kk = MyBookcaseFragment.newInstance();
            } else if (position == 1) {
                kk = MyDownloadFragment.newInstance();
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

    @Override
    protected void onResume() {
        super.onResume();

        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 1)
            bannerAdView.setVisibility(View.GONE);
    }

}
