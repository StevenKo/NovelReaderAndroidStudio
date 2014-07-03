package com.novel.reader;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.novel.db.SQLiteNovel;
import com.novel.reader.util.Setting;

public class SettingActivity extends ActionBarActivity implements RadioGroup.OnCheckedChangeListener{

    // private SharedPreferences prefs;
    private int                 textSize;
    private int                 textLanguage;           // 0 for 繁體, 1 for 簡體
    private int                 readingDirection;       // 0 for 直向, 1 for 橫向
    private int                 clickToNextPage;        // 0 for yes, 1 for no
    private int                 stopSleeping;           // 0 for yes, 1 for no
    private SeekBar             mSeekBar;
    private RadioGroup          langRadioGroup;
    private RadioGroup          directionRadioGroup;
    private RadioGroup          tapRadioGroup;
    private RadioGroup          stopSleepRadioGroup;
    private RadioGroup          themeRadioGroup;
    private RadioGroup          articleAdTypeRadioGroup; 
    private int                 appTheme;
    private int                 articleAdType;

    private AlertDialog.Builder finishDialog;
    private Button              dbResetButton;
    
    private boolean isSettingChanged = false;
	private RadioGroup modeRadioGroup;
	private String textMode;
	private TextView sunModeTextPreView;
	private TextView moonModeTextPreView;
    private int     sunModeTextColor;
    private int     sunModeTextBackground;
    private int     moonModeTextColor;
    private int     moonModeTextBackground;
	private ImageView sunModeImageViewTextColor;
	private ImageView sunModeImageViewTextBackground;
	private ImageView moonModeImageViewTextColor;
	private ImageView moonnModeImageViewTextBackground;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Setting.setApplicationActionBarTheme(this);
        setContentView(R.layout.layout_setting);

        textSize = Setting.getSettingInt(Setting.keyTextSize, SettingActivity.this);
        textLanguage = Setting.getSettingInt(Setting.keyTextLanguage, SettingActivity.this);
        readingDirection = Setting.getSettingInt(Setting.keyReadingDirection, SettingActivity.this);
        clickToNextPage = Setting.getSettingInt(Setting.keyClickToNextPage, SettingActivity.this);
        stopSleeping = Setting.getSettingInt(Setting.keyStopSleeping, SettingActivity.this);
        appTheme = Setting.getSettingInt(Setting.keyAppTheme, SettingActivity.this);
        articleAdType = Setting.getSettingInt(Setting.keyArticleAdType, SettingActivity.this);
        textMode = Setting.getSettingString(Setting.keyMode, this);
        sunModeTextBackground = Setting.getBackgroundModeBackgroundColor(Setting.keySunMode, this);
        sunModeTextColor = Setting.getBackgroundModeTextColor(Setting.keySunMode, this);
        moonModeTextBackground = Setting.getBackgroundModeBackgroundColor(Setting.keyMoonMode, this);
        moonModeTextColor = Setting.getBackgroundModeTextColor(Setting.keyMoonMode, this);

        setViews();

