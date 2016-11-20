package com.ads;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.novel.reader.NovelReaderBaseActivity;
import com.novel.reader.R;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by steven on 12/17/14.
 */
public class MopubAdFragmentActivity extends NovelReaderBaseActivity{
    protected MopubViewExtend moPubView;

    protected void onResume(){
        super.onResume();
        if(moPubView != null)
            moPubView.callSetAdVisibility(View.VISIBLE);

    }

    protected void onPause(){
        super.onPause();
        if(moPubView != null)
            moPubView.callSetAdVisibility(View.GONE);

    }


    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSPARENT);
    }

    protected void onDestroy() {
        if(moPubView != null)
            moPubView.destroy();
        super.onDestroy();
    }

    public MopubViewExtend setBannerAdView(RelativeLayout bannerAdView) {
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

    private MopubViewExtend getBannerAdRequest(final RelativeLayout bannerAdView) {
        moPubView = new MopubViewExtend(this);
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
}
