package com.chocolab.ad;

import android.content.Context;
import android.util.Log;

import com.chocolabs.adsdk.ads.ChocoAdsInterstitial;
import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;

import java.util.Map;

import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_NO_FILL;

/**
 * Created by steven on 12/17/14.
 */
public class ChocolabInterstitial extends CustomEventInterstitial {

    private static final String PLACEMENT_ID_KEY = "placement_id";
    private CustomEventInterstitialListener mInterstitialListener;
    private ChocoAdsInterstitial interstitialAd;

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

        interstitialAd = new ChocoAdsInterstitial((android.app.Activity) context, "afbea19140e745ae8c4e0e182201c720");
        interstitialAd.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial interstitial) {
                Log.d("MoPub", "Steven Chocolab interstitial ad loaded successfully.");
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialLoaded();
                }
            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
                if (interstitialAd != null) {
                    interstitialAd.destroy();
                    interstitialAd = null;
                }
                Log.d("MoPub", "Steven Chocolab interstitial ad failed to load.");
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialFailed(NETWORK_NO_FILL);
                }
            }

            @Override
            public void onInterstitialShown(MoPubInterstitial interstitial) {
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialShown();
                }
            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial interstitial) {
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialClicked();
                }
            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial interstitial) {
                if (interstitialAd != null) {
                    interstitialAd.destroy();
                    interstitialAd = null;
                }
                Log.d("MoPub", "Vpon interstitial ad dismissed.");
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialDismissed();
                }
            }
        });
        interstitialAd.load();
    }

    @Override
    protected void showInterstitial() {
        if (interstitialAd != null && interstitialAd.isReady()) {
            interstitialAd.show();
        }
    }

    @Override
    protected void onInvalidate() {
        if (interstitialAd != null) {
            interstitialAd.setInterstitialAdListener(null);
        }
        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }
    }

    private boolean extrasAreValid(final Map<String, String> serverExtras) {
        final String placementId = serverExtras.get(PLACEMENT_ID_KEY);
        return (placementId != null && placementId.length() > 0);
    }

}
