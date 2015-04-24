package com.chocolabs.adsdk.utils;

import android.content.Context;
import android.util.Log;

import com.chocolabs.adsdk.ChocoAdSDK;
import com.chocolabs.adsdk.account.AdsInfo;
import com.chocolabs.adsdk.account.UserInfo;
import com.chocolabs.adsdk.config.AdsConfig;
import com.chocolabs.adsdk.config.SDKConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class AdTrackUtils {
	private static final String TAG = AdTrackUtils.class.getSimpleName();
    private static String adType = "";
	
	public static void impressionTracking(Context context) {
		sendTrackingData(context, "impression");
	}
	
	public static void clickTracking(Context context) {
		sendTrackingData(context, "click");
	}

    public static void setAdTypeBanner() {
        adType = "banner";
    }

    public static void setAdTypeInterstitial() {
        adType = "interstitial";
    }
	
	private static void sendTrackingData(Context context, String action) {
		JSONObject params = new JSONObject();
		try {
			UserInfo userInfo = ChocoAdSDK.getInstance().getUserInfo();
			userInfo.setBirthday("03/25/1989");
			params.put("app_id", ChocoAdSDK.getInstance().getAPP_KEY());
			params.put("timestamp", DateUtil.getFormateDateWithTime(System.currentTimeMillis()));
			params.put("ad_id", userInfo.getAdId());
			params.put("social_id", userInfo.getSocialId());
			params.put("social_type", userInfo.getSocialType());
			params.put("social_name", userInfo.getSocialName());
			params.put("social_email", userInfo.getSocialEmail());
			params.put("birthday", userInfo.getBirthday());
			if (DateUtil.getFormatDate(userInfo.getBirthday()) == 0)
				params.put("age", null);
			else
				params.put("age", DateUtil.getAgeFromDate(DateUtil.getFormatDate(userInfo.getBirthday())));
			params.put("gender", userInfo.getGender());
			params.put("country", userInfo.getCountry());
			params.put("city", userInfo.getCity());
			params.put("dist", userInfo.getDist());
			params.put("longitude", userInfo.getLongitude());
			params.put("latitude", userInfo.getLatitude());
			params.put("language", Locale.getDefault().getLanguage());
			params.put("device", userInfo.getDevice());
			params.put("os", "android");
			params.put("version", AdsConfig.getAppVersion());
            params.put("ad_type", adType);
			params.put("carrier", userInfo.getCarrier());
			params.put("connection", NetworkUtil.getConnectionStatus(context));
			params.put("backup1", SDKConfig.sdkVersion + "_full");
			params.put("action", action);
			setAdsInfo(params);
			
		} catch (Exception e) {
			Log.w(TAG, e.toString());
			e.printStackTrace();
		}
		Log.i(TAG, params.toString());
		new HttpPostOOMAsyncTask(params, false, 0).execute(AdsConfig.AD_TRACKING);
	}
	
	private static void setAdsInfo(JSONObject params) throws JSONException {
		AdsInfo adsInfo = ChocoAdSDK.getInstance().tmpAdsInfo;
		params.put("advertisement_type", adsInfo.getAdvertisementType());
		params.put("progress", adsInfo.getProgress());
		params.put("percentage", "");
		params.put("line_id", adsInfo.getLineId());
		params.put("campaign", adsInfo.getCampaign());
	}
	
	public static String getDevice() {
    	return android.os.Build.MODEL;
    }
	
	public static String getDeviceVersion(){
		return android.os.Build.VERSION.RELEASE;
	}

}
