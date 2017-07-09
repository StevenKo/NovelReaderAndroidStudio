package com.ads;

import com.mopub.mobileads.CustomEventInterstitial;
import com.taiwanmobile.pt.adp.view.TWMAd;
import com.taiwanmobile.pt.adp.view.TWMAdRequest;
import com.taiwanmobile.pt.adp.view.TWMAdViewListener;
import com.taiwanmobile.pt.adp.view.TWMInterstitialAd;

import android.content.Context;
import android.util.Log;

import java.util.Map;

import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_NO_FILL;

/**
 * Created by steven on 09/07/2017.
 */

public class TAMediaMopubInterstitial extends CustomEventInterstitial {

    private static final String PLACEMENT_ID_KEY = "placement_id";
    private CustomEventInterstitialListener mInterstitialListener;
    private TWMInterstitialAd interstitialAd;


    @Override
    protected void loadInterstitial(Context context, CustomEventInterstitialListener customEventInterstitialListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        mInterstitialListener = customEventInterstitialListener;


        final String placementId;
        if (extrasAreValid(serverExtras)) {
            placementId = serverExtras.get(PLACEMENT_ID_KEY);
        } else {
            mInterstitialListener.onInterstitialFailed(ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        interstitialAd = new TWMInterstitialAd((android.app.Activity) context, placementId);

        interstitialAd.setAdListener(new TWMAdViewListener() {

            @Override
            public void onReceiveAd(TWMAd ad) {
                Log.d("MoPub", "TAMediaMopub interstitial ad loaded successfully.");
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialLoaded();
                }
            }

            @Override
            public void onFailedToReceiveAd(TWMAd ad, TWMAdRequest.ErrorCode errorCode) {
                Log.d("MoPub", "TAMediaMopub interstitial ad failed to load.");
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialFailed(NETWORK_NO_FILL);
                }
            }

            @Override
            public void onPresentScreen(TWMAd ad) {
                Log.d("MoPub", "TAMediaMopub  onPresentScreen interstitial ad.");
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialShown();
                }
            }

            @Override
            public void onDismissScreen(TWMAd ad) {
                Log.d("MoPub", "TAMediaMopub interstitial ad dismissed.");
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialDismissed();
                }
            }

            @Override
            public void onLeaveApplication(TWMAd ad) {
                mInterstitialListener.onLeaveApplication();
            }

        });

        interstitialAd.loadAd(new TWMAdRequest());
    }

    @Override
    protected void showInterstitial() {
        Log.d("MoPub", "TAMediaMopub interstitial ad show.");
        if (interstitialAd != null && interstitialAd.isReady()) {
            interstitialAd.show();
        }
    }

    @Override
    protected void onInvalidate() {
        if (interstitialAd != null) {
            interstitialAd.setAdListener(null);
        }
    }

    private boolean extrasAreValid(final Map<String, String> serverExtras) {
        final String placementId = serverExtras.get(PLACEMENT_ID_KEY);
        return (placementId != null && placementId.length() > 0);
    }


}
