package com.novel.reader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.kosbrother.tool.ChildArticle;
import com.kosbrother.tool.Group;
import com.kosbrother.tool.Report;
import com.novel.reader.adapter.ExpandListAdapter;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.Article;
import com.novel.reader.entity.Bookmark;
import com.novel.reader.entity.Novel;
import com.novel.reader.util.NovelReaderUtil;
import com.novel.reader.util.Setting;
import com.taiwan.imageload.ImageLoader;

import java.util.ArrayList;
import java.util.TreeMap;

public class NovelIntroduceActivity extends NovelReaderBaseActivity {

    private static final int                    ID_SETTING        = 0;
    private static final int                    ID_RESPONSE       = 1;
    private static final int                    ID_ABOUT_US       = 2;
    private static final int                    ID_GRADE          = 3;
    private static final int                    ID_DOWNLOAD       = 4;
    private static final int                    ID_SEARCH         = 5;
    private static final int                    ID_Report         = 6;

    private EditText                            search;
    private Bundle                              mBundle;
    private String                              novelName;
    private int                                 novelId;
    private String                              novelAuthor;
    private String                              novelDescription;
    private String                              novelUpdate;
    private String                              novelPicUrl;
    private String                              novelArticleNum;
    private ImageView                           novelImageView;
    private TextView                            novelTextName;
    private TextView                            novelTextAuthor;
    private TextView                            novelTextDescription;
    private TextView                            novelTextUpdate;
    private Button                              novelButton;
    private ImageLoader                         mImageLoader;
    private LinearLayout                        novelLayoutProgress;
    private LinearLayout                        layoutTextArrow;
    private CheckBox                            checkBoxAddBookcase;
    private ImageView                           imageArrow;
    private ArrayList<Article>                  articleList       = new ArrayList<Article>();
    private ExpandableListView                  novelListView;
    private Novel                               theNovel;
    private Boolean                             descriptionExpand = false;
    private MenuItem                            itemSearch;
    private int                                 expandGroup       = -1;

    private TreeMap<String, ArrayList<Article>> myData            = new TreeMap<String, ArrayList<Article>>();
    // private ArrayList<String> groupTitleList = new ArrayList<String>();
    private ArrayList<Group>                    mGroups           = new ArrayList<Group>();
    private AlertDialog.Builder                 aboutUsDialog;
    
	private LinearLayout bannerAdView;
	private Boolean isNovelChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Setting.setApplicationActionBarTheme(this);
        setContentView(R.layout.layout_novel_introduce);
        findViews();
        mBundle = this.getIntent().getExtras();
        novelId = mBundle.getInt("NovelId");

        new DownloadNovelTask().execute();

        final ActionBar ab = getSupportActionBar();
        novelName = mBundle.getString("NovelName");
        novelAuthor = mBundle.getString("NovelAuthor");
        novelDescription = mBundle.getString("NovelDescription");
        novelUpdate = mBundle.getString("NovelUpdate");
        novelPicUrl = mBundle.getString("NovelPicUrl");
        novelArticleNum = mBundle.getString("NovelArticleNum");

        theNovel = new Novel(novelId, novelName, novelAuthor, novelDescription, novelPicUrl, 0, novelArticleNum, novelUpdate, false, false, false);

        ab.setTitle(getResources().getString(R.string.title_novel_introduce));
        ab.setDisplayHomeAsUpEnabled(true);

