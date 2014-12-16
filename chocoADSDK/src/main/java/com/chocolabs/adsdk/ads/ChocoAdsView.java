package com.chocolabs.adsdk.ads;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

import com.chocolabs.adsdk.ChocoAdSDK;
import com.chocolabs.adsdk.KeywordInterface;
import com.chocolabs.adsdk.account.AdsInfo;
import com.chocolabs.adsdk.utils.AdTrackUtils;
import com.mopub.mobileads.AdViewController;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.MoPubView.BannerAdListener;

public class ChocoAdsView extends MoPubView implements BannerAdListener, KeywordInterface {
	private static final String TAG = ChocoAdsView.class.getSimpleName();
	private Context context;
	private boolean hasKeywordBack = false;
	private String customKeyResult = null;
	private StringBuilder customKeyword = new StringBuilder();
	private Handler mHandler = new Handler();

	public ChocoAdsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initView();
	}

	public ChocoAdsView(Context context) {
		super(context);
		this.context = context;
		initView();
	}
	
	private void initView() {
		setBannerAdListener(this);
	}
	
	public void putCustomKeyword(int value) {
		addCustomKey(String.valueOf(value));
		this.customKeyResult = customKeyword.toString();
	}
	
	private void addCustomKey(String value) {
		if (customKeyword.length() > 0)
			customKeyword.append(",");
		customKeyword.append("custom:" + value);
	}
	
	@Override
	protected void adLoaded() {
		super.adLoaded();
		Log.w(TAG, "adLoaded()");
		try {
			String clickUrl = ChocoAdsParser.clickUrlParser(mAdViewController.getResponseString());
			String responseUrl = ChocoAdsParser.htmlParser(mAdViewController.getResponseString());
			if (!ChocoAdSDK.getInstance().isAdDisplay(responseUrl)) {
				ChocoAdSDK.getInstance().tmpAdsInfo.setCampaign(clickUrl);
				ChocoAdSDK.getInstance().tmpAdsInfo.setLineId(responseUrl);
			}
			AdTrackUtils.impressionTracking(context);
		} catch (Exception e) {
		}
	}
	
	@Override
	protected void adFailed(MoPubErrorCode errorCode) {
		super.adFailed(errorCode);
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
	public void loadAd() {
		if (hasKeywordBack) {
			setKeywords(customKeyResult);
			super.loadAd();
		} else {
			ChocoAdSDK.getInstance().getKeywords().delete(0, ChocoAdSDK.getInstance().getKeywords().length());
			ChocoAdSDK.getInstance().getKeyword(this);
		}
		
	}
	
	@Override
	protected void adClicked() {
		super.adClicked();
		Log.w(TAG, "adClicked()");
		try {
			String clickUrl = ChocoAdsParser.clickUrlParser(mAdViewController.getResponseString());
			String responseUrl = ChocoAdsParser.htmlParser(mAdViewController.getResponseString());
			if (!ChocoAdSDK.getInstance().isAdDisplay(responseUrl)) {
				ChocoAdSDK.getInstance().tmpAdsInfo.setCampaign(clickUrl);
				ChocoAdSDK.getInstance().tmpAdsInfo.setLineId(responseUrl);
			}
			AdTrackUtils.clickTracking(context);
		} catch (Exception e) {
		}
	}
	
	
	
	@Override
	protected void adClosed() {
		super.adClosed();
	}
	
	public void onResume() {
		this.setAutorefreshEnabled(true);
	}
	
	public void onStop() {
		this.setAutorefreshEnabled(false);
	}

	@Override
	public void onBannerClicked(MoPubView moPubView) {
	}

	@Override
	public void onBannerCollapsed(MoPubView moPubView) {
	}

	@Override
	public void onBannerExpanded(MoPubView moPubView) {
	}

	@Override
	public void onBannerFailed(MoPubView moPubView, MoPubErrorCode errorCode) {
		
	}

	@Override
	public void onBannerLoaded(MoPubView moPubView) {
		Log.w(TAG, "onBannerLoaded()");
		
	}

	@Override
	public void loadComplete() {
		hasKeywordBack = true;
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				loadAd();
			}
		});
		
	}

}
