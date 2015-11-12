package com.vpon.ad;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.ads.AdSize;
import com.mopub.common.util.Views;
import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPubErrorCode;
import com.vpadn.ads.VpadnAd;
import com.vpadn.ads.VpadnAdListener;
import com.vpadn.ads.VpadnAdRequest;
import com.vpadn.ads.VpadnAdSize;
import com.vpadn.ads.VpadnBanner;

import java.util.HashSet;
import java.util.Map;

/**
 * Created by steven on 12/17/14.
 */
public class VponBanner extends CustomEventBanner {

    private static final String PLACEMENT_ID_KEY = "placement_id";
    private VpadnBanner vponBanner = null;
    private CustomEventBannerListener mBannerListener;

    @Override
    protected void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        mBannerListener = customEventBannerListener;


        final String placementId;
        if (extrasAreValid(serverExtras)) {
            placementId = serverExtras.get(PLACEMENT_ID_KEY);
        } else {
            mBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        requestBannerAd(null, (Activity) context, "", placementId, AdSize.SMART_BANNER, new VpadnAdRequest(),null);
    }

    @Override
    protected void onInvalidate() {
        if (vponBanner != null) {
            Views.removeFromParent(vponBanner);
            vponBanner.destroy();
            vponBanner = null;
        }
    }

    public void requestBannerAd(com.google.ads.mediation.customevent.CustomEventBannerListener customEventBannerListener, Activity activity, String s, String serverParameter, AdSize adSize, VpadnAdRequest mediationAdRequest, Object o) {
        if (vponBanner != null) {
            vponBanner.destroy();
            vponBanner = null;
        }
        //serverParameter = "8a80818237562d25013759f861ed0169";
        VpadnAdRequest adRequest = mediationAdRequest;

        HashSet<String> testDeviceImeiSet = new HashSet<String>();
        testDeviceImeiSet.add("357138052176820");
        adRequest.setTestDevices(testDeviceImeiSet);

        //
        vponBanner = new VpadnBanner(activity, serverParameter, getVponAdSizeByAdSize(adSize), "TW");


        vponBanner.setAdListener(new VpadnAdListener() {

            @Override
            public void onVpadnDismissScreen(VpadnAd arg0) {

            }

            @Override
            public void onVpadnFailedToReceiveAd(VpadnAd arg0, VpadnAdRequest.VpadnErrorCode arg1) {
                Log.d("MoPub", "Vpon banner ad failed to load.");
                if (mBannerListener != null) {
                    mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_NO_FILL);
                }
            }

            @Override
            public void onVpadnLeaveApplication(VpadnAd arg0) {
                mBannerListener.onLeaveApplication();
            }

            @Override
            public void onVpadnPresentScreen(VpadnAd arg0) {

            }

            @Override
            public void onVpadnReceiveAd(VpadnAd arg0) {
                Log.d("MoPub", "Vpon banner ad loaded successfully. Showing ad...");
                if (mBannerListener != null) {
                    mBannerListener.onBannerLoaded(vponBanner);
                }
            }

        });

        vponBanner.loadAd(adRequest);
    }

    private boolean extrasAreValid(final Map<String, String> serverExtras) {
        final String placementId = serverExtras.get(PLACEMENT_ID_KEY);
        return (placementId != null && placementId.length() > 0);
    }

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
}
