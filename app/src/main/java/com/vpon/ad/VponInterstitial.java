package com.vpon.ad;

import android.content.Context;
import android.util.Log;

import com.mopub.mobileads.CustomEventInterstitial;
import com.vpadn.ads.VpadnAd;
import com.vpadn.ads.VpadnAdListener;
import com.vpadn.ads.VpadnAdRequest;
import com.vpadn.ads.VpadnInterstitialAd;

import java.util.Map;

import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_NO_FILL;


/**
 * Created by steven on 12/17/14.
 */
public class VponInterstitial extends CustomEventInterstitial {

    private static final String PLACEMENT_ID_KEY = "placement_id";
    private VpadnInterstitialAd interstitialAd = null;
    private CustomEventInterstitialListener mInterstitialListener;

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


        interstitialAd = new VpadnInterstitialAd((android.app.Activity) context, placementId, "TW");
        interstitialAd.setAdListener(new VpadnAdListener() {

            @Override
            public void onVpadnDismissScreen(VpadnAd arg0) {
                if (interstitialAd != null) {
                    interstitialAd.destroy();
                    interstitialAd = null;
                }
                Log.d("MoPub", "Vpon interstitial ad dismissed.");
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialDismissed();
                }
            }

            @Override
            public void onVpadnFailedToReceiveAd(VpadnAd arg0, VpadnAdRequest.VpadnErrorCode arg1) {
                if (interstitialAd != null) {
                    interstitialAd.destroy();
                    interstitialAd = null;
                }
                Log.d("MoPub", "Vpon interstitial ad failed to load.");
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialFailed(NETWORK_NO_FILL);
                }
            }

            @Override
            public void onVpadnLeaveApplication(VpadnAd arg0) {
                mInterstitialListener.onLeaveApplication();
            }

            @Override
            public void onVpadnPresentScreen(VpadnAd arg0) {
                Log.d("MoPub", "onVpadnPresentScreen interstitial ad.");
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialShown();
                }
            }

            @Override
            public void onVpadnReceiveAd(VpadnAd arg0) {
                Log.d("MoPub", "Vpon interstitial ad loaded successfully.");
                if (mInterstitialListener != null) {
                    mInterstitialListener.onInterstitialLoaded();
                }
            }

        });
        interstitialAd.loadAd(new VpadnAdRequest());
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
            interstitialAd.setAdListener(null);
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
