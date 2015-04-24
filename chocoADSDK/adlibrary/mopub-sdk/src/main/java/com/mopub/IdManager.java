package com.mopub;

import android.content.Context;

public class IdManager {
	
	private Context context;
	private static IdManager instance = null;
	private String inMobiBannerId = "";
	private String inMobiInterstitialId = "";

	private IdManager(Context context) {
		this.context = context;
	}
	
	public static IdManager initSingleton(Context context) {
		if (instance == null && context != null) {
			Context appContext = context.getApplicationContext();
			instance = new IdManager(appContext);
		}
		return instance;
	}
	
	public static IdManager getInstance() {
		return instance;
	}
	
	public String getInMobiBannerId() {
		return inMobiBannerId;
	}

	public void setInMobiBannerId(String inMobiBannerId) {
		this.inMobiBannerId = inMobiBannerId;
	}

	public String getInMobiInterstitialId() {
		return inMobiInterstitialId;
	}

	public void setInMobiInterstitialId(String inMobiInterstitialId) {
		this.inMobiInterstitialId = inMobiInterstitialId;
	}

}
