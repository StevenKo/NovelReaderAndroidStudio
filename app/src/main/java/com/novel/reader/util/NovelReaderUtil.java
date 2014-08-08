package com.novel.reader.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.novel.reader.service.CollectNovelsService;

import java.io.IOException;
import java.util.Calendar;

public class NovelReaderUtil {

    private static final String LOG_TAG = NovelReaderUtil.class.getSimpleName();


    public static boolean isDisplayDefaultBookCover(String url) {
        if (url.equals("") || url == null || url.equals("null") || url.equals("http://www.bestory.com/pics/0.jpg"))
            return true;

        return false;
    }

    public static String translateTextIfCN(Context context, String string) {

        String retrun_string = null;

        int local = Setting.getSettingInt(Setting.keyTextLanguage, context);
        if (local == Setting.TEXT_CHINA) {
            try {
                retrun_string = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(string);
            } catch (IOException e) {
                retrun_string = string;
            }
        } else {
            retrun_string = string;
        }

        return retrun_string;
    }

    public static void dailyCollectNovelsAlarmSetup(Context context) {

        boolean alarmUp = (PendingIntent.getService(context, 123,
                new Intent(context, CollectNovelsService.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (!alarmUp) {
            int random = Setting.getSettingInt(Setting.keyCollectNotificationRandomNum, context);
            Setting.saveSetting(Setting.keyCollectNotificationRandomNum, random, context);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, random);
            calendar.set(Calendar.SECOND, random);
            calendar.set(Calendar.MILLISECOND, 0);

            PendingIntent pi = PendingIntent.getService(context, 123,
                    new Intent(context, CollectNovelsService.class), PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
            Log.d(LOG_TAG, "AlarmSet");
        }
    }

}
