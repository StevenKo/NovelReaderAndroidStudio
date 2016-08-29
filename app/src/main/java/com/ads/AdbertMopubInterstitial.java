package com.ads;

import com.adbert.AdbertInterstitialAD;
import com.adbert.AdbertListener;
import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPubErrorCode;

import android.content.Context;

import java.util.Map;


public class AdbertMopubInterstitial extends CustomEventInterstitial {

    AdbertInterstitialAD adbertInterstitialAD;

    @Override
    protected void loadInterstitial(
            final Context context,
            final CustomEventInterstitialListener customEventInterstitialListener,
            final Map<String, Object> localExtras,
            final Map<String, String> serverExtras) {
        adbertInterstitialAD = new AdbertInterstitialAD(context);
        String appid = serverExtras.get("appid");
        String appkey = serverExtras.get("appkey");
        adbertInterstitialAD.setMediationAPPID(appid + "|" + appkey);
        adbertInterstitialAD.setListener(new AdbertListener() {
            @Override
            public void onReceive(String msg) {
                customEventInterstitialListener.onInterstitialLoaded();
            }

            @Override
            public void onFailedReceive(String msg) {
                customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);
            }

            @Override
            public void onAdClosed() {
                customEventInterstitialListener.onInterstitialDismissed();
            }
        });
        adbertInterstitialAD.loadAd();

    }

    @Override
    protected void showInterstitial() {
        if (adbertInterstitialAD != null )
            adbertInterstitialAD.show();
    }

    @Override
    protected void onInvalidate() {
        if (adbertInterstitialAD != null)
            adbertInterstitialAD.destroy();
    }

}