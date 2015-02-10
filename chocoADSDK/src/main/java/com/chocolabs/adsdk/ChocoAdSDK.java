package com.chocolabs.adsdk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.chocolabs.adsdk.account.AdsInfo;
import com.chocolabs.adsdk.account.UserInfo;
import com.chocolabs.adsdk.config.AdsConfig;
import com.chocolabs.adsdk.utils.DateUtil;
import com.chocolabs.adsdk.utils.HttpPostOOMAsyncTask;

import android.content.Context;
import android.util.Log;

/**
 * @version 1.1.0
 * @author	TakumaLee
 * 
 * */
public class ChocoAdSDK {
	private static final String TAG = ChocoAdSDK.class.getSimpleName();
	
	private Context context;
	private static ChocoAdSDK instance = null;
	private String APP_KEY = null;
	private StringBuilder keywords = new StringBuilder();
	private UserInfo userInfo;
	public List<AdsInfo> adsInfos = new ArrayList<AdsInfo>();
	public AdsInfo tmpAdsInfo = null;
	
	private ChocoAdSDK(Context context, String appKey, String appName) {
		this.context = context;
		this.APP_KEY = appKey;
		AdsConfig.initConfig(context, appName);
		userInfo = new UserInfo(context, APP_KEY);
		adsInfos.clear();
	}
	
	public static ChocoAdSDK initSingleton(Context context, String appKey, String appName) {
		if (instance == null && context != null) {
			Context appContext = context.getApplicationContext();
			instance = new ChocoAdSDK(appContext, appKey, appName);
		}
		return instance;
	}
	
	public static ChocoAdSDK getInstance() {
		return instance;
	}
	
	public void getKeyword(final KeywordInterface keywordInterface) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					String result = null;
					JSONObject object = new JSONObject();
					object.put("app_id", APP_KEY);
					object.put("ad_id", userInfo.getAdId());
					object.put("version", AdsConfig.getAppVersion());
					object.put("timestamp", DateUtil.getFormateDateWithTime(System.currentTimeMillis()));
					result = new HttpPostOOMAsyncTask(object, true, 0).execute(AdsConfig.INSIGHT_API).get();
					parserKeywords(result);
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} finally {
					keywordInterface.loadComplete();
				}
				
			}
		}).start();
		
	}
	
	private void parserKeywords(String result) throws JSONException {
		JSONObject object = new JSONObject(result);
		JSONArray keywordArray = object.getJSONArray("keywords");
		parserKeywordObjects(keywordArray);
	}
	
	private void parserKeywordObjects(JSONArray array) throws JSONException {
		for (int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);
			AdsInfo adsInfo = new AdsInfo();
			adsInfo.setAccountId(object.getString("account_id"));
			adsInfo.setAdvertisementId(object.getString("advertising_id"));
			adsInfo.setAdvertisementType(object.getString("advertising_type"));
			adsInfo.setHashKeyToken(object.getString("hash_key"));
			adsInfo.setLineId(object.getString("creative_id"));
			adsInfo.setPositionId(object.getString("position_id"));
			adsInfo.setCategory(object.getString("category"));
			adsInfo.setSubcategory(object.getString("subcategory"));
			adsInfo.setCampaign(object.getString("campaign"));
			adsInfo.setTag(object.getString("tag"));
			adsInfos.add(adsInfo);
			if (i > 0)
				keywords.append(",");
			keywords.append(adsInfo.getHashKeyToken());
		}
	}
	
	public boolean isAdDisplay(String lineId) {
		for (int i = 0; i < adsInfos.size(); i++) {
			if (adsInfos.get(i).getLineId().equals(lineId)) {
				tmpAdsInfo = adsInfos.get(i);
				return true;
			}
		}
		tmpAdsInfo = new AdsInfo();
		return false;
	}
	
	/**
	 * @param eventNumber 1 to 5
	 * @param event your event string
	 * 
	 * */
	public void setEvent(int eventNumber, String event) {
		if (eventNumber < 1 || eventNumber > 5) {
			Log.w(TAG, "Event number need in 1 to 5.");
			return;
		}
		userInfo.getEvent()[eventNumber - 1] = event;
	}

	public String getAPP_KEY() {
		return APP_KEY;
	}
	
	/**
	 * @param userInfo update for new user data
	 * 
	 * */
	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public StringBuilder getKeywords() {
		return keywords;
	}

}
