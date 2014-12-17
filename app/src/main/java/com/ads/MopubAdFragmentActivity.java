package com.ads;

import android.view.Display;
import android.widget.RelativeLayout;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import com.novel.reader.NovelReaderBaseActivity;
import com.novel.reader.R;

/**
 * Created by steven on 12/17/14.
 */
public class MopubAdFragmentActivity extends NovelReaderBaseActivity implements MoPubInterstitial.InterstitialAdListener{
    protected MoPubView moPubView;
    private MoPubInterstitial mInterstitial;

    protected void onDestroy() {
        if(moPubView != null)
            moPubView.destroy();
        super.onDestroy();
    }

    public MoPubView setBannerAdView(RelativeLayout bannerAdView) {
        try {
            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth(); // deprecated

            if (width > 320) {
                return getBannerAdRequest(bannerAdView);
            }
        } catch (Exception e) {

        }
        return null;
    }

    private MoPubView getBannerAdRequest(final RelativeLayout bannerAdView) {
        final MoPubView adMobAdView = new MoPubView(this);
        adMobAdView.setAdUnitId(getResources().getString(R.string.mopub_key));
        adMobAdView.setBannerAdListener(new MoPubView.BannerAdListener() {
            @Override
            public void onBannerLoaded(MoPubView banner) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                if (adMobAdView.getParent() == null)
                    bannerAdView.addView(adMobAdView, params);
            }

            @Override
            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {

            }

            @Override
            public void onBannerClicked(MoPubView banner) {

            }

            @Override
            public void onBannerExpanded(MoPubView banner) {

            }

            @Override
            public void onBannerCollapsed(MoPubView banner) {

            }
        });

        adMobAdView.loadAd();

        return adMobAdView;
    }

    public void requestInterstitialAd() {
        mInterstitial = new MoPubInterstitial(this, "0bdafd84edcd4107a314edcd2911eea4");
        mInterstitial.setInterstitialAdListener(this);
        mInterstitial.load();
    }

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        if (interstitial.isReady()) {
            mInterstitial.show();
        } else {
            // Other code
        }
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {

    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {

    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {

    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {

    }
}
