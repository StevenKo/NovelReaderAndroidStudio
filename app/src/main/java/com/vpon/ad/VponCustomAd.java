package com.vpon.ad;


import java.util.HashSet;

import android.app.Activity;
import android.util.Log;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.mediation.MediationAdRequest;
import com.google.ads.mediation.customevent.CustomEventBanner;
import com.google.ads.mediation.customevent.CustomEventBannerListener;
import com.google.ads.mediation.customevent.CustomEventInterstitial;
import com.google.ads.mediation.customevent.CustomEventInterstitialListener;
import com.vpadn.ads.VpadnAd;
import com.vpadn.ads.VpadnAdListener;
import com.vpadn.ads.VpadnAdRequest;
import com.vpadn.ads.VpadnAdSize;
import com.vpadn.ads.VpadnBanner;
import com.vpadn.ads.VpadnInterstitialAd;

public class VponCustomAd implements CustomEventBanner, CustomEventInterstitial {

	private VpadnBanner vponBanner = null;
	private VpadnInterstitialAd interstitialAd = null;

	@Override
	public void destroy() {
		if (vponBanner != null) {
			vponBanner.destroy();
			vponBanner = null;
		}
		if (interstitialAd != null) {
			interstitialAd.destroy();
			interstitialAd = null;
		}
	}

	/*
	 * 
	 */
	private VpadnAdSize getVponAdSizeByAdSize(AdSize adSize) {

		if (adSize.equals(AdSize.BANNER)) {
			return VpadnAdSize.BANNER;
		} else if (adSize.equals(AdSize.IAB_BANNER)) {
			return VpadnAdSize.IAB_BANNER;
		} else if (adSize.equals(AdSize.IAB_LEADERBOARD)) {
			return VpadnAdSize.IAB_LEADERBOARD;
		} else if (adSize.equals(AdSize.IAB_MRECT)) {
			return VpadnAdSize.IAB_MRECT;
		} else if (adSize.equals(AdSize.IAB_WIDE_SKYSCRAPER)) {
			return VpadnAdSize.IAB_WIDE_SKYSCRAPER;
		} else if (adSize.equals(AdSize.SMART_BANNER)) {
			return VpadnAdSize.SMART_BANNER;
		}

		boolean isAutoHeight = false;
		boolean isFullWidth = false;
		if (adSize.isAutoHeight()) {
			isAutoHeight = true;
		}
		if (adSize.isFullWidth()) {
			isFullWidth = true;
		}

		if (isAutoHeight && isFullWidth) {
			return VpadnAdSize.SMART_BANNER;
		}

		if (isAutoHeight && !isFullWidth) {
			return new VpadnAdSize(adSize.getWidth(), VpadnAdSize.AUTO_HEIGHT);
		}
		if (!isAutoHeight && isFullWidth) {
			return new VpadnAdSize(VpadnAdSize.FULL_WIDTH, adSize.getHeight());
		}

		if (adSize.isCustomAdSize()) {
			return new VpadnAdSize(adSize.getWidth(), adSize.getHeight());
		}

		return VpadnAdSize.SMART_BANNER;
	}

	/*
	 * 
	 */
	private VpadnAdRequest getVpadnAdRequestByMediationAdRequest(MediationAdRequest request) {

		VpadnAdRequest adRequest = new VpadnAdRequest();
//		if (request.getBirthday() != null)
//			Log.e("VPON", request.getBirthday().toString());
//		if (request.getBirthday() != null) {
//			adRequest.setBirthday(request.getBirthday());
//		}
//		if (request.getAgeInYears() != null) {
//			adRequest.setAge(request.getAgeInYears());
//		}

		if (request.getKeywords() != null) {
			adRequest.setKeywords(request.getKeywords());
		}

		if (request.getGender() != null) {
			if (request.getGender().equals(AdRequest.Gender.FEMALE)) {
				adRequest.setGender(VpadnAdRequest.Gender.FEMALE);
			} else if (request.getGender().equals(AdRequest.Gender.MALE)) {
				adRequest.setGender(VpadnAdRequest.Gender.MALE);
			} else {
				adRequest.setGender(VpadnAdRequest.Gender.UNKNOWN);
			}
		}
		
		return adRequest;
	}

	@Override
	public void requestBannerAd(final CustomEventBannerListener listener, final Activity activity, String label, String serverParameter, AdSize adSize,
			MediationAdRequest request, Object customEventExtra) {

		if (vponBanner != null) {
			vponBanner.destroy();
			vponBanner = null;
		}
		//serverParameter = "8a80818237562d25013759f861ed0169";
		VpadnAdRequest adRequest = getVpadnAdRequestByMediationAdRequest(request);
		
		HashSet<String> testDeviceImeiSet = new HashSet<String>();
	    testDeviceImeiSet.add("357138052176820"); 
	    adRequest.setTestDevices(testDeviceImeiSet);

		// 
		vponBanner = new VpadnBanner(activity, serverParameter, getVponAdSizeByAdSize(adSize), "TW");
		
       
        
		vponBanner.setAdListener(new VpadnAdListener() {

			@Override
			public void onVpadnDismissScreen(VpadnAd arg0) {
				listener.onDismissScreen();
			}

			@Override
			public void onVpadnFailedToReceiveAd(VpadnAd arg0, VpadnAdRequest.VpadnErrorCode arg1) {
				listener.onFailedToReceiveAd();
			}

			@Override
			public void onVpadnLeaveApplication(VpadnAd arg0) {
				listener.onLeaveApplication();
			}

			@Override
			public void onVpadnPresentScreen(VpadnAd arg0) {
				listener.onPresentScreen();
			}

			@Override
			public void onVpadnReceiveAd(VpadnAd arg0) {
				listener.onReceivedAd(vponBanner);
			}

		});

		vponBanner.loadAd(adRequest);
	}

	@Override
	public void requestInterstitialAd(final CustomEventInterstitialListener listener, Activity activity, String label, String serverParameter,
			MediationAdRequest request, Object customEventExtra) {
		Log.d("abcdefg","Call VponCustomAd");

		// 
		interstitialAd = new VpadnInterstitialAd(activity, serverParameter, "TW");
		interstitialAd.setAdListener(new VpadnAdListener() {

			@Override
			public void onVpadnDismissScreen(VpadnAd arg0) {
				if (interstitialAd != null) {
					interstitialAd.destroy();
					interstitialAd = null;
				}
				listener.onDismissScreen();
			}

			@Override
			public void onVpadnFailedToReceiveAd(VpadnAd arg0, VpadnAdRequest.VpadnErrorCode arg1) {
				if (interstitialAd != null) {
					interstitialAd.destroy();
					interstitialAd = null;
				}
				listener.onFailedToReceiveAd();
			}

			@Override
			public void onVpadnLeaveApplication(VpadnAd arg0) {
				listener.onLeaveApplication();
			}

			@Override
			public void onVpadnPresentScreen(VpadnAd arg0) {
				listener.onPresentScreen();
			}

			@Override
			public void onVpadnReceiveAd(VpadnAd arg0) {
				listener.onReceivedAd();
			}

		});
		interstitialAd.loadAd(new VpadnAdRequest());

	}

	@Override
	public void showInterstitial() {
		if (interstitialAd != null && interstitialAd.isReady()) {
			interstitialAd.show();
		}

	}
}
