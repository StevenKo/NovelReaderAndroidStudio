package com.chocolabs.adsdk.ads;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class ChocoAdsParser {
	private static final String TAG = ChocoAdsParser.class.getSimpleName();
	
	public static String htmlParser(String html) throws Exception {
		if (html == null)
			return "";
		String result = "";
		if (html.contains("<!-- Adgroup is ")) {
			String url = null;
			Pattern pattern = Pattern.compile("(?is)<!-- Adgroup is (.*?) -->");
			if (html != null) {
				Matcher matcher = pattern.matcher(html);
				if (matcher.find()) {
					url = matcher.group(1);
					
				}
				if (url != null) {
					try {
						result = URLDecoder.decode(url, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
			Log.d(TAG, result);
			
		}
		return result;
	}
	
	public static String clickUrlParser(String html) throws Exception {
		if (html == null)
			return "";
		String result = "";
		if (html.contains("href=\"")) {
			String url = null;
			Pattern pattern = Pattern.compile("(?is)href=\"(.*?)\"");
			if (html != null) {
				Matcher matcher = pattern.matcher(html);
				if (matcher.find()) {
					url = matcher.group(1);
					
				}
				if (url != null) {
					try {
						result = URLDecoder.decode(url, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
			Log.d(TAG, result);
			
		}
		return "url_" + result;
	}


}
