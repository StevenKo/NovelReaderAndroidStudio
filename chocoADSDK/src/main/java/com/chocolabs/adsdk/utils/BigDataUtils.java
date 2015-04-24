package com.chocolabs.adsdk.utils;

import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.chocolabs.adsdk.ChocoAdSDK;
import com.chocolabs.adsdk.account.UserInfo;
import com.chocolabs.adsdk.config.AdsConfig;
import com.chocolabs.adsdk.config.SDKConfig;

import android.content.Context;
import android.util.Log;

public class BigDataUtils {
	private static final String TAG = BigDataUtils.class.getSimpleName();
	
	public static void sendBigDataProfile(Context context) {
		try {
			JSONObject params = getSendParams(context);
			new HttpPostOOMAsyncTask(params, false, 0).execute(AdsConfig.DATA_PROFILE);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void sendBigDataBehavior(Context context) {
		try {
			JSONObject params = getSendParams(context);
			if (setCustomParamsToJson(params))
				new HttpPostOOMAsyncTask(params, false, 0).execute(AdsConfig.DATA_BEHAVIOR);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void sendBigDataPreference(Context context) {
		try {
			JSONObject params = getSendParams(context);
			if (setCustomParamsToJson(params))
				new HttpPostOOMAsyncTask(params, false, 0).execute(AdsConfig.DATA_PREFERENCE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static boolean setCustomParamsToJson(JSONObject params) throws JSONException {
		UserInfo userInfo = ChocoAdSDK.getInstance().getUserInfo();
		if (checkEvent(userInfo)) {
			setEventValue(params, userInfo);
			return true;
		} else {
			Log.w(TAG, "Event count is not enough, please insert event least 3 events.");
			return false;
		}
	}
	
	private static boolean checkEvent(UserInfo userInfo) {
		for (int i = 0; i < 3; i++) {
			if (userInfo.getEvent()[i] == null) {
				return false;
			}
		}
		return true;
	}
	
	private static void setEventValue(JSONObject params, UserInfo userInfo) throws JSONException {
		String[] eventStrings = userInfo.getEvent();
		params.put("event1", eventStrings[0]);
		params.put("event2", eventStrings[1]);
		params.put("event3", eventStrings[2]);
		params.put("event4", eventStrings[3]);
		params.put("event5", eventStrings[4]);
	}
	
	private static JSONObject getSendParams(Context context) throws JSONException {
		UserInfo userInfo = ChocoAdSDK.getInstance().getUserInfo();
		JSONObject params = new JSONObject();
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
		params.put("dist", "");
		params.put("longitude", "");
		params.put("latitude", "");
		params.put("language", Locale.getDefault().getLanguage());
		params.put("device", userInfo.getDevice());
		params.put("os", "android");
		params.put("version", AdsConfig.getAppVersion());
		params.put("carrier", userInfo.getCarrier());
		params.put("connection", NetworkUtil.getConnectionStatus(context));
		params.put("backup1", SDKConfig.sdkVersion + "_full");
		return params;
	}

}
