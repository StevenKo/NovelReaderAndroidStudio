package com.ads;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import com.novel.reader.NovelReaderBaseActivity;
import com.novel.reader.R;

import android.view.Display;
import android.widget.RelativeLayout;

/**
 * Created by steven on 12/17/14.
 */
public class MopubAdFragmentActivity extends NovelReaderBaseActivity implements MoPubInterstitial.InterstitialAdListener{
    protected MoPubView moPubView;
    private MoPubInterstitial mInterstitial;

    protected void onResume(){
        super.onResume();
    }

    protected void onPause(){
        super.onPause();
    }

    protected void onDestroy() {
        if(moPubView != null)
            moPubView.destroy();
        if(mInterstitial != null)
            mInterstitial.destroy();
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
        moPubView = new MoPubView(this);
        moPubView.setAdUnitId(getResources().getString(R.string.mopub_key));
        moPubView.setBannerAdListener(new MoPubView.BannerAdListener() {
            @Override
            public void onBannerLoaded(MoPubView banner) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                if (moPubView.getParent() == null)
                    bannerAdView.addView(moPubView, params);
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

        moPubView.loadAd();

        return moPubView;
    }

    public void requestInterstitialAd() {
        mInterstitial = new MoPubInterstitial(this, "ccf6296fccc54758adf23f534175664b");
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
