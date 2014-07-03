package com.novel.reader;

import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ads.AdFragmentActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.kosbrother.tool.DetectScrollView;
import com.kosbrother.tool.DetectScrollView.DetectScrollViewListener;
import com.kosbrother.tool.Report;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.Article;
import com.novel.reader.entity.Bookmark;
import com.novel.reader.entity.Novel;
import com.novel.reader.util.Setting;

public class ArticleActivity extends AdFragmentActivity implements DetectScrollViewListener {

    private static final int    ID_SETTING  = 0;
    private static final int    ID_Bookmark = 4;
    private static final int    ID_Report   = 5;
	private static final int    ID_MODE = 6;
	private static final int    ID_FONT_SIZE = 7;
	private static final int    ID_CONTENTS = 8;
	private static final int    ID_NOVEL = 9;
	
	private int                 textSize;
    private int                 textLanguage;                                    // 0 for 繁體, 1 for 簡體
    private int                 readingDirection;                                // 0 for 直向, 1 for 橫向
    private int                 clickToNextPage;                                 // 0 for yes, 1 for no
    private int                 stopSleeping;                                    // 0 for yes, 1 for no
    private String 				textMode;

    private TextView            articleTextView;
    private DetectScrollView    articleScrollView;
    private Button              articleButtonUp;
    private Button              articleButtonDown;
    private TextView            articlePercent;
    private Article             myArticle;                                        // uset to get article text
    private Article             theGottenArticle;
    private Boolean             downloadBoolean;
    private Bundle              mBundle;
    private String              novelName;
    private String              articleTitle;
    private int                 articleId = -1;
    private String              novelPic;
    private int                 novelId;
    private int                 yRate = -1;
    private int                 ariticlePosition = -1;
    private ArrayList<Integer>  articleIDs;
    // private ProgressDialog progressDialog= null;
    private ActionBar           ab;
    private LinearLayout        layoutProgress;
    private int                 currentY    = 0;
    private int                 articleNum = -1;
    private ArrayList<Integer>  articleNums;
	private WebView             articleWebView;
	private LinearLayout articleLayout;
	private int articleAdType;

	private RelativeLayout bannerAdView;
	private TextView articleTitleTextView;
	private ImageView bookmarkImage;
	private ImageView novelImage;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Setting.setApplicationActionBarTheme(this);
        setContentView(R.layout.layout_article);

        
        restorePreValues();
        setViews();

        ab = getSupportActionBar();
        mBundle = this.getIntent().getExtras();
        novelName = mBundle.getString("NovelName");
        novelPic = mBundle.getString("NovelPic");
        novelId = mBundle.getInt("NovelId");
        
        if(myArticle == null){
	         articleTitle = mBundle.getString("ArticleTitle");
	         articleId = mBundle.getInt("ArticleId");
	         downloadBoolean = mBundle.getBoolean("ArticleDownloadBoolean", false);
	         yRate = mBundle.getInt("ReadingRate", 0);
	         articleIDs = mBundle.getIntegerArrayList("ArticleIDs");
	         ariticlePosition = mBundle.getInt("ArticlePosition");
	         articleNum = mBundle.getInt("ArticleNum");
	         articleNums = mBundle.getIntegerArrayList("ArticleNums");
	         if(savedInstanceState != null){
	        	 getSavedState(savedInstanceState);
	         }

	  	    if (articleIDs != null) {
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
	  	    new UploadUserReadNovelTask().execute();
        }
        
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        
        setArticleTitle(articleTitle);
        
        // ab.setTitle(novelName);
        ab.setDisplayHomeAsUpEnabled(true);        
    }
    
    
    public void getSavedState(Bundle savedInstanceState){
    	 if(savedInstanceState.containsKey("ArticleTitle"));
	 		articleTitle = savedInstanceState.getString("ArticleTitle");
	 	 if(savedInstanceState.containsKey("ArticleId"));
	 	 	articleId = savedInstanceState.getInt("ArticleId");
	 	 if(savedInstanceState.containsKey("ArticleDownloadBoolean"));
	 	 	downloadBoolean = savedInstanceState.getBoolean("ArticleDownloadBoolean", false);
	     if(savedInstanceState.containsKey("ReadingRate"));
	     	yRate = savedInstanceState.getInt("ReadingRate", 0);
	     if(savedInstanceState.containsKey("ArticleIDs"));
	     	articleIDs = savedInstanceState.getIntegerArrayList("ArticleIDs");
	     if(savedInstanceState.containsKey("ArticlePosition"));
	     	ariticlePosition = savedInstanceState.getInt("ArticlePosition");
	     if(savedInstanceState.containsKey("ArticleNum"));
	     	articleNum = savedInstanceState.getInt("ArticleNum");
	     if(savedInstanceState.containsKey("ArticleNums"));
	     	articleNums = savedInstanceState.getIntegerArrayList("ArticleNums");
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
      super.onSaveInstanceState(savedInstanceState);
    }

