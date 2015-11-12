package com.analytics;

import com.kosbrother.fragments.IndexNovelFragment;

import java.util.HashMap;

/**
 * Created by steven on 8/28/14.
 */
public class AnalyticsName {

    // Screen
    public final static String IndexCatogry = "Index Category List Fragment";
    public final static String IndexRecommendGrid = "Index Recommend Grid Fragment";

    public static final HashMap<Integer, String> IndexFragmentMap = new HashMap<Integer, String>() {
        {
            put(IndexNovelFragment.HOT_NOVEL, "Index HOT_NOVEL Fragment");
            put(IndexNovelFragment.MONTH_NOVEL, "Index MONTH_NOVEL Fragment");
            put(IndexNovelFragment.WEEK_NOVEL, "Index WEEK_NOVEL Fragment");
            put(IndexNovelFragment.LATEST_NOVEL, "Index LATEST_NOVEL Fragment");
        }
    };

    public final static String CategroyHotNovelsFragment = "Category Hot Novel Fragment";
    public final static String CategoryRecommendFragment = "Category Recommend Fragment";
    public final static String CategoryWeekFragment = "Category All Novels Fragment";
    public final static String CategoryLatestNovelsFragment = "Category Latest Novels Fragment";
    public final static String CategoryFinishFragment = "Category Finish Fragment";
    public final static String CategoryAllNovelsFragment = "Category AllNovels Fragment";


    public final static String SettingActivity = "Setting Activity";
    public final static String ArticleActivity = "Article Activity";
    public final static String DonateActivity = "Donate Activity";
    public final static String DownloadActivity = "Download Activity";
    public final static String MyDownloadArticleActivity = "MyDownloadArticle Activity";
    public final static String NovelContentsActivity = "NovelContents Activity";
    public final static String NovelIntroduceActivity = "NovelIntroduce Activity";
    public final static String NovelRecommendActivity = "NovelRecommend Activity";
    public final static String SearchActivity = "Search Activity";

    public final static String BookmarkFragment = "Bookmark Fragment";
    public final static String BookmarkRecentReadFragment = "Bookmark Recent Read Fragment";

    public final static String MyNovelCollectedBookcaseFragment = "My Collected Novels Bookcase Fragment";
    public final static String MyNovelDownloadBookcaseFragment = "My Downloaded Novels Bookcase Fragment";

    //Event
    public final static String Recommend = "Recommend";
    public final static String RecommendIndex = "RecommendIndex";

    public final static String Novel = "Novel";
    public final static String NovelIntro = "NovelIntro";

    public final static String Index = "Index";
    public final static String Category = "Category";
    public final static String Collect = "Collect";
    public final static String Search = "Search";
    public final static String SearchNull = "SearchNull";

    public final static String Donate = "Donate";
    public final static String DonateClick = "Donate Click";

}
