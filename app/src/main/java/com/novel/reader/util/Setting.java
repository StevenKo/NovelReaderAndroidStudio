package com.novel.reader.util;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.util.Log;

import com.novel.reader.ArticleActivity;
import com.novel.reader.R;

public class Setting {
    // public static int textSize;
    // public static int textLanguage; // 0 for 繁體, 1 for 簡體
    // public static int readingDirection; // 0 for 直向, 1 for 橫向
    // public static int clickToNextPage; // 0 for yes, 1 for no
    // public static int stopSleeping; // 0 for yes, 1 for no

    public final static String                    keyRemindLeaving        = "Leaving";
    public final static Boolean                   initialReindLeaving     = true;

    public final static String                    keyPref                 = "pref";
    public final static String                    keyTextSize             = "TextSize";
    public final static String                    keyTextLanguage         = "TextLanguage";
    public final static String                    keyReadingDirection     = "ReadingDirection";
    public final static String                    keyClickToNextPage      = "ClickToNextPage";
    public final static String                    keyStopSleeping         = "StopSleeping";
    public final static String                    keyOpenDownloadPage     = "OpenDownloadPage";
    public final static String                    keyAppTheme             = "AppTheme";
    public final static String                    keyArticleAdType        = "ArticleAdType";
    public final static String                    keyUpdateAppVersion     = "UpdateAppVersion";
    public final static String                    keyYearSubscription     = "YearSubscription";
    public final static String keySunMode = "SunModeSetting";
    public final static String keyMoonMode = "MoonModeSetting";
    public final static String keyMode = "ModeSetting"; // 0 for SunMode, 1 for MoonMode

    public final static int                       initialTextSize         = 20;                // textsize in pixel
    public final static int                       initialTextLanguage     = 0;                 // 0 繁體, 1 簡體
    public final static int                       initialReadingDirection = 0;
    public final static int                       initialClickToNextPage  = 1;
    public final static int                       initialStopSleeping     = 1;
    public final static int                       initialOpenDownloadPage = 0;
    public final static int                       initialTextColor        = -16777216;
    public final static int                       initialAppTheme         = 0;                 // 0 for 亮白, 1 for 灰黑
    public final static int                       InterstitialAd          = 0;                // 0 for InterstitialAd, 1 for Banner
    public final static int                       BannerAd                = 1;               
    
    public final static int TEXT_CHINA = 1;

    private static final HashMap<String, Integer> initMap                 = new HashMap<String, Integer>() {
                                                                              {
                                                                                  put(keyTextSize, initialTextSize);
                                                                                  put(keyTextLanguage, initialTextLanguage);
                                                                                  put(keyReadingDirection, initialReadingDirection);
                                                                                  put(keyClickToNextPage, initialClickToNextPage);
                                                                                  put(keyStopSleeping, initialStopSleeping);
                                                                                  put(keyOpenDownloadPage, initialOpenDownloadPage);
                                                                                  put(keyAppTheme, initialAppTheme);
                                                                                  put(keyArticleAdType,InterstitialAd);
                                                                                  put(keyUpdateAppVersion,0);
                                                                                  put(keyYearSubscription,0);
                                                                              }
                                                                          };
                                                                        // WHITE is -1, Black is -16777216
     private static final HashMap<String, String> initStringMap          = new HashMap<String, String>() {
                                                                              {
                                                                                  put(keySunMode, "-1,-16777216");
                                                                                  put(keyMoonMode, "-16777216,-1");
                                                                                  put(keyMode,keySunMode);
                                                                              }
                                                                          };
                                             
	

    public static int getSettingInt(String settingKey, Context context) {
        SharedPreferences sharePreference = context.getSharedPreferences(keyPref, 0);
        int settingValue = sharePreference.getInt(settingKey, initMap.get(settingKey));
        return settingValue;
    }
    
    public static String getSettingString(String settingKey, Context context) {
        SharedPreferences sharePreference = context.getSharedPreferences(keyPref, 0);
        String settingValue = sharePreference.getString(settingKey, initStringMap.get(settingKey));
        return settingValue;
    }
    
    public static void saveSetting(String settingKey, String settingValue, Context context){
    	SharedPreferences sharePreference = context.getSharedPreferences(keyPref, 0);
        sharePreference.edit().putString(settingKey, settingValue).commit();
    }

    public static void saveSetting(String settingKey, int settingValue, Context context) {
        SharedPreferences sharePreference = context.getSharedPreferences(keyPref, 0);
        sharePreference.edit().putInt(settingKey, settingValue).commit();
    }

    public static Boolean getSettingRemind(Context context) {
        SharedPreferences sharePreference = context.getSharedPreferences(keyPref, 0);
        Boolean settingValue = sharePreference.getBoolean(keyRemindLeaving, initialReindLeaving);
        return settingValue;
    }

    public static void saveSettingRemind(Boolean value, Context context) {
        SharedPreferences sharePreference = context.getSharedPreferences(keyPref, 0);
        sharePreference.edit().putBoolean(keyRemindLeaving, value).commit();
    }

    public static void setApplicationActionBarTheme(Activity activity) {
        int theme = getSettingInt(keyAppTheme, activity);
        switch (theme) {
        case 0:
            activity.setTheme(R.style.Theme_AppCompat_Light);
            return;
        case 1:
            activity.setTheme(R.style.Theme_AppCompat_Light_DarkActionBar);
            // try {
            // LinearLayout layout = (LinearLayout) activity.findViewById(R.id.adonView);
            // layout.setBackgroundColor(activity.getResources().getColor(R.color.black));
            // } catch (Exception e) {
            //
            // }
            return;
        }
    }
    
    // gcm use start from here
    
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String PROPERTY_DEVICE_ID = "device_id";
	static final String TAG = "GCM_kos";
	
	/**
     * Default lifespan (7 days) of a reservation until it is considered expired.
     */
    public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;
    
    public static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";


    
    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.length() == 0) {
            Log.v(TAG, "Registration not found.");
            return "";
        }
        // check if app was updated; if so, it must clear registration id to
        // avoid a race condition if GCM sends a message
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion || isRegistrationExpired(context)) {
            Log.v(TAG, "App version changed or registration expired.");
            return "";
        }
        return registrationId;
    }
    
    public static int getRegisteredVersion(Context context){
    	 final SharedPreferences prefs = getGCMPreferences(context);
    	 return prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
    }
    
    public static String getDeviceId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String deviceId = prefs.getString(PROPERTY_DEVICE_ID, "");
        return deviceId;
    }
    
    public static SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(Setting.keyPref, 0);
    }
    
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
    private static boolean isRegistrationExpired(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        // checks if the information is not stale
        long expirationTime =
                prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
        return System.currentTimeMillis() > expirationTime;
    }
    
    public static int getTextModePosition(String mode) {
		if (mode == Setting.keySunMode)
			return 0;
		else
			return 1;
	}
    

	public static int getBackgroundModeBackgroundColor(String keyMode, Context context) {
    	String sunSetting = getSettingString(keyMode, context);
    	return Integer.parseInt(sunSetting.split(",")[0]);
	}
    
	public static int getBackgroundModeTextColor(String keyMode, Context context) {
    	String sunSetting = getSettingString(keyMode, context);
    	return Integer.parseInt(sunSetting.split(",")[1]);
	}

}
