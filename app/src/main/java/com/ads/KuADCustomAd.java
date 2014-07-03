package com.ads;

import android.app.Activity;
import android.util.Log;

import com.google.ads.AdSize;
import com.google.ads.mediation.MediationAdRequest;
import com.google.ads.mediation.customevent.CustomEventBanner;
import com.google.ads.mediation.customevent.CustomEventBannerListener;
import com.kuad.KuBanner;
import com.kuad.kuADListener;

public class KuADCustomAd implements CustomEventBanner {
    
	private KuBanner banner = null;
	
	@Override
	public void destroy() { if (banner != null) {
		banner.destory();
		banner = null; }
	}

	@Override
	public void requestBannerAd(final CustomEventBannerListener listener, final Activity activity, String label, String serverParameter, AdSize adSize,
	MediationAdRequest request, Object customEventExtra) { 
		if (banner != null) {
			banner.destory();
			banner = null; 
		}
		banner = new KuBanner(activity);
		banner.setAutoRefresh(false);
		banner.setAPID(serverParameter);
		banner.setBannerPosition(KuBanner.TOP); // Banner是置頂或置底:KuBanner.TOP or
        
		banner.setkuADListener(new kuADListener() { 
			
			@Override
			public void onRecevie(String arg0) {
				Log.i("onRecevie", "onRecevie -> " + arg0);
				listener.onReceivedAd(banner); 
			}
			
			@Override
			public void onFailedRecevie(String arg0) { Log.i("onFailedRecevie", "showKuAd -> " + arg0); listener.onFailedToReceiveAd();
			} });
		
		banner.appStart();
	
	}


}