    private void setArticleTitle(String articleTitle) {
              articleTitleTextView.setText(novelName + ":" + articleTitle);
    }

    private void restorePreValues() {
    	
    	textSize = Setting.getSettingInt(Setting.keyTextSize, ArticleActivity.this);
        textMode = Setting.getSettingString(Setting.keyMode, ArticleActivity.this);
        textLanguage = Setting.getSettingInt(Setting.keyTextLanguage, ArticleActivity.this);
        readingDirection = Setting.getSettingInt(Setting.keyReadingDirection, ArticleActivity.this);
        clickToNextPage = Setting.getSettingInt(Setting.keyClickToNextPage, ArticleActivity.this);
        stopSleeping = Setting.getSettingInt(Setting.keyStopSleeping, ArticleActivity.this);

        if (readingDirection == 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (readingDirection == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        if (stopSleeping == 0) {
            ArticleActivity.this.findViewById(android.R.id.content).setKeepScreenOn(true);
        }
    }

    private void setViews() {

        layoutProgress = (LinearLayout) findViewById(R.id.layout_progress);
        articleTextView = (TextView) findViewById(R.id.article_text);
        articleWebView = (WebView)findViewById(R.id.article_webview);
        articleScrollView = (DetectScrollView) findViewById(R.id.article_scrollview);
        articleButtonUp = (Button) findViewById(R.id.article_button_up);
        articleButtonDown = (Button) findViewById(R.id.article_button_down);
        articlePercent = (TextView) findViewById(R.id.article_percent);
        articleLayout = (LinearLayout)findViewById(R.id.article_layout);
        articleTitleTextView = (TextView)findViewById(R.id.article_title);
        bookmarkImage = (ImageView)findViewById(R.id.bookmarkImage);
        novelImage = (ImageView)findViewById(R.id.novelImage);

        articleScrollView.setScrollViewListener(ArticleActivity.this);

        articleTextView.setTextSize(textSize);
        articleTextView.setTextColor(Setting.getBackgroundModeTextColor(textMode,this));
        articleLayout.setBackgroundColor(Setting.getBackgroundModeBackgroundColor(textMode,this));
        layoutProgress.setBackgroundColor(Setting.getBackgroundModeBackgroundColor(textMode,this));
        	
        
        
        
        articleButtonUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

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
        });

        articleButtonDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
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
    	if(bookmark != null){
    		bookmarkImage.setVisibility(View.VISIBLE);
    	}else{
    		bookmarkImage.setVisibility(View.GONE);
    	}
	}


