package com.chocolabs.adsdk.ads;

import android.app.Activity;
import android.util.Log;

import com.chocolabs.adsdk.ChocoAdSDK;
import com.chocolabs.adsdk.utils.AdTrackUtils;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;;

public class ChocoAdsInterstitial extends MoPubInterstitial implements InterstitialAdListener {
	private static final String TAG = ChocoAdsInterstitial.class.getSimpleName();
	Activity activity;
	private String customKeyResult = null;
	private StringBuilder customKeyword = new StringBuilder();

	public ChocoAdsInterstitial(Activity activity, String id) {
		super(activity, id);
		this.activity = activity;
		setInterstitialAdListener(this);
	}
	
	public void putCustomKeyword(int value) {
		addCustomKey(String.valueOf(value));
		this.customKeyResult = customKeyword.toString();
	}
	
	public void putCustomKeyword(String value) {
		if (null == value || value.isEmpty())
			return;
		addCustomKey(value);
		this.customKeyResult = customKeyword.toString();
	}
	
	private void addCustomKey(String value) {
		if (customKeyword.length() > 0)
			customKeyword.append(",");
		customKeyword.append("custom:" + ChocoAdSDK.getInstance().getAPP_KEY() + "_" + value);
	}
	
	@Override
	public void setKeywords(String keywords) {
		if (!ChocoAdSDK.getInstance().getUserInfo().isLimitTrackingEnabled()) {
			if (keywords != null) {
				if (ChocoAdSDK.getInstance().getKeywords().length() > 0)
					ChocoAdSDK.getInstance().getKeywords().append(",");
				ChocoAdSDK.getInstance().getKeywords().append(keywords);
			}
			Log.w(TAG, ChocoAdSDK.getInstance().getKeywords().toString());
			super.setKeywords(ChocoAdSDK.getInstance().getKeywords().toString());
		} else {
			super.setKeywords(keywords);
		}
	}
	
	@Override
	public void load() {
		setKeywords(customKeyResult);
		super.load();
	}

	@Override
	public void onInterstitialClicked(MoPubInterstitial moPubInterstitial) {
		Log.w(TAG, "onInterstitialClicked()");
        AdTrackUtils.setAdTypeInterstitial();
		AdTrackUtils.clickTracking(activity);
	}

	@Override
	public void onInterstitialDismissed(MoPubInterstitial moPubInterstitial) {
		
	}

	@Override
	public void onInterstitialFailed(MoPubInterstitial moPubInterstitial, MoPubErrorCode errorCode) {
		Log.w(TAG, "onInterstitialFailed()");
	}

	@Override
	public void onInterstitialLoaded(MoPubInterstitial moPubInterstitial) {
		Log.w(TAG, "onInterstitialLoaded()");
	}

	@Override
	public void onInterstitialShown(MoPubInterstitial moPubInterstitial) {
		Log.w(TAG, "onInterstitialShown()");
		AdTrackUtils.impressionTracking(activity);
//		String responseUrl = ChocoAdsParser.htmlParser(this.getMoPubInterstitialView..getResponseString());
//		if (ChocoAdSDK.getInstance().isAdDisplay(responseUrl)) {
//			Utils.impressionTracking(context);
//		}
	}

}
