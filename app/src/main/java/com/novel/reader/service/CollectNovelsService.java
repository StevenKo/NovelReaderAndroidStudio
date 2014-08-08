package com.novel.reader.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.novel.reader.MyNovelActivity;
import com.novel.reader.R;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.Novel;
import com.novel.reader.util.NovelReaderUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class CollectNovelsService extends IntentService {

    public CollectNovelsService(String name) {
        super(name);
    }

    public CollectNovelsService() {
        super("CollectNovelsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        NovelReaderUtil.dailyCollectNovelsAlarmSetup(getApplicationContext());

        ArrayList<Novel> novels = NovelAPI.getCollectedNovels(getApplicationContext());
        if(novels.size() == 0)
            return;

        ArrayList<Novel> novelsFromServer = NovelAPI.getCollectNovelsInfoFromServer(novels);
        if (novelsFromServer != null) {
            NovelAPI.updateNovelsInfo(novelsFromServer, getApplicationContext());

            ArrayList<Novel> hasNewArticleNovel = new ArrayList<Novel>();
            for (Novel novel : novelsFromServer) {

                SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
                try {
                    Date date = dateFormat.parse(novel.getLastUpdate());
                    if (novel.getLastViewDate().before(date)) {
                        hasNewArticleNovel.add(novel);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
            if (hasNewArticleNovel.size() > 0)
                createCollectNovelNotification(hasNewArticleNovel);
        }
    }

    private void createCollectNovelNotification(ArrayList<Novel> novels) {

        PendingIntent viewPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MyNovelActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        String content = "";
        for (int i = 0; i < novels.size(); i++) {
            if (i == novels.size() - 1)
                content += "(" + novels.get(i).getName() + ")";
            else
                content += "(" + novels.get(i).getName() + ")" + ",";
        }

        String strFormat = getResources().getString(R.string.collect_notification_ticker);
        String ticker = String.format(strFormat, novels.size());

        strFormat = getResources().getString(R.string.collect_notification_title);
        String title = String.format(strFormat, novels.size());

        Bitmap iconBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.app_icon);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_notify)
                        .setLargeIcon(iconBitmap)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                        .setContentTitle(title)
                        .setContentText(content)
                        .setContentIntent(viewPendingIntent)
                        .setTicker(ticker)
                        .setSound(alarmSound)
                        .setAutoCancel(true);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        int notificationId = 2;
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