	@Override
    public void onScrollChanged(DetectScrollView scrollView, int x, int y, int oldx, int oldy) {
        int kk = articleScrollView.getHeight();
        int tt = articleTextView.getHeight();

        currentY = y;
        yRate = (int) (((double) (y) / (double) (tt)) * 100);
        int xx = (int) (((double) (y + kk) / (double) (tt)) * 100);
        if (xx > 100)
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
    	
    	menu.add(0, ID_Bookmark, 3, getResources().getString(R.string.menu_add_bookmark)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    	menu.add(0, ID_NOVEL, 4, getResources().getString(R.string.menu_collect_novel)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    	menu.add(0, ID_SETTING, 5, getResources().getString(R.string.my_read_setting)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    	menu.add(0, ID_Report, 6, getResources().getString(R.string.menu_article_report)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        
        return true;
    }
    
    private boolean isLightAppTheme(){
    	if(Setting.getSettingInt(Setting.keyAppTheme, this) == 0)
    		return true;
    	else
    		return false;
    }
    private int getModeIcon() {
		if(isLightAppTheme())
			return R.drawable.article_sun;
		else
			return R.drawable.article_sun_white;
	}
    private int getFontSizeIcon(){
    	if(isLightAppTheme())
			return R.drawable.article_font_size;
		else
			return R.drawable.article_font_size_white;
    }
    private int getContentsIcon(){
    	if(isLightAppTheme())
			return R.drawable.article_contents;
		else
			return R.drawable.article_contents_white;
    }


	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	setBookMarkItem(menu);
    	setCollectNovelItem(menu);
    	
        return super.onPrepareOptionsMenu(menu);
    }

    private void setCollectNovelItem(Menu menu) {
    	if (NovelAPI.isNovelCollected(this, novelId)){
    		novelImage.setVisibility(View.VISIBLE);
    		menu.findItem(ID_NOVEL).setTitle(getResources().getString(R.string.menu_remove_collect_novel));
    	}else{
    		novelImage.setVisibility(View.GONE);
    		menu.findItem(ID_NOVEL).setTitle(getResources().getString(R.string.menu_collect_novel));
    	}
	}


	private void setBookMarkItem(Menu menu) {
    	Bookmark bookmark = NovelAPI.findBookMarkByArticle(myArticle, ArticleActivity.this);
    	if(bookmark != null){
    		bookmarkImage.setVisibility(View.VISIBLE);
    		menu.findItem(ID_Bookmark).setTitle(getResources().getString(R.string.menu_delete_bookmark));
    	}else{
    		bookmarkImage.setVisibility(View.GONE);
    		menu.findItem(ID_Bookmark).setTitle(getResources().getString(R.string.menu_add_bookmark));
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
        	if(bookmark != null){
        		NovelAPI.deleteBookmark(bookmark, ArticleActivity.this);
        		Toast.makeText(ArticleActivity.this, getResources().getString(R.string.menu_delete_bookmark), Toast.LENGTH_SHORT).show();
        	}else{
	            NovelAPI.insertBookmark(new Bookmark(0, myArticle.getNovelId(), myArticle.getId(), yRate, novelName, myArticle.getTitle(), novelPic, false),ArticleActivity.this);
	            Toast.makeText(ArticleActivity.this, getResources().getString(R.string.menu_add_bookmark), Toast.LENGTH_SHORT).show();
        	}
            invalidateOptionsMenu();
            break;
        case ID_Report:
        	Report.createReportDialog(this,novelName+"("+novelId+")",myArticle.getTitle()+"(Num:"+myArticle.getNum()+")");  	
            break;
        case ID_MODE:
        	showModeDialog();
        	break;
        case ID_NOVEL:
        	if (NovelAPI.isNovelCollected(this, novelId)){
        		Novel theNovel = NovelAPI.getNovel(novelId, this);
        		NovelAPI.removeNovelFromCollected(theNovel, this);
        		Toast.makeText(ArticleActivity.this, getResources().getString(R.string.menu_remove_collect_novel), Toast.LENGTH_SHORT).show();
        		invalidateOptionsMenu();
        	}else{
        		new AsyncTask(){
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
		int resultArticleId = data.getIntExtra("SelectArticleId", 0);
		int resultPosition = data.getIntExtra("SelectArticlePosition", 0);
	    if (requestCode == 1 && resultCode == RESULT_OK && resultArticleId != 0 && resultPosition != 0) {
	    	myArticle.id = resultArticleId;
	    	ariticlePosition = resultPosition;
	    	new UpdateArticleTask().execute();
	    }
	}

    
    private void showFontSizeDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	CharSequence[] array = getResources().getStringArray(R.array.fontsize_selection);
    	builder.setTitle(getResources().getString(R.string.fontsize_setting_title)).setSingleChoiceItems(array, -1, new DialogInterface.OnClickListener() {

	    	@Override
	    	public void onClick(DialogInterface dialog, int position) {
	    		switch(position){
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
	    		switch(position){
	    		case 0:
	    			articleLayout.setBackgroundColor(Setting.getBackgroundModeBackgroundColor(Setting.keySunMode,ArticleActivity.this));
	    			articleTextView.setTextColor(Setting.getBackgroundModeTextColor(Setting.keySunMode,ArticleActivity.this));
	    			textMode = Setting.keySunMode;
	    			Setting.saveSetting(Setting.keyMode, Setting.keySunMode, ArticleActivity.this);
	    			break;
	    		case 1:
	    			articleLayout.setBackgroundColor(Setting.getBackgroundModeBackgroundColor(Setting.keyMoonMode,ArticleActivity.this));
	    			articleTextView.setTextColor(Setting.getBackgroundModeTextColor(Setting.keyMoonMode,ArticleActivity.this));
	    			textMode = Setting.keyMoonMode;
	    			Setting.saveSetting(Setting.keyMode, Setting.keyMoonMode, ArticleActivity.this);
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


	private class UploadUserReadNovelTask extends AsyncTask{

		@Override
		protected Object doInBackground(Object... arg0) {
			NovelAPI.sendNovel(myArticle.getId(), Settings.Secure.getString(ArticleActivity.this.getContentResolver(),Settings.Secure.ANDROID_ID));
			return null;
		}
    	
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
            if(articleAdType == Setting.InterstitialAd && Setting.getSettingInt(Setting.keyYearSubscription, ArticleActivity.this) ==  0)
                requestInterstitialAd();

        }
    }
    
    private void setArticleText(){
    	if(myArticle.getText().indexOf("*&&$$*") > 0){
        	// this is img of text
        	articleScrollView.setVisibility(View.GONE);
        	articleWebView.setVisibility(View.VISIBLE);
        	String[] urls = myArticle.getText().split("\\*&&\\$\\$\\*");
        	String html = "<html><body>";
        	String imgString = "";
        	for(int i=0; i < urls.length ; i++){
        		imgString += "<img src=\""+urls[i]+"\"><br><br>";
        	}
        	html += imgString + "<br><br></body></html>";
        	String mime = "text/html";
        	String encoding = "utf-8";
        	articleWebView.getSettings().setSupportZoom(true);
            articleWebView.getSettings().setBuiltInZoomControls(true);
            articleWebView.getSettings().setLoadWithOverviewMode(true);
            articleWebView.getSettings().setUseWideViewPort(true);
            articleWebView.loadDataWithBaseURL(null, html, mime, encoding, null);
        }else{
        	articleScrollView.setVisibility(View.VISIBLE);
        	articleWebView.setVisibility(View.GONE);
            String text = "";
            if(textLanguage == 1){
            	try {
            	    text = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(myArticle.getText());
	            }catch (IOException e) {
	            	e.printStackTrace();
	            }
            }else{
            	text = myArticle.getText();
            }
            articleTextView.setText(text+"\n");
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
                if(articleAdType == Setting.InterstitialAd && Setting.getSettingInt(Setting.keyYearSubscription, ArticleActivity.this) ==  0)
                	requestInterstitialAd();

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
                if(articleAdType == Setting.InterstitialAd && Setting.getSettingInt(Setting.keyYearSubscription, ArticleActivity.this) ==  0)
                	requestInterstitialAd();

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
                if(articleAdType == Setting.InterstitialAd && Setting.getSettingInt(Setting.keyYearSubscription, ArticleActivity.this) ==  0)
                	requestInterstitialAd();
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

            int kk = articleScrollView.getHeight();
            int tt = articleTextView.getHeight();

            if (yRate == 0) {
                int xx = (int) (((double) (kk) / (double) (tt)) * 100);
                if (xx > 100)
                    xx = 100;
                String yPositon = Integer.toString(xx);
                articlePercent.setText(yPositon + "%");
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
        readingDirection = Setting.getSettingInt(Setting.keyReadingDirection, ArticleActivity.this);
        clickToNextPage = Setting.getSettingInt(Setting.keyClickToNextPage, ArticleActivity.this);
        stopSleeping = Setting.getSettingInt(Setting.keyStopSleeping, ArticleActivity.this);

        articleTextView.setTextSize(textSize);
        articleTextView.setTextColor(Setting.getBackgroundModeTextColor(textMode,this));
        articleLayout.setBackgroundColor(Setting.getBackgroundModeBackgroundColor(textMode,this));
        layoutProgress.setBackgroundColor(Setting.getBackgroundModeBackgroundColor(textMode,this));

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

        if (readingDirection == 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (readingDirection == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
        
        articleAdType = Setting.getSettingInt(Setting.keyArticleAdType, ArticleActivity.this);
        if(articleAdType == Setting.BannerAd){
        	((RelativeLayout) findViewById(R.id.adonView)).setVisibility(View.VISIBLE);
        }else
        	((RelativeLayout) findViewById(R.id.adonView)).setVisibility(View.GONE);
        
        bannerAdView = (RelativeLayout) findViewById(R.id.adonView);
        if(Setting.getSettingInt(Setting.keyYearSubscription, this) ==  0)
        	mAdView = setBannerAdView(bannerAdView);
        else
        	((RelativeLayout) findViewById(R.id.adonView)).setVisibility(View.GONE);

    }

    @Override
    protected void onPause() {
        NovelAPI.createRecentBookmark(new Bookmark(0, myArticle.getNovelId(), myArticle.getId(), yRate, novelName, myArticle.getTitle(), novelPic, true),
                ArticleActivity.this);
        super.onPause();
    }
    
    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this);
    }

}
