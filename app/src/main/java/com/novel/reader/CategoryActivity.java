package com.novel.reader;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

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
import com.novel.db.SQLiteNovel;
import com.novel.reader.adapter.RecentSearchAdapter;
import com.novel.reader.util.Setting;

public class CategoryActivity extends MopubAdFragmentActivity {

    private String[] CONTENT;
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
                if(!queryTextFocused) {
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
                intent.setClass(CategoryActivity.this, SearchActivity.class);
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
                intent.setClass(CategoryActivity.this, SearchActivity.class);
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
                SQLiteNovel db = new SQLiteNovel(CategoryActivity.this);
                final SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
                search.setSuggestionsAdapter(new RecentSearchAdapter(CategoryActivity.this, db.getLastQueryHistory(100,query)));
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
                Intent intent = new Intent(CategoryActivity.this, SettingActivity.class);
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