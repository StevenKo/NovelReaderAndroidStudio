package com.ads;


import android.content.Context;

import com.adbert.AdbertADView;
import com.adbert.AdbertListener;
import com.google.android.gms.ads.AdSize;
import com.mopub.common.util.Views;
import com.mopub.mobileads.CustomEventBanner;

import java.util.Map;

import static com.mopub.mobileads.MoPubErrorCode.NETWORK_NO_FILL;


public class AdbertMopubBanner extends CustomEventBanner {
    private AdbertADView adbertADView;

    @Override
    protected void loadBanner(
            final Context context,
            final CustomEventBannerListener customEventBannerListener,
            final Map<String, Object> localExtras,
            final Map<String, String> serverExtras) {

        adbertADView = new AdbertADView(context);
        String appid = serverExtras.get("appid");
        String appkey = serverExtras.get("appkey");
        adbertADView.setMediationAPPID(appid + "|" + appkey);
        adbertADView.setBannerSize(AdSize.SMART_BANNER);
        adbertADView.setListener(new AdbertListener() {
            @Override
            public void onReceive(String s) {
                customEventBannerListener.onBannerLoaded(adbertADView);
            }

            @Override
            public void onFailedReceive(String s) {
                customEventBannerListener.onBannerFailed(NETWORK_NO_FILL);
            }
        });
        adbertADView.start();
    }

    @Override
    protected void onInvalidate() {
        Views.removeFromParent(adbertADView);
        if (adbertADView != null) {
            adbertADView.destroy();
        }
    }


}