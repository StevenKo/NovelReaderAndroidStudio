package com.chocolabs.adsdk.account;

import java.util.Locale;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.chocolabs.adsdk.config.AdsConfig;
import com.chocolabs.adsdk.utils.AdTrackUtils;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;

public class UserInfo {
	
	private String	appId;
	private String	socialId;
	private String	socialType;
	private String	socialName;
	private String	socialEmail;
	private String	birthday;
	private String	gender;
	private String	country;
	private String	city;
	private double	dist;
	private double	longitude;
	private double	latitude;
	private int		userAge;
	private String	language;
	private String	device;
	private String	os;
	private String	version;
	private String	carrier;
	private String	connection;
	private String[] event;
	private String	adId;	
	private boolean isTrackingEnabled;
	
	public UserInfo(final Context context, final String appHashKey) {
		getCarrier(context);
		appId = appHashKey;
		device = AdTrackUtils.getDevice();
		os = AdTrackUtils.getDeviceVersion();
		event = new String[5];
		AdsConfig.setCountryCity(context);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				setAdIdInBackThread(context);
			}
		}).start();
	}

    public String setAdIdInBackThread(Context context) {
        Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            isTrackingEnabled = adInfo.isLimitAdTrackingEnabled();
            adId = adInfo.getId();
        } catch (Exception e) {
        }
        return  adId;
    }
	
	public String getAppId() {
		return appId;
	}
	private void getCarrier(Context context) {
		setCarrier(((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName());
	}
	public String getSocialId() {
		return socialId;
	}
	/**
	 * @param socialId for user id
	 * 
	 * */
	public void setSocialId(String socialId) {
		this.socialId = socialId;
	}
	public String getSocialType() {
		return socialType;
	}
	/**
	 * @param socialType platform type, like facebook, google+, twitter...
	 * 
	 * */
	public void setSocialType(String socialType) {
		this.socialType = socialType;
	}
	public String getSocialName() {
		return socialName;
	}
	/**
	 * @param socialName user name in social platform
	 * 
	 * */
	public void setSocialName(String socialName) {
		this.socialName = socialName;
	}
	public String getSocialEmail() {
		return socialEmail;
	}
	/**
	 * @param socialEmail user's email in social platform
	 * 
	 * */
	public void setSocialEmail(String socialEmail) {
		this.socialEmail = socialEmail;
	}
	/**
	 * @param birthday type is mm/dd/yyyy
	 * 
	 * */
	public String getBirthday() {
		return birthday;
	}
	/**
	 * @param birthday type is mm/dd/yyyy
	 * 
	 * */
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	/**
	 * @param gender type is male or female
	 * 
	 * */
	public String getGender() {
		return gender;
	}
	/**
	 * @param gender type is male or female
	 * 
	 * */
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getCountry() {
		if (country == null)
			return Locale.getDefault().getCountry();
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCity() {
		if (city == null)
			return "";
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public double getDist() {
		return dist;
	}
	public void setDist(double dist) {
		this.dist = dist;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public int getUserAge() {
		return userAge;
	}
	public void setUserAge(int userAge) {
		this.userAge = userAge;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getCarrier() {
		return carrier;
	}
	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}
	public String getConnection() {
		return connection;
	}
	public void setConnection(String connection) {
		this.connection = connection;
	}
	public String getAdId() {
		return adId;
	}
	public boolean isLimitTrackingEnabled() {
		return isTrackingEnabled;
	}
	public String[] getEvent() {
		return event;
	}

}
