package com.hodo.ads;
import android.app.Activity;

import com.google.ads.AdSize;
import com.google.ads.mediation.MediationAdRequest;
import com.google.ads.mediation.customevent.CustomEventBanner;
import com.google.ads.mediation.customevent.CustomEventBannerListener;
import com.hodo.HodoADView;
import com.hodo.listener.HodoADListener;
public class HODoADBanner implements CustomEventBanner {
	HodoADView hodoADview;
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		hodoADview.closeBanne();
		
	}
	@Override
	public void requestBannerAd(final CustomEventBannerListener listener, Activity activity,
			String label, String serverParameter, AdSize adSize, MediationAdRequest request,
			Object customEventExtra) {
		// TODO Auto-generated method stub
		hodoADview=new HodoADView(activity);
		hodoADview.setBannerPositionType(HodoADView.BANNER_BOTTOM);
		hodoADview.setAutoRefresh(false);
		hodoADview.setListener(new HodoADListener() {
			@Override
			public void onGetBanner() {
				//¶®•\®˙±obanner
				listener.onReceivedAd(hodoADview);
			}
			@Override
			public void onFailed(String msg) {
			}
			@Override
			public void onBannerChange() {
			}
		});
		//Ω–¥¿¥´±ºßA™∫HODo AD ¥£®—™∫appid
		hodoADview.requestAD("5209e27037e");
	}
}
