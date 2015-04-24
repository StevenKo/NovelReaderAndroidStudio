package com.chocolabs.adsdk.config;

import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.chocolabs.adsdk.ChocoAdSDK;
import com.chocolabs.adsdk.utils.HttpGetOOMAsyncTask;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class AdsConfig {
	private static final String TAG = AdsConfig.class.getSimpleName();
	
	public static final String AD_TRACKING = SDKConfig.BIGDATA + SDKConfig.AD + "tracking";
	public static final String DATA_PROFILE = SDKConfig.BIGDATA + SDKConfig.DATA + "profile";
	public static final String DATA_BEHAVIOR = SDKConfig.BIGDATA + SDKConfig.DATA + "behavior";
	public static final String DATA_PREFERENCE = SDKConfig.BIGDATA + SDKConfig.DATA + "preference";

    public static final String INSIGHT_API = SDKConfig.ITA + SDKConfig.AD + "keywords";
	public static final String IP_URL = "http://ip-api.com/json";
	
	protected static boolean isInDebugMode = false;
	protected static String appVersion = "";
    protected static String appName = "";
	
	public static void initConfig(Context context, String app_name) {
        try {
            PackageInfo infos = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            appVersion = infos.versionName;
//            appVersionCode = infos.versionCode;
            ApplicationInfo appinfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), 0);
            if ((appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                isInDebugMode = true;
            }

            appName = app_name;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
    }
	
	public static void setCountryCity(Context context) {
		try {
			JSONObject object = new JSONObject(new HttpGetOOMAsyncTask(context).execute(IP_URL).get(3000, TimeUnit.MILLISECONDS));
			ChocoAdSDK.getInstance().getUserInfo().setCountry(object.getString("country"));
			ChocoAdSDK.getInstance().getUserInfo().setCity(object.getString("city"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getAppVersion() {
        return appVersion;
    }

    public static String getAppName() {
        return appName;
    }

}
