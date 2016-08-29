package com.ads;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;

import com.adbert.AdbertADView;
import com.adbert.AdbertInterstitialAD;
import com.adbert.AdbertListener;
import com.adbert.AdbertOrientation;
import com.adbert.ExpandVideoPosition;

import android.content.Context;
import android.os.Bundle;

public class AdbertMediation implements CustomEventBanner, CustomEventInterstitial {

	@Override
	public void onDestroy() {
		if (ad != null) {
			ad.destroy();
			ad = null;
		}
		if (adbertInterstitialAD != null) {
			adbertInterstitialAD.destroy();
			adbertInterstitialAD = null;
		}
	}

	@Override
	public void onPause() {
		if (ad != null) {
			ad.pause();
		}
	}

	@Override
	public void onResume() {
		if (ad != null) {
			ad.resume();
		}
	}

	AdbertADView ad;

	@Override
	public void requestBannerAd(Context context, final CustomEventBannerListener listener, String serverParameter,
			com.google.android.gms.ads.AdSize size, MediationAdRequest mediationAdRequest, Bundle customEventExtras) {
		if (ad != null) {
			ad.destroy();
			ad = null;
		}
		ad = new AdbertADView(context);
		ad.setExpandVideo(ExpandVideoPosition.BOTTOM);
		ad.setMode(AdbertOrientation.NORMAL);
		ad.setFullScreen(false);
		ad.setBannerSize(size);
		ad.setMediationAPPID(serverParameter);
		ad.setListener(new AdbertListener() {
			@Override
			public void onReceive(String arg0) {
				listener.onAdLoaded(ad);
			}

			@Override
			public void onFailedReceive(String arg0) {
				listener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
			}
		});
		ad.start();
	}

	AdbertInterstitialAD adbertInterstitialAD;

	@Override
	public void requestInterstitialAd(Context context, final CustomEventInterstitialListener listener, String serverParameter,
			MediationAdRequest mediationAdRequest, Bundle customEventExtras) {
		if (adbertInterstitialAD != null) {
			adbertInterstitialAD.destroy();
			adbertInterstitialAD = null;
		}
		adbertInterstitialAD = new AdbertInterstitialAD(context);
		adbertInterstitialAD.setMediationAPPID(serverParameter);
		adbertInterstitialAD.setListener(new AdbertListener() {
			@Override
			public void onReceive(String arg0) {
				listener.onAdLoaded();
			}

			@Override
			public void onFailedReceive(String arg0) {
				listener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
			}
		});
		adbertInterstitialAD.loadAd();
	}

	@Override
	public void showInterstitial() {
		if (adbertInterstitialAD != null) {
			adbertInterstitialAD.show();
		}
	}
}