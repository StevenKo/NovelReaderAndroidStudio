package com.novel.reader;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.ads.AdInterstitialManager;
import com.ads.MopubAdFragmentActivity;
import com.analytics.AnalyticsName;
import com.analytics.NovelReaderAnalyticsApp;
import com.kosbrother.tool.DetectScrollView;
import com.kosbrother.tool.DetectScrollView.DetectScrollViewListener;
import com.kosbrother.tool.Report;
import com.mopub.mobileads.MoPubInterstitial;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.Article;
import com.novel.reader.entity.Bookmark;
import com.novel.reader.entity.Novel;
import com.novel.reader.util.Setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArticleActivity extends MopubAdFragmentActivity implements DetectScrollViewListener {

    private static final int ID_SETTING = 0;
    private static final int ID_Bookmark = 4;
    private static final int ID_Report = 5;
    private static final int ID_MODE = 6;
    private static final int ID_FONT_SIZE = 7;
    private static final int ID_CONTENTS = 8;
    private static final int ID_NOVEL = 9;

    private int textSize;
    private int textLanguage;                                    // 0 for 繁體, 1 for 簡體
    private int clickToNextPage;                                 // 0 for yes, 1 for no
    private int stopSleeping;                                    // 0 for yes, 1 for no
    private String textMode;

    private TextView articleTextView;
    private DetectScrollView articleScrollView;
    private Button articleButtonUp;
    private Button articleButtonDown;
    private TextView articlePercent;
    private Article myArticle;                                        // uset to get article text
    private Article theGottenArticle;
    private Boolean downloadBoolean;
    private Bundle mBundle;
    private String novelName;
    private String articleTitle;
    private int articleId = -1;
    private String novelPic;
    private int novelId;
    private int yRate = -1;
    private int ariticlePosition = -1;
    private ArrayList<Integer> articleIDs;
    // private ProgressDialog progressDialog= null;
    private ActionBar ab;
    private LinearLayout layoutProgress;
    private int currentY = 0;
    private int articleNum = -1;
    private ArrayList<Integer> articleNums;
    private WebView articleWebView;
    private LinearLayout articleLayout;
    private int articleAdType;

    private RelativeLayout bannerAdView;
    private TextView articleTitleTextView;
    private ImageView bookmarkImage;
    private ImageView novelImage;
    private boolean adHasShowed = false;
    private AdInterstitialManager interstitialManager;
    private FrameLayout mAdView2;
    private FrameLayout mAdView1;

    private static final String ADMOB_AD_UNIT_ID = "ca-app-pub-5774496584789671/5182575586";
    private static final String ADMOB_APP_ID = "ca-app-pub-5774496584789671~4050055547";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_article);

        restorePreValues();
        setViews();

        MobileAds.initialize(this, ADMOB_APP_ID);

        ab = getSupportActionBar();
        mBundle = this.getIntent().getExtras();
        novelName = mBundle.getString("NovelName");
        novelPic = mBundle.getString("NovelPic");
        novelId = mBundle.getInt("NovelId");

        if (myArticle == null) {
            articleTitle = mBundle.getString("ArticleTitle");
            articleId = mBundle.getInt("ArticleId");
            downloadBoolean = mBundle.getBoolean("ArticleDownloadBoolean", false);
            yRate = mBundle.getInt("ReadingRate", 0);
            articleIDs = mBundle.getIntegerArrayList("ArticleIDs");
            ariticlePosition = mBundle.getInt("ArticlePosition");
            articleNum = mBundle.getInt("ArticleNum");
            articleNums = mBundle.getIntegerArrayList("ArticleNums");
            if (savedInstanceState != null) {
                getSavedState(savedInstanceState);
            }

            if (articleIDs != null && articleIDs.size() > 0 && ariticlePosition <= articleIDs.size() - 1) {
                if (downloadBoolean) {
                    myArticle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", true, articleNums.get(ariticlePosition));
                } else {
                    myArticle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", false, articleNums.get(ariticlePosition));
                }
            } else {
                if (downloadBoolean) {
                    myArticle = new Article(articleId, novelId, "", articleTitle, "", true, articleNum);
                } else {
                    myArticle = new Article(articleId, novelId, "", articleTitle, "", false, articleNum);
                }
            }
            new DownloadArticleTask().execute();
        }

        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        setArticleTitle(articleTitle);

        // ab.setTitle(novelName);
        ab.setDisplayHomeAsUpEnabled(true);

        trackScreen();

        interstitialManager = new AdInterstitialManager(this);
        articleAdType = Setting.getSettingInt(Setting.keyArticleAdType, ArticleActivity.this);
        if (articleAdType == Setting.BannerAd) {
            ((RelativeLayout) findViewById(R.id.adonView)).setVisibility(View.VISIBLE);
        } else
            ((RelativeLayout) findViewById(R.id.adonView)).setVisibility(View.GONE);
        if (articleAdType == Setting.InterstitialAd && Setting.getSettingInt(Setting.keyYearSubscription, ArticleActivity.this) == 0 && !adHasShowed) {
            requestInterstitialAd();
            adHasShowed = false;
        }
    }

    private void trackScreen() {
        Tracker t = ((NovelReaderAnalyticsApp) getApplication()).getTracker(NovelReaderAnalyticsApp.TrackerName.APP_TRACKER);
        t.setScreenName(AnalyticsName.ArticleActivity);
        t.send(new HitBuilders.AppViewBuilder().build());
    }


    public void getSavedState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey("ArticleTitle"))
            articleTitle = savedInstanceState.getString("ArticleTitle");
        if (savedInstanceState.containsKey("ArticleId"))
            articleId = savedInstanceState.getInt("ArticleId");
        if (savedInstanceState.containsKey("ArticleDownloadBoolean"))
            downloadBoolean = savedInstanceState.getBoolean("ArticleDownloadBoolean", false);
        if (savedInstanceState.containsKey("ReadingRate"))
            yRate = savedInstanceState.getInt("ReadingRate", 0);
        if (savedInstanceState.containsKey("ArticleIDs"))
            articleIDs = savedInstanceState.getIntegerArrayList("ArticleIDs");
        if (savedInstanceState.containsKey("ArticlePosition"))
            ariticlePosition = savedInstanceState.getInt("ArticlePosition");
        if (savedInstanceState.containsKey("ArticleNum"))
            articleNum = savedInstanceState.getInt("ArticleNum");
        if (savedInstanceState.containsKey("ArticleNums"))
            articleNums = savedInstanceState.getIntegerArrayList("ArticleNums");
        if (savedInstanceState.getBoolean("AdHasShowed"))
            adHasShowed = savedInstanceState.getBoolean("AdHasShowed");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        NovelAPI.createRecentBookmark(new Bookmark(0, myArticle.getNovelId(), myArticle.getId(), yRate, novelName, myArticle.getTitle(), novelPic, true),
                ArticleActivity.this);
        savedInstanceState.putString("ArticleTitle", myArticle.getTitle());
        savedInstanceState.putInt("ArticleId", myArticle.getId());
        savedInstanceState.putBoolean("ArticleDownloadBoolean", myArticle.isDownload());
        savedInstanceState.putInt("ReadingRate", yRate);
        savedInstanceState.putIntegerArrayList("ArticleIDs", articleIDs);
        savedInstanceState.putInt("ArticlePosition", ariticlePosition);
        savedInstanceState.putInt("ArticleNum", myArticle.getNum());
        savedInstanceState.putIntegerArrayList("ArticleNums", articleNums);
        savedInstanceState.putBoolean("AdHasShowed", true);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setArticleTitle(String articleTitle) {
        articleTitleTextView.setText(novelName + ":" + articleTitle);
    }

    private void restorePreValues() {

        textSize = Setting.getSettingInt(Setting.keyTextSize, ArticleActivity.this);
        textMode = Setting.getSettingString(Setting.keyMode, ArticleActivity.this);
        textLanguage = Setting.getSettingInt(Setting.keyTextLanguage, ArticleActivity.this);
        clickToNextPage = Setting.getSettingInt(Setting.keyClickToNextPage, ArticleActivity.this);
        stopSleeping = Setting.getSettingInt(Setting.keyStopSleeping, ArticleActivity.this);

        if (stopSleeping == 0) {
            ArticleActivity.this.findViewById(android.R.id.content).setKeepScreenOn(true);
        }
    }

    private void setViews() {

        layoutProgress = (LinearLayout) findViewById(R.id.layout_progress);
        articleTextView = (TextView) findViewById(R.id.article_text);
        articleWebView = (WebView) findViewById(R.id.article_webview);
        articleScrollView = (DetectScrollView) findViewById(R.id.article_scrollview);
        articleButtonUp = (Button) findViewById(R.id.article_button_up);
        articleButtonDown = (Button) findViewById(R.id.article_button_down);
        articlePercent = (TextView) findViewById(R.id.article_percent);
        articleLayout = (LinearLayout) findViewById(R.id.article_layout);
        articleTitleTextView = (TextView) findViewById(R.id.article_title);
        bookmarkImage = (ImageView) findViewById(R.id.bookmarkImage);
        novelImage = (ImageView) findViewById(R.id.novelImage);

        articleScrollView.setScrollViewListener(ArticleActivity.this);

        articleTextView.setTextSize(textSize);
        articleTextView.setTextColor(Setting.getBackgroundModeTextColor(textMode, this));
        articleLayout.setBackgroundColor(Setting.getBackgroundModeBackgroundColor(textMode, this));
        layoutProgress.setBackgroundColor(Setting.getBackgroundModeBackgroundColor(textMode, this));

        mAdView1 = (FrameLayout) findViewById(R.id.adView1);
        mAdView2 = (FrameLayout) findViewById(R.id.adView2);
        mAdView1.setVisibility(View.GONE);
        mAdView2.setVisibility(View.GONE);


        articleButtonUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (articleAdType == Setting.InterstitialAd && Setting.getSettingInt(Setting.keyYearSubscription, ArticleActivity.this) == 0) {
                    requestInterstitialAd();
                    adHasShowed = false;
                }
                previousPage();
            }
        });

        articleButtonDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (articleAdType == Setting.InterstitialAd && Setting.getSettingInt(Setting.keyYearSubscription, ArticleActivity.this) == 0 ) {
                    requestInterstitialAd();
                    adHasShowed = false;
                }
                nextPage();
            }
        });

        if (clickToNextPage == 0) {
            articleTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // new GetNextArticleTask().execute();
                    int kk = articleScrollView.getHeight();
                    int tt = articleTextView.getHeight();
                    articleScrollView.scrollTo(0, currentY + kk - 8);
                }
            });
        }

    }


    private void updateBookmark() {
        Bookmark bookmark = NovelAPI.findBookMarkByArticle(myArticle, ArticleActivity.this);
        if (bookmark != null) {
            bookmarkImage.setVisibility(View.VISIBLE);
        } else {
            bookmarkImage.setVisibility(View.GONE);
        }
    }


    @Override
    public void onScrollChanged(DetectScrollView scrollView, int x, int y, int oldx, int oldy) {
        int kk = articleScrollView.getHeight();
        int tt = articleTextView.getHeight();

        currentY = y;
        yRate = (int) (((double) (y) / (double) (tt)) * 100);
        int xx = (int) (((double) (y) / (double) (tt - kk)) * 100);
        if (xx > 100 || xx < 0)
            xx = 100;
        String yPositon = Integer.toString(xx);
        articlePercent.setText(yPositon + "%");
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, ID_MODE, 0, "日間模式").setIcon(getModeIcon()).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, ID_FONT_SIZE, 1, "字型大小").setIcon(getFontSizeIcon()).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, ID_CONTENTS, 2, "目錄").setIcon(getContentsIcon()).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, ID_Bookmark, 3, getResources().getString(R.string.menu_add_bookmark)).setIcon(R.drawable.article_bookmark_white).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(0, ID_NOVEL, 4, getResources().getString(R.string.menu_collect_novel)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_SETTING, 5, getResources().getString(R.string.my_read_setting)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_Report, 6, getResources().getString(R.string.menu_article_report)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        return true;
    }


    private int getModeIcon() {
        return R.drawable.article_sun_white;
    }

    private int getFontSizeIcon() {
        return R.drawable.article_font_size_white;
    }

    private int getContentsIcon() {
        return R.drawable.article_contents_white;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setBookMarkItem(menu);
        setCollectNovelItem(menu);

        return super.onPrepareOptionsMenu(menu);
    }

    private void setCollectNovelItem(Menu menu) {
        if (NovelAPI.isNovelCollected(this, novelId)) {
            novelImage.setVisibility(View.VISIBLE);
            menu.findItem(ID_NOVEL).setTitle(getResources().getString(R.string.menu_remove_collect_novel));
        } else {
            novelImage.setVisibility(View.GONE);
            menu.findItem(ID_NOVEL).setTitle(getResources().getString(R.string.menu_collect_novel));
        }
    }


    private void setBookMarkItem(Menu menu) {
        Bookmark bookmark = NovelAPI.findBookMarkByArticle(myArticle, ArticleActivity.this);
        if (bookmark != null) {
            bookmarkImage.setVisibility(View.VISIBLE);
            menu.findItem(ID_Bookmark).setIcon(R.drawable.article_bookmark);
        } else {
            bookmarkImage.setVisibility(View.GONE);
            menu.findItem(ID_Bookmark).setIcon(R.drawable.article_bookmark_white);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
            case ID_SETTING: // setting
                Intent intent = new Intent(ArticleActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case ID_Bookmark:
                Bookmark bookmark = NovelAPI.findBookMarkByArticle(myArticle, ArticleActivity.this);
                if (bookmark != null) {
                    NovelAPI.deleteBookmark(bookmark, ArticleActivity.this);
                    Toast.makeText(ArticleActivity.this, getResources().getString(R.string.menu_delete_bookmark), Toast.LENGTH_SHORT).show();
                } else {
                    NovelAPI.insertBookmark(new Bookmark(0, myArticle.getNovelId(), myArticle.getId(), yRate, novelName, myArticle.getTitle(), novelPic, false), ArticleActivity.this);
                    Toast.makeText(ArticleActivity.this, getResources().getString(R.string.menu_add_bookmark), Toast.LENGTH_SHORT).show();
                }
                invalidateOptionsMenu();
                break;
            case ID_Report:
                Report.createReportDialog(this, novelName + "(" + novelId + ")", myArticle.getTitle() + "(Num:" + myArticle.getNum() + ")");
                break;
            case ID_MODE:
                showModeDialog();
                break;
            case ID_NOVEL:
                if (NovelAPI.isNovelCollected(this, novelId)) {
                    Novel theNovel = NovelAPI.getNovel(novelId, this);
                    NovelAPI.removeNovelFromCollected(theNovel, this);
                    Toast.makeText(ArticleActivity.this, getResources().getString(R.string.menu_remove_collect_novel), Toast.LENGTH_SHORT).show();
                    invalidateOptionsMenu();
                } else {
                    new AsyncTask() {
                        Novel theNovel;

                        @Override
                        protected Object doInBackground(Object... arg0) {
                            theNovel = NovelAPI.getNovel(novelId, ArticleActivity.this);
                            NovelAPI.collecNovel(theNovel, ArticleActivity.this);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object result) {
                            super.onPostExecute(result);
                            Toast.makeText(ArticleActivity.this, getResources().getString(R.string.menu_collect_novel), Toast.LENGTH_SHORT).show();
                            invalidateOptionsMenu();

                            trackCollectNovel();
                        }

                        private void trackCollectNovel() {
                            Tracker t = ((NovelReaderAnalyticsApp) getApplication()).getTracker(NovelReaderAnalyticsApp.TrackerName.APP_TRACKER);
                            t.send(new HitBuilders.EventBuilder().setCategory(AnalyticsName.Collect).setAction(theNovel.getName()).build());
                        }
                    }.execute();


                }
                break;
            case ID_FONT_SIZE:
                showFontSizeDialog();
                break;
            case ID_CONTENTS:
                Intent intentContents = new Intent(ArticleActivity.this, NovelContentsActivity.class);
                intentContents.putExtra("NovelName", novelName);
                intentContents.putExtra("NovelId", novelId);
                intentContents.putExtra("ArticleId", myArticle.getId());
                startActivityForResult(intentContents, 1);
                break;
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            int resultArticleId = data.getIntExtra("SelectArticleId", 0);
            int resultPosition = data.getIntExtra("SelectArticlePosition", 0);
            if (requestCode == 1 && resultCode == RESULT_OK && resultArticleId != 0 && resultPosition != 0) {
                myArticle.id = resultArticleId;
                ariticlePosition = resultPosition;
                new UpdateArticleTask().execute();
            }
        }
    }


    private void showFontSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence[] array = getResources().getStringArray(R.array.fontsize_selection);
        builder.setTitle(getResources().getString(R.string.fontsize_setting_title)).setSingleChoiceItems(array, -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int position) {
                switch (position) {
                    case 0:
                        articleTextView.setTextSize(28);
                        Setting.saveSetting(Setting.keyTextSize, 28, ArticleActivity.this);
                        break;
                    case 1:
                        articleTextView.setTextSize(20);
                        Setting.saveSetting(Setting.keyTextSize, 20, ArticleActivity.this);
                        break;
                    case 2:
                        articleTextView.setTextSize(14);
                        Setting.saveSetting(Setting.keyTextSize, 12, ArticleActivity.this);
                        break;
                    case 3:
                        Intent intent = new Intent(ArticleActivity.this, SettingActivity.class);
                        startActivity(intent);
                        break;
                }
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    private void showModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence[] array = getResources().getStringArray(R.array.mode_selection);
        builder.setTitle(getResources().getString(R.string.mode_setting_title)).setSingleChoiceItems(array, Setting.getTextModePosition(textMode), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int position) {
                switch (position) {
                    case 0:
                        articleLayout.setBackgroundColor(Setting.getBackgroundModeBackgroundColor(Setting.keySunMode, ArticleActivity.this));
                        articleTextView.setTextColor(Setting.getBackgroundModeTextColor(Setting.keySunMode, ArticleActivity.this));
                        textMode = Setting.keySunMode;
                        Setting.saveSetting(Setting.keyMode, Setting.keySunMode, ArticleActivity.this);
                        setBottomButtonsColor();
                        break;
                    case 1:
                        articleLayout.setBackgroundColor(Setting.getBackgroundModeBackgroundColor(Setting.keyMoonMode, ArticleActivity.this));
                        articleTextView.setTextColor(Setting.getBackgroundModeTextColor(Setting.keyMoonMode, ArticleActivity.this));
                        textMode = Setting.keyMoonMode;
                        Setting.saveSetting(Setting.keyMode, Setting.keyMoonMode, ArticleActivity.this);
                        setBottomButtonsColor();
                        break;
                    case 2:
                        Intent intent = new Intent(ArticleActivity.this, SettingActivity.class);
                        startActivity(intent);
                        break;
                }
                dialog.dismiss();
            }


        });

        builder.create().show();
    }

    private class DownloadArticleTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object... params) {
            if (myArticle != null) {
                theGottenArticle = NovelAPI.getArticle(myArticle, ArticleActivity.this);
                if (theGottenArticle != null) {
                    myArticle = theGottenArticle;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            layoutProgress.setVisibility(View.GONE);
            setArticleText();
            myArticle.setNovelId(novelId);

            new GetLastPositionTask().execute();
        }
    }

    private void requestInterstitialAd() {
        MoPubInterstitial ad = interstitialManager.getAd();
        if (ad != null && ad.isReady()) {
            mAdView1.setVisibility(View.GONE);
            mAdView2.setVisibility(View.GONE);
            ad.show();
        }else{
            mAdView1.setVisibility(View.VISIBLE);
            mAdView2.setVisibility(View.VISIBLE);
            refreshAd();
        }
    }

    private void refreshAd() {
        AdLoader.Builder builder2 = new AdLoader.Builder(this, ADMOB_AD_UNIT_ID);


        builder2.forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
            @Override
            public void onAppInstallAdLoaded(NativeAppInstallAd ad) {

                NativeAppInstallAdView adView = (NativeAppInstallAdView) getLayoutInflater()
                        .inflate(R.layout.ad_app_install, null);
                populateAppInstallAdView(ad, adView);
                mAdView2.removeAllViews();
                mAdView2.addView(adView);
            }
        });


        AdLoader.Builder builder1 = new AdLoader.Builder(this, ADMOB_AD_UNIT_ID);

        builder1.forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
            @Override
            public void onContentAdLoaded(NativeContentAd ad) {
                NativeContentAdView adView = (NativeContentAdView) getLayoutInflater()
                        .inflate(R.layout.ad_content, null);
                populateContentAdView(ad, adView);
                mAdView1.removeAllViews();
                mAdView1.addView(adView);
            }
        });


        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder1.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder1.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {

            }
        }).build();
        adLoader.loadAd(new AdRequest.Builder().build());

        builder2.withNativeAdOptions(adOptions);

        AdLoader adLoader2 = builder2.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {

            }
        }).build();

        adLoader2.loadAd(new AdRequest.Builder().build());
    }

    /**
     * Populates a {@link NativeAppInstallAdView} object with data from a given
     * {@link NativeAppInstallAd}.
     *
     * @param nativeAppInstallAd the object containing the ad's assets
     * @param adView             the view to be populated
     */
    private void populateAppInstallAdView(NativeAppInstallAd nativeAppInstallAd,
                                          NativeAppInstallAdView adView) {
        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = nativeAppInstallAd.getVideoController();

        // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
        // VideoController will call methods on this object when events occur in the video
        // lifecycle.
        vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
            public void onVideoEnd() {
                // Publishers should allow native ads to complete video playback before refreshing
                // or replacing them with another ad in the same UI location.
                super.onVideoEnd();
            }
        });

        adView.setHeadlineView(adView.findViewById(R.id.appinstall_headline));
        adView.setBodyView(adView.findViewById(R.id.appinstall_body));
        adView.setCallToActionView(adView.findViewById(R.id.appinstall_call_to_action));
        adView.setIconView(adView.findViewById(R.id.appinstall_app_icon));
        adView.setPriceView(adView.findViewById(R.id.appinstall_price));
        adView.setStarRatingView(adView.findViewById(R.id.appinstall_stars));
        adView.setStoreView(adView.findViewById(R.id.appinstall_store));

        // Some assets are guaranteed to be in every NativeAppInstallAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAppInstallAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAppInstallAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAppInstallAd.getCallToAction());
        ((ImageView) adView.getIconView()).setImageDrawable(
                nativeAppInstallAd.getIcon().getDrawable());

        MediaView mediaView = adView.findViewById(R.id.appinstall_media);
        ImageView mainImageView = adView.findViewById(R.id.appinstall_image);

        // Apps can check the VideoController's hasVideoContent property to determine if the
        // NativeAppInstallAd has a video asset.
        if (vc.hasVideoContent()) {
            adView.setMediaView(mediaView);
            mainImageView.setVisibility(View.GONE);

        } else {
            adView.setImageView(mainImageView);
            mediaView.setVisibility(View.GONE);

            // At least one image is guaranteed.
            List<NativeAd.Image> images = nativeAppInstallAd.getImages();
            mainImageView.setImageDrawable(images.get(0).getDrawable());

        }

        // These assets aren't guaranteed to be in every NativeAppInstallAd, so it's important to
        // check before trying to display them.
        if (nativeAppInstallAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAppInstallAd.getPrice());
        }

        if (nativeAppInstallAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAppInstallAd.getStore());
        }

        if (nativeAppInstallAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAppInstallAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAppInstallAd);
    }

    /**
     * Populates a {@link NativeContentAdView} object with data from a given
     * {@link NativeContentAd}.
     *
     * @param nativeContentAd the object containing the ad's assets
     * @param adView          the view to be populated
     */
    private void populateContentAdView(NativeContentAd nativeContentAd,
                                       NativeContentAdView adView) {

        adView.setHeadlineView(adView.findViewById(R.id.contentad_headline));
        adView.setImageView(adView.findViewById(R.id.contentad_image));
        adView.setBodyView(adView.findViewById(R.id.contentad_body));
        adView.setCallToActionView(adView.findViewById(R.id.contentad_call_to_action));
        adView.setLogoView(adView.findViewById(R.id.contentad_logo));
        adView.setAdvertiserView(adView.findViewById(R.id.contentad_advertiser));

        // Some assets are guaranteed to be in every NativeContentAd.
        ((TextView) adView.getHeadlineView()).setText(nativeContentAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeContentAd.getBody());
        ((TextView) adView.getCallToActionView()).setText(nativeContentAd.getCallToAction());
        ((TextView) adView.getAdvertiserView()).setText(nativeContentAd.getAdvertiser());

        List<NativeAd.Image> images = nativeContentAd.getImages();

        if (images.size() > 0) {
            ((ImageView) adView.getImageView()).setImageDrawable(images.get(0).getDrawable());
        }

        // Some aren't guaranteed, however, and should be checked.
        NativeAd.Image logoImage = nativeContentAd.getLogo();

        if (logoImage == null) {
            adView.getLogoView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getLogoView()).setImageDrawable(logoImage.getDrawable());
            adView.getLogoView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeContentAd);
    }

    private void setArticleText() {
        if (myArticle.getText().indexOf("*&&$$*") > 0) {
            // this is img of text
            articleScrollView.setVisibility(View.GONE);
            articleWebView.setVisibility(View.VISIBLE);
            String[] urls = myArticle.getText().split("\\*&&\\$\\$\\*");
            String html = "<html><body>";
            String imgString = "";
            for (int i = 0; i < urls.length; i++) {
                imgString += "<img src=\"" + urls[i] + "\"><br><br>";
            }
            html += imgString + "<br><br></body></html>";
            String mime = "text/html";
            String encoding = "utf-8";
            articleWebView.getSettings().setSupportZoom(true);
            articleWebView.getSettings().setBuiltInZoomControls(true);
            articleWebView.getSettings().setLoadWithOverviewMode(true);
            articleWebView.getSettings().setUseWideViewPort(true);
            articleWebView.loadDataWithBaseURL(null, html, mime, encoding, null);
        } else {
            articleScrollView.setVisibility(View.VISIBLE);
            articleWebView.setVisibility(View.GONE);
            String text = "";
            if (textLanguage == 1) {
                try {
                    text = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(myArticle.getText());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                text = myArticle.getText();
            }
            articleTextView.setText(text + "\n");
        }
    }

    private class UpdateArticleTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object... params) {
            if (myArticle != null) {
                theGottenArticle = NovelAPI.getArticle(myArticle, ArticleActivity.this);
                if (theGottenArticle != null) {
                    myArticle = theGottenArticle;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {

            super.onPostExecute(result);
            layoutProgress.setVisibility(View.GONE);
            if (theGottenArticle != null) {
                setArticleText();

                myArticle.setNovelId(novelId);
                articleScrollView.scrollTo(0, 0);
                setArticleTitle(myArticle.getTitle());
                articlePercent.setText("0%");
            } else {
                Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_data), Toast.LENGTH_SHORT).show();
            }
            updateBookmark();
        }
    }

    private class GetPreviousArticleTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object... params) {
            if (myArticle != null) {
                theGottenArticle = NovelAPI.getPreviousArticle(myArticle, ArticleActivity.this);
                if (theGottenArticle != null) {
                    myArticle = theGottenArticle;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {

            super.onPostExecute(result);
            layoutProgress.setVisibility(View.GONE);
            if (theGottenArticle != null) {
                setArticleText();

                myArticle.setNovelId(novelId);
                articleScrollView.scrollTo(0, 0);
                setArticleTitle(myArticle.getTitle());
                articlePercent.setText("0%");
            } else {
                Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_up), Toast.LENGTH_SHORT).show();
            }
            updateBookmark();
        }
    }

    private class GetNextArticleTask extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object... params) {
            if (myArticle != null) {
                theGottenArticle = NovelAPI.getNextArticle(myArticle, ArticleActivity.this);
                if (theGottenArticle != null) {
                    myArticle = theGottenArticle;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            layoutProgress.setVisibility(View.GONE);
            if (theGottenArticle != null) {
                setArticleText();

                myArticle.setNovelId(novelId);
                articleScrollView.scrollTo(0, 0);
                setArticleTitle(myArticle.getTitle());
                articlePercent.setText("0%");
            } else {
                Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_down), Toast.LENGTH_SHORT).show();
            }
            updateBookmark();
        }
    }

    private class GetLastPositionTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object... params) {

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {

            super.onPostExecute(result);

            int tt = articleTextView.getHeight();

            if (yRate == 0) {
                articlePercent.setText(0 + "%");
                articleScrollView.scrollTo(0, 0);
            } else {
                String yPositon = Integer.toString(yRate);
                articlePercent.setText(yPositon + "%");
                currentY = yRate * tt / 100;
                articleScrollView.scrollTo(0, currentY);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        int originTextLan = textLanguage;

        textSize = Setting.getSettingInt(Setting.keyTextSize, ArticleActivity.this);
        textMode = Setting.getSettingString(Setting.keyMode, ArticleActivity.this);
        textLanguage = Setting.getSettingInt(Setting.keyTextLanguage, ArticleActivity.this);
        clickToNextPage = Setting.getSettingInt(Setting.keyClickToNextPage, ArticleActivity.this);
        stopSleeping = Setting.getSettingInt(Setting.keyStopSleeping, ArticleActivity.this);

        articleTextView.setTextSize(textSize);
        articleTextView.setTextColor(Setting.getBackgroundModeTextColor(textMode, this));
        articleLayout.setBackgroundColor(Setting.getBackgroundModeBackgroundColor(textMode, this));
        layoutProgress.setBackgroundColor(Setting.getBackgroundModeBackgroundColor(textMode, this));

        if (originTextLan != textLanguage) {
            if (textLanguage == 1) {
                String text = "";
                try {
                    text = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(myArticle.getTitle() + "\n\n" + myArticle.getText());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                articleTextView.setText(text);
            } else {
                articleTextView.setText(articleTitle + "\n\n" + myArticle.getText());
            }
        }

        if (clickToNextPage == 0) {
            articleTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // new GetNextArticleTask().execute();
                    int kk = articleScrollView.getHeight();
                    int tt = articleTextView.getHeight();
                    articleScrollView.scrollTo(0, currentY + kk - 8);
                }
            });
        } else {
            articleTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // do nothing
                }
            });
        }

        if (stopSleeping == 0) {
            ArticleActivity.this.findViewById(android.R.id.content).setKeepScreenOn(true);
        }

        bannerAdView = (RelativeLayout) findViewById(R.id.adonView);
        if (Setting.getSettingInt(Setting.keyYearSubscription, this) == 0 && Setting.getSettingInt(Setting.keyArticleAdType,this) != Setting.InterstitialAd)
            moPubView = setBannerAdView(bannerAdView);
        else
            ((RelativeLayout) findViewById(R.id.adonView)).setVisibility(View.GONE);


        setBottomButtonsColor();

    }

    private void setBottomButtonsColor() {

        RelativeLayout background = (RelativeLayout) findViewById(R.id.bottom_buttons);

        float[] hsv = new float[3];
        int color = Setting.getBackgroundModeBackgroundColor(textMode, this);
        Color.colorToHSV(color, hsv);

        if (textMode.equals(Setting.keySunMode)) {
            hsv[2] *= 0.8f; // value component
        } else {
            hsv[2] = 1.0f - 0.8f * (1.0f - hsv[2]);
        }
        color = Color.HSVToColor(hsv);
        background.setBackgroundColor(color);

        articleButtonUp.setTextColor(Setting.getBackgroundModeTextColor(textMode, this));
        articleButtonDown.setTextColor(Setting.getBackgroundModeTextColor(textMode, this));
        ;
        articlePercent.setTextColor(Setting.getBackgroundModeTextColor(textMode, this));
        ;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int audioClickSetting = Setting.getSettingInt(Setting.keyAudioClickToNextPage,this);

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && audioClickSetting == 0){
            if (articleAdType == Setting.InterstitialAd && Setting.getSettingInt(Setting.keyYearSubscription, ArticleActivity.this) == 0) {
                requestInterstitialAd();
                adHasShowed = false;
            }
            nextPage();
            return true;
        }else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && audioClickSetting == 0) {
            if (articleAdType == Setting.InterstitialAd && Setting.getSettingInt(Setting.keyYearSubscription, ArticleActivity.this) == 0) {
                requestInterstitialAd();
                adHasShowed = false;
            }
            previousPage();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void nextPage() {
        if (articleIDs != null) {
            if (ariticlePosition < articleIDs.size() - 1) {
                ariticlePosition = ariticlePosition + 1;
                if (downloadBoolean) {
                    myArticle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", true, articleNums.get(ariticlePosition));
                } else {
                    myArticle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", false, articleNums.get(ariticlePosition));
                }
                new UpdateArticleTask().execute();
            } else {
                Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_down), Toast.LENGTH_SHORT).show();
            }
        } else {
            new GetNextArticleTask().execute();
        }
    }

    private void previousPage() {
        if (articleIDs != null) {
            if (ariticlePosition != 0) {
                ariticlePosition = ariticlePosition - 1;
                if (downloadBoolean) {
                    myArticle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", true, articleNums.get(ariticlePosition));
                } else {
                    myArticle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", false, articleNums.get(ariticlePosition));
                }
                new UpdateArticleTask().execute();
            } else {
                Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_up), Toast.LENGTH_SHORT).show();
            }
        } else {
            new GetPreviousArticleTask().execute();
        }
    }



    @Override
    protected void onPause() {
        NovelAPI.createRecentBookmark(new Bookmark(0, myArticle.getNovelId(), myArticle.getId(), yRate, novelName, myArticle.getTitle(), novelPic, true),
                ArticleActivity.this);
        NovelAPI.keepNovelLastViewDateIfInDB(myArticle.getNovelId(), ArticleActivity.this);
        super.onPause();
    }

}
