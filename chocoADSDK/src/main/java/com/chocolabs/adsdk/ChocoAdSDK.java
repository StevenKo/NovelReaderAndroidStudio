package com.chocolabs.adsdk;

import android.content.Context;
import android.util.Log;

import com.chocolabs.adsdk.account.AdsInfo;
import com.chocolabs.adsdk.account.UserInfo;
import com.chocolabs.adsdk.config.AdsConfig;
import com.chocolabs.adsdk.utils.DateUtil;
import com.chocolabs.adsdk.utils.HttpPostOOMAsyncTask;
import com.mopub.IdManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.2.1
 * @author	TakumaLee
 *
 *
 *
 * ░░░░░░░░░░
░░░░░░░░░░▄▄█▀▀▄░░░░
░░░░░░░░▄█████▄▄█▄░░░░
░░░░░▄▄▄▀██████▄▄██░░░░
░░▄██░░█░█▀░░▄▄▀█░█░░░▄▄▄▄
▄█████░░██░░░▀▀░▀░█▀▀██▀▀▀█▀▄
█████░█░░▀█░▀▀▀▀▄▀░░░███████▀
░▀▀█▄░██▄▄░▀▀▀▀█▀▀▀▀▀░▀▀▀▀
░▄████████▀▀▀▄▀░░░░
██████░▀▀█▄░░░█▄░░░░
░▀▀▀▀█▄▄▀░██████▄░░░░
░░░░░░░░░█████████░░░░
太多 bug，只好讓洛克人來殺
 *
 *
 *
 *
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
	public boolean hasKeywordBack = false;
	private KeywordInterface keywordInterface = null;

	private ChocoAdSDK(Context context, String appKey) {
		chocoInit(context, appKey, null);
	}

	private ChocoAdSDK(Context context, String appKey, String appName) {
		chocoInit(context, appKey, appName);
	}

    private void chocoInit(Context context, String appKey, String appName) {
        this.context = context;
        this.APP_KEY = appKey;
        AdsConfig.initConfig(context, appName);
        initManager();
    }

	public static ChocoAdSDK initSingleton(Context context, String appKey) {
		if (instance == null && context != null) {
			Context appContext = context.getApplicationContext();
			instance = new ChocoAdSDK(appContext, appKey);
		}
		return instance;
	}
	
	private void initManager() {
		IdManager.initSingleton(context);
		userInfo = new UserInfo(context, APP_KEY);
        tmpAdsInfo = new AdsInfo();
		adsInfos.clear();
        fetchKeyword();
	}

	public static ChocoAdSDK getInstance() {
		return instance;
	}
	
	public ChocoAdSDK setInmobiBannerId(String adUnit) {
		IdManager.getInstance().setInMobiBannerId(adUnit);
		return instance;
	}
	
	public ChocoAdSDK setInmobiInterstitialId(String adUnit) {
		IdManager.getInstance().setInMobiInterstitialId(adUnit);
		return instance;
	}

	public void setKeywordInterface(KeywordInterface keywordInterface) {
		this.keywordInterface = keywordInterface;
	}

	public void fetchKeyword() {
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					String result = null;
					JSONObject object = new JSONObject();
					object.put("app_id", APP_KEY);
					object.put("ad_id", (userInfo.getAdId() != null) ? userInfo.getAdId() : userInfo.setAdIdInBackThread(context));
					object.put("version", AdsConfig.getAppVersion());
					object.put("timestamp", DateUtil.getFormateDateWithTime(System.currentTimeMillis()));
                    Log.v("getKeyword", object.toString());
					result = new HttpPostOOMAsyncTask(object, true, 0).execute(AdsConfig.INSIGHT_API).get();
                    Log.v("getKeyword", result);
					parserKeywords(result);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					hasKeywordBack = true;
					if (null != keywordInterface)
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
			adsInfo.setAdvertisementId(object.getString("advertising_id"));
			adsInfo.setHashKeyToken(object.getString("hash_key"));
			adsInfos.add(adsInfo);
			if (i > 0)
				keywords.append(",");
			keywords.append(adsInfo.getHashKeyToken());
		}
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