        final ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.title_reading_setting));
        ab.setDisplayHomeAsUpEnabled(true);

    }

    private void setViews() {
        // TODO Auto-generated method stub
        mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
        modeRadioGroup = (RadioGroup) findViewById(R.id.RadioGroup_mode);
        langRadioGroup = (RadioGroup) findViewById(R.id.RadioGroup_lan);
        directionRadioGroup = (RadioGroup) findViewById(R.id.RadioGroup_reading_direction);
        tapRadioGroup = (RadioGroup) findViewById(R.id.RadioGroup_tap);
        stopSleepRadioGroup = (RadioGroup) findViewById(R.id.RadioGroup_stop_sleep);
        themeRadioGroup = (RadioGroup) findViewById(R.id.RadioGroup_theme);
        articleAdTypeRadioGroup = (RadioGroup) findViewById(R.id.RadioGroup_AD);
        sunModeTextPreView = (TextView) findViewById(R.id.sunmode_text_preview);
        moonModeTextPreView = (TextView) findViewById(R.id.moonmode_text_preview);
        sunModeImageViewTextColor = (ImageView) findViewById(R.id.imageview_sunmode_textcolor);
        sunModeImageViewTextBackground = (ImageView) findViewById(R.id.imageview_sunmode_textbackground);
        moonModeImageViewTextColor = (ImageView) findViewById(R.id.imageview_moonmode_textcolor);
        moonnModeImageViewTextBackground = (ImageView) findViewById(R.id.imageview_moonmode_textbackground);
        
        
        dbResetButton = (Button) findViewById(R.id.dbResetButton);

        sunModeTextPreView.setTextSize(textSize);
        moonModeTextPreView.setTextSize(textSize);
        sunModeTextPreView.setTextColor(sunModeTextColor);
        sunModeTextPreView.setBackgroundColor(sunModeTextBackground);
        moonModeTextPreView.setTextColor(moonModeTextColor);
        moonModeTextPreView.setBackgroundColor(moonModeTextBackground);
        
        sunModeImageViewTextColor.setBackgroundColor(sunModeTextColor);
        sunModeImageViewTextBackground.setBackgroundColor(sunModeTextBackground);
        moonModeImageViewTextColor.setBackgroundColor(moonModeTextColor);
        moonnModeImageViewTextBackground.setBackgroundColor(moonModeTextBackground);
        
        mSeekBar.setProgress(textSize);
        ((RadioButton) modeRadioGroup.getChildAt(Setting.getTextModePosition(textMode))).setChecked(true);
        ((RadioButton) langRadioGroup.getChildAt(textLanguage)).setChecked(true);
        ((RadioButton) directionRadioGroup.getChildAt(readingDirection)).setChecked(true);
        ((RadioButton) tapRadioGroup.getChildAt(clickToNextPage)).setChecked(true);
        ((RadioButton) stopSleepRadioGroup.getChildAt(textLanguage)).setChecked(true);
        ((RadioButton) themeRadioGroup.getChildAt(appTheme)).setChecked(true);
        ((RadioButton) articleAdTypeRadioGroup.getChildAt(articleAdType)).setChecked(true);
        
        modeRadioGroup.setOnCheckedChangeListener(this);
        langRadioGroup.setOnCheckedChangeListener(this);
        directionRadioGroup.setOnCheckedChangeListener(this);
        tapRadioGroup.setOnCheckedChangeListener(this);
        stopSleepRadioGroup.setOnCheckedChangeListener(this);
        themeRadioGroup.setOnCheckedChangeListener(this);
        articleAdTypeRadioGroup.setOnCheckedChangeListener(this);

        dbResetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showDbResetDialog();
            }
        });

        sunModeImageViewTextColor.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showTextColorPicker(sunModeTextColor,sunModeImageViewTextColor, sunModeTextPreView);
            }
        });

        sunModeImageViewTextBackground.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showTextBackgroundPicker(sunModeTextBackground,sunModeImageViewTextBackground, sunModeTextPreView );
            }
        });
        
        moonModeImageViewTextColor.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showTextColorPicker(moonModeTextColor,moonModeImageViewTextColor, moonModeTextPreView);
            }
        });

        moonnModeImageViewTextBackground.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showTextBackgroundPicker(moonModeTextBackground, moonnModeImageViewTextBackground, moonModeTextPreView);
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                moonModeTextPreView.setTextSize(progress);
                sunModeTextPreView.setTextSize(progress);
                isSettingChanged = true;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        setFinishDialog();

    }

    private void showTextColorPicker(int modeColor, final ImageView imageView, final TextView textPreview) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, modeColor, new OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {      	
            	isSettingChanged = true;
                
            	imageView.setBackgroundColor(color);
            	textPreview.setTextColor(color);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // cancel was selected by the user
            }
        });
        dialog.show();
    }

    private void showTextBackgroundPicker(int modeColor, final ImageView imageView, final TextView textPreview) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, modeColor, new OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
            	isSettingChanged = true;
                
            	imageView.setBackgroundColor(color);
            	textPreview.setBackgroundColor(color);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // cancel was selected by the user
            }
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
        case android.R.id.home:
            if(isSettingChanged)
                finishDialog.show();
            else
            	finish();
            break;
        }
        return true;
    }

    private void saveRadioGroupValue(RadioGroup theRadioGroup, String key) {

        int radioButtonID = theRadioGroup.getCheckedRadioButtonId();
        View radioButton = theRadioGroup.findViewById(radioButtonID);
        int idx = theRadioGroup.indexOfChild(radioButton);
        Setting.saveSetting(key, idx, SettingActivity.this);

    }
    
    private void saveModeRadioGroup(){
    	int radioButtonID = modeRadioGroup.getCheckedRadioButtonId();
    	View radioButton = modeRadioGroup.findViewById(radioButtonID);
        int idx = modeRadioGroup.indexOfChild(radioButton);
        if(idx == 0){
        	Setting.saveSetting(Setting.keyMode, Setting.keySunMode, SettingActivity.this);
        }else{
        	Setting.saveSetting(Setting.keyMode, Setting.keyMoonMode, SettingActivity.this);
        }
    }
    private void saveModeColor() {
    	 sunModeTextBackground = ((ColorDrawable)sunModeImageViewTextBackground.getBackground()).getColor();
         sunModeTextColor = ((ColorDrawable)sunModeImageViewTextColor.getBackground()).getColor();
         moonModeTextBackground = ((ColorDrawable)moonnModeImageViewTextBackground.getBackground()).getColor();
         moonModeTextColor = ((ColorDrawable)moonModeImageViewTextColor.getBackground()).getColor();
         Setting.saveSetting(Setting.keySunMode, sunModeTextBackground + "," + sunModeTextColor , SettingActivity.this);
         Setting.saveSetting(Setting.keyMoonMode, moonModeTextBackground + "," + moonModeTextColor , SettingActivity.this);
	}

    private void showDbResetDialog() {
        new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.reset_db_hint))
                .setMessage(getResources().getString(R.string.reset_db_message))
                .setPositiveButton(getResources().getString(R.string.yes_string), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	isSettingChanged = true;
                    	
                        SQLiteNovel db = new SQLiteNovel(SettingActivity.this);
                        boolean reset = db.resetDB();
                        if (reset) {
                            Toast.makeText(SettingActivity.this, getResources().getString(R.string.reset_db_success), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SettingActivity.this, getResources().getString(R.string.reset_db_fail), Toast.LENGTH_LONG).show();
                        }
                        db.close();
                    }
                }).setNegativeButton(getResources().getString(R.string.report_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();

    }

    private void setFinishDialog() {
        finishDialog = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.setting_living))
                .setMessage(getResources().getString(R.string.setting_message))
                .setPositiveButton(getResources().getString(R.string.setting_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Setting.saveSetting(Setting.keyTextSize, mSeekBar.getProgress(), SettingActivity.this);
                        saveModeRadioGroup();
                        saveModeColor();
                        saveRadioGroupValue(langRadioGroup, Setting.keyTextLanguage);
                        saveRadioGroupValue(directionRadioGroup, Setting.keyReadingDirection);
                        saveRadioGroupValue(tapRadioGroup, Setting.keyClickToNextPage);
                        saveRadioGroupValue(stopSleepRadioGroup, Setting.keyStopSleeping);
                        saveRadioGroupValue(themeRadioGroup, Setting.keyAppTheme);
                        saveRadioGroupValue(articleAdTypeRadioGroup, Setting.keyArticleAdType);
                        finish();

                    }
                }).setNeutralButton(getResources().getString(R.string.setting_neutral), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setNegativeButton(getResources().getString(R.string.setting_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }

    @Override
    public void onBackPressed() {
    	if(isSettingChanged)
    		finishDialog.show();
    	else
    		finish();
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

	@Override
	public void onCheckedChanged(RadioGroup arg0, int arg1) {
		isSettingChanged = true;
	}

}