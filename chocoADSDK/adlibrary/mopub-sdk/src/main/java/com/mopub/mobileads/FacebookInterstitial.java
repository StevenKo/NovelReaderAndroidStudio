package com.mopub.mobileads;

import android.content.Context;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

import java.util.Map;

import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_INVALID_STATE;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_NO_FILL;
import static com.mopub.mobileads.MoPubErrorCode.UNSPECIFIED;

/**
 * Tested with Facebook SDK 3.18.1.
 */
public class FacebookInterstitial extends CustomEventInterstitial implements InterstitialAdListener {
    private static final String PLACEMENT_ID_KEY = "placement_id";

    private InterstitialAd mFacebookInterstitial;
    private CustomEventInterstitialListener mInterstitialListener;

    /**
     * CustomEventInterstitial implementation
     */

    @Override
    protected void loadInterstitial(final Context context,
            final CustomEventInterstitialListener customEventInterstitialListener,
            final Map<String, Object> localExtras,
            final Map<String, String> serverExtras) {
        mInterstitialListener = customEventInterstitialListener;

        final String placementId;
        if (extrasAreValid(serverExtras)) {
            placementId = serverExtras.get(PLACEMENT_ID_KEY);
        } else {
            mInterstitialListener.onInterstitialFailed(ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        mFacebookInterstitial = new InterstitialAd(context, placementId);
        mFacebookInterstitial.setAdListener(this);
        mFacebookInterstitial.loadAd();
    }

    @Override
    protected void showInterstitial() {
        if (mFacebookInterstitial != null && mFacebookInterstitial.isAdLoaded()) {
            mFacebookInterstitial.show();
        } else {
            Log.d("MoPub", "Tried to show a Facebook interstitial ad before it finished loading. Please try again.");
        }
    }

    @Override
    protected void onInvalidate() {
        if (mFacebookInterstitial != null) {
            mFacebookInterstitial.destroy();
            mFacebookInterstitial = null;
        }
    }

    /**
     * InterstitialAdListener implementation
     */

    @Override
    public void onAdLoaded(final Ad ad) {
        Log.d("MoPub", "Facebook interstitial ad loaded successfully.");
        mInterstitialListener.onInterstitialLoaded();
    }

    @Override
    public void onError(final Ad ad, final AdError error) {
        Log.d("MoPub", "Facebook interstitial ad failed to load.");
        if (error == AdError.NO_FILL) {
            mInterstitialListener.onInterstitialFailed(NETWORK_NO_FILL);
        } else if (error == AdError.INTERNAL_ERROR) {
            mInterstitialListener.onInterstitialFailed(NETWORK_INVALID_STATE);
        } else {
            mInterstitialListener.onInterstitialFailed(UNSPECIFIED);
        }
    }

    @Override
    public void onInterstitialDisplayed(final Ad ad) {
        Log.d("MoPub", "Showing Facebook interstitial ad.");
        mInterstitialListener.onInterstitialShown();
    }

    @Override
    public void onAdClicked(final Ad ad) {
        Log.d("MoPub", "Facebook interstitial ad clicked.");
        mInterstitialListener.onInterstitialClicked();
    }

    @Override
    public void onInterstitialDismissed(final Ad ad) {
        Log.d("MoPub", "Facebook interstitial ad dismissed.");
        mInterstitialListener.onInterstitialDismissed();
    }

    private boolean extrasAreValid(final Map<String, String> serverExtras) {
        final String placementId = serverExtras.get(PLACEMENT_ID_KEY);
        return (placementId != null && placementId.length() > 0);
    }

    @Deprecated // for testing
    InterstitialAd getInterstitialAd() {
        return mFacebookInterstitial;
    }
}