        setViews();
        setAboutUsDialog();


    }
    

    @Override
    protected void onResume() {
        super.onResume();
        
        isNovelChecked = NovelAPI.isNovelCollected(NovelIntroduceActivity.this, novelId);
        if (isNovelChecked) {
            checkBoxAddBookcase.setChecked(true);
        } else {
            checkBoxAddBookcase.setChecked(false);
        }
        
        novelLayoutProgress.setVisibility(View.VISIBLE);
        if (articleList == null || articleList.size() == 0) {
            new DownloadArticlesTask().execute();
        } else {
            setGroupsAndAdatper();
            novelLayoutProgress.setVisibility(View.GONE);
        }
        

    }
    

    private void findViews() {
        novelImageView = (ImageView) findViewById(R.id.novel_image);
        novelTextName = (TextView) findViewById(R.id.novel_name);
        novelTextAuthor = (TextView) findViewById(R.id.novel_author);
        novelTextDescription = (TextView) findViewById(R.id.novel_description);
        novelTextUpdate = (TextView) findViewById(R.id.novel_update);
        novelListView = (ExpandableListView) findViewById(R.id.novel_artiles_list);
        novelButton = (Button) findViewById(R.id.novel_button);
        novelLayoutProgress = (LinearLayout) findViewById(R.id.novel_layout_progress);
        layoutTextArrow = (LinearLayout) findViewById(R.id.layout_text_arrow);
        imageArrow = (ImageView) findViewById(R.id.image_arrow);
        checkBoxAddBookcase = (CheckBox) findViewById(R.id.checkbox_add_bookcase);
    }

    private void setViews() {

        novelTextName.setText(NovelReaderUtil.translateTextIfCN(this,theNovel.getName() + "(" + theNovel.getArticleNum() + ")"));
        novelTextAuthor.setText(NovelReaderUtil.translateTextIfCN(this,getResources().getString(R.string.novel_author) + theNovel.getAuthor()));
        novelTextDescription.setText(NovelReaderUtil.translateTextIfCN(this,theNovel.getDescription()));
        novelTextUpdate.setText(getResources().getString(R.string.novel_update_time) + theNovel.getLastUpdate());

        mImageLoader = new ImageLoader(NovelIntroduceActivity.this, 70);
        mImageLoader.DisplayImage(theNovel.getPic(), novelImageView);

        novelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (novelButton.getText().equals(getResources().getString(R.string.novel_back))) {
                    novelButton.setText(getResources().getString(R.string.novel_front));
                    reverseMGroups();
                    ExpandListAdapter mAdapter = new ExpandListAdapter(NovelIntroduceActivity.this, mGroups, theNovel, expandGroup);
                    novelListView.setAdapter(mAdapter);
                } else {
                    novelButton.setText(getResources().getString(R.string.novel_back));
                    reverseMGroups();
                    ExpandListAdapter mAdapter = new ExpandListAdapter(NovelIntroduceActivity.this, mGroups, theNovel, expandGroup);
                    novelListView.setAdapter(mAdapter);
                }
            }

        });

        novelTextDescription.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (descriptionExpand.equals(false)) {
                    novelTextDescription.setMaxLines(999);
                    descriptionExpand = true;
                    imageArrow.setBackgroundDrawable(getResources().getDrawable(R.drawable.text_arrow_up));
                } else {
                    novelTextDescription.setMaxLines(3);
                    descriptionExpand = false;
                    imageArrow.setBackgroundDrawable(getResources().getDrawable(R.drawable.text_arrow_right));
                }
            }
        });

        layoutTextArrow.setOnClickListener(new OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View arg0) {
                if (descriptionExpand.equals(false)) {
                    novelTextDescription.setMaxLines(999);
                    descriptionExpand = true;
                    imageArrow.setBackgroundDrawable(getResources().getDrawable(R.drawable.text_arrow_up));
                } else {
                    novelTextDescription.setMaxLines(3);
                    descriptionExpand = false;
                    imageArrow.setBackgroundDrawable(getResources().getDrawable(R.drawable.text_arrow_right));
                }
            }
        });

        imageArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (descriptionExpand.equals(false)) {
                    novelTextDescription.setMaxLines(999);
                    descriptionExpand = true;
                    imageArrow.setBackgroundDrawable(getResources().getDrawable(R.drawable.text_arrow_up));
                } else {
                    novelTextDescription.setMaxLines(3);
                    descriptionExpand = false;
                    imageArrow.setBackgroundDrawable(getResources().getDrawable(R.drawable.text_arrow_right));
                }
            }
        });

        checkBoxAddBookcase.setOnClickListener((new OnClickListener() {
            public void onClick(View v) {
                if (checkBoxAddBookcase.isChecked()) {
                    Toast.makeText(NovelIntroduceActivity.this, NovelIntroduceActivity.this.getResources().getString(R.string.add_my_bookcase),
                            Toast.LENGTH_SHORT).show();
                    NovelAPI.collecNovel(theNovel, NovelIntroduceActivity.this);
                } else {
                    Toast.makeText(NovelIntroduceActivity.this, NovelIntroduceActivity.this.getResources().getString(R.string.remove_my_bookcase),
                            Toast.LENGTH_SHORT).show();
                    NovelAPI.removeNovelFromCollected(theNovel, NovelIntroduceActivity.this);
                    // need remove api
                }
            }
        }));
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
        expandGroup = mGroups.size() - expandGroup - 1;
    }

    private void setGroupsAndAdatper() {

        mGroups = new ArrayList<Group>();
        myData = new TreeMap<String, ArrayList<Article>>();

        if (articleList != null && articleList.size() != 0) {

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

            Bookmark theNovelBookmark = NovelAPI.getNovelBookmark(theNovel.getId(), NovelIntroduceActivity.this);

            for (int i = 0; i < mGroups.size(); i++) {
                ArrayList<Article> articles = myData.get(mGroups.get(i).getTitle());
                for (int j = 0; j < articles.size(); j++) {
                    mGroups.get(i).addChildrenItem(
                            new ChildArticle(articles.get(j).getId(), articles.get(j).getNovelId(), "", articles.get(j).getTitle(), articles.get(j)
                                    .getSubject(), articles.get(j).isDownload(), articles.get(j).getNum()));

                    if (theNovelBookmark != null) {
                        if (theNovelBookmark.getArticleTitle().equals(articles.get(j).getTitle()))
                            expandGroup = i;
                    }
                }
            }

        }
        if (novelButton.getText().equals(getResources().getString(R.string.novel_front)))
            reverseMGroups();
        ExpandListAdapter mAdapter = new ExpandListAdapter(NovelIntroduceActivity.this, mGroups, theNovel, expandGroup);
        novelListView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.activity_main, menu);

        menu.add(0, ID_SETTING, 0, getResources().getString(R.string.menu_settings)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_RESPONSE, 1, getResources().getString(R.string.menu_respond)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_ABOUT_US, 2, getResources().getString(R.string.menu_aboutus)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_GRADE, 3, getResources().getString(R.string.menu_recommend)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, ID_DOWNLOAD, 5, getResources().getString(R.string.menu_download)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, ID_Report, 6, getResources().getString(R.string.menu_report)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        if(Setting.getSettingInt(Setting.keyYearSubscription, this) ==  0)
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
                                    intent.setClass(NovelIntroduceActivity.this, SearchActivity.class);
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
            Intent intent = new Intent(NovelIntroduceActivity.this, SettingActivity.class);
            startActivity(intent);
            break;
        case ID_RESPONSE: // response
            final Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { getResources().getString(R.string.respond_mail_address) });
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
        case ID_DOWNLOAD: // response
            Intent intent_to_download = new Intent(NovelIntroduceActivity.this, DownloadActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("NovelId", novelId);
            bundle.putString("NovelName", novelName);
            intent_to_download.putExtras(bundle);
            startActivity(intent_to_download);
            break;
        case ID_SEARCH: // response
            Toast.makeText(NovelIntroduceActivity.this, "SEARCH", Toast.LENGTH_SHORT).show();
            break;
        case ID_Report:
        	Report.createReportDialog(this,novelName+"("+novelId+")",this.getResources().getString(R.string.report_not_article_problem));  	
            break;
        case 7:
        	Intent intent1 = new Intent();
            intent1.setClass(this, DonateActivity.class);
            startActivity(intent1);
        	break;
        }
        return true;
    }

    private class DownloadArticlesTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object... params) {
            articleList = NovelAPI.getNovelArticles(novelId, true, NovelIntroduceActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {

            super.onPostExecute(result);
            setGroupsAndAdatper();
            novelLayoutProgress.setVisibility(View.GONE);

        }
    }

    private class DownloadNovelTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object... params) {
            Novel getNovel = NovelAPI.getNovel(novelId, NovelIntroduceActivity.this);
            if (getNovel != null)
                theNovel = getNovel;

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (theNovel != null)
                setViews();
            // novelTextDescription.setText(theNovel.getDescription());

            novelTextDescription.setMaxLines(3);
            descriptionExpand = false;

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
