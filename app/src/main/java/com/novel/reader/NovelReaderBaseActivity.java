package com.novel.reader;

import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;

import com.novel.reader.util.Setting;

public class NovelReaderBaseActivity extends ActionBarActivity {

    private int readingDirection;

    @Override
    protected void onResume() {
        super.onResume();
        setReadingDirection();
    }

    private void setReadingDirection() {
        readingDirection = Setting.getSettingInt(Setting.keyReadingDirection, NovelReaderBaseActivity.this);
        if (readingDirection == 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (readingDirection == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }
}
