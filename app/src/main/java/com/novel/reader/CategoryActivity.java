package com.novel.reader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ads.MopubAdFragmentActivity;
import com.analytics.AnalyticsName;
import com.analytics.NovelReaderAnalyticsApp;
import com.android.slidingtab.SlidingTabLayout;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kosbrother.fragments.CategoryAllNovelsFragment;
import com.kosbrother.fragments.CategoryFinishFragment;
import com.kosbrother.fragments.CategoryLatestNovelsFragment;
import com.kosbrother.fragments.CategoryRecommendFragment;
import com.kosbrother.fragments.CategoryWeekFragment;
import com.kosbrother.fragments.CategroyHotNovelsFragment;
import com.kosbrother.tool.Report;
import com.novel.reader.util.Setting;

public class CategoryActivity extends MopubAdFragmentActivity {

    private static final int ID_SETTING = 0;
    private static final int ID_RESPONSE = 1;
    private static final int ID_ABOUT_US = 2;
    private static final int ID_GRADE = 3;
    private static final int ID_DOWNLOAD = 4;
    private static final int ID_SEARCH = 5;
    private static final int ID_Report = 6;

    private String[] CONTENT;
    private EditText search;
    private Bundle mBundle;
    private String categoryName;
    public static int categoryId;
    private MenuItem itemSearch;

    private AlertDialog.Builder aboutUsDialog;

    private RelativeLayout bannerAdView;
    private ActionBar actionbar;
    private ViewPager pager;
    private SlidingTabLayout mSlidingTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Setting.setApplicationActionBarTheme(this);
        setContentView(R.layout.simple_titles);

        mBundle = this.getIntent().getExtras();
        categoryName = mBundle.getString("CategoryName");
        categoryId = mBundle.getInt("CategoryId");

        actionbar = getSupportActionBar();
        actionbar.setTitle(categoryName);
        actionbar.setDisplayHomeAsUpEnabled(true);

        Resources res = getResources();
        CONTENT = res.getStringArray(R.array.category_sections);

        FragmentPagerAdapter adapter = new NovelPagerAdapter(getSupportFragmentManager());

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(pager);

        setAboutUsDialog();

        bannerAdView = (RelativeLayout) findViewById(R.id.adonView);
        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 0)
            moPubView = setBannerAdView(bannerAdView);

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
            t.setScreenName(AnalyticsName.CategroyHotNovelsFragment);
        } else if (position == 1) {
            t.setScreenName(AnalyticsName.CategoryRecommendFragment);
        } else if (position == 2) {
            t.setScreenName(AnalyticsName.CategoryWeekFragment);
        } else if (position == 3) {
            t.setScreenName(AnalyticsName.CategoryLatestNovelsFragment);
        } else if (position == 4) {
            t.setScreenName(AnalyticsName.CategoryFinishFragment);
        } else if (position == 5) {
            t.setScreenName(AnalyticsName.CategoryAllNovelsFragment);
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

        itemSearch = menu.add(0, ID_SEARCH, 4, getResources().getString(R.string.menu_search)).setIcon(R.drawable.ic_search_inverse)
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
                                    intent.setClass(CategoryActivity.this, SearchActivity.class);
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
                }).setActionView(R.layout.collapsible_edittext);
        itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

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
                Intent intent = new Intent(CategoryActivity.this, SettingActivity.class);
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
                break;
            case ID_Report:
                Report.createReportDialog(this, this.getResources().getString(R.string.report_not_novel_problem), this.getResources().getString(R.string.report_not_article_problem));
                break;
            case 7:
                Intent intent1 = new Intent();
                intent1.setClass(this, DonateActivity.class);
                startActivity(intent1);
                break;

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
                kk = CategroyHotNovelsFragment.newInstance(categoryName);
            } else if (position == 1) {
                kk = CategoryRecommendFragment.newInstance(categoryName);
            } else if (position == 2) {
                kk = CategoryWeekFragment.newInstance(categoryName);
            } else if (position == 3) {
                kk = CategoryLatestNovelsFragment.newInstance(categoryName);
            } else if (position == 4) {
                kk = CategoryFinishFragment.newInstance(categoryName);
            } else if (position == 5) {
                kk = CategoryAllNovelsFragment.newInstance(categoryName);
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
    protected void onResume() {
        super.onResume();
        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 1)
            bannerAdView.setVisibility(View.GONE);
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