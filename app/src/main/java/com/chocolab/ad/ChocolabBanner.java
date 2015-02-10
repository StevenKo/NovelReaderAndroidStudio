package com.chocolab.ad;

import android.content.Context;
import android.util.Log;

import com.chocolabs.adsdk.ads.ChocoAdsView;
import com.mopub.common.util.Views;
import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;

import java.util.Map;

/**
 * Created by steven on 12/17/14.
 */
public class ChocolabBanner extends CustomEventBanner {

    private static final String PLACEMENT_ID_KEY = "placement_id";
    private ChocoAdsView adsView;
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

        adsView = new ChocoAdsView(context);
        adsView.setAdUnitId(placementId);
        adsView.setBannerAdListener(new MoPubView.BannerAdListener() {
            @Override
            public void onBannerLoaded(MoPubView banner) {
                Log.d("MoPub", "Steven Chocolab banner ad loaded successfully. Showing ad...");
                if (mBannerListener != null) {
                    mBannerListener.onBannerLoaded(banner);
                }
            }

            @Override
            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                Log.d("MoPub", "Steven Chocolab banner ad failed to load.");
                if (mBannerListener != null) {
                    mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_NO_FILL);
                }
            }

            @Override
            public void onBannerClicked(MoPubView banner) {

            }

            @Override
            public void onBannerExpanded(MoPubView banner) {
                mBannerListener.onBannerExpanded();
            }

            @Override
            public void onBannerCollapsed(MoPubView banner) {
                mBannerListener.onBannerCollapsed();
            }
        });
        adsView.loadAd();
    }

    @Override
    protected void onInvalidate() {
        if (adsView != null) {
            Views.removeFromParent(adsView);
            adsView.destroy();
            adsView = null;
        }
    }

    private boolean extrasAreValid(final Map<String, String> serverExtras) {
        final String placementId = serverExtras.get(PLACEMENT_ID_KEY);
        return (placementId != null && placementId.length() > 0);
    }
}
