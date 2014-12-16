package com.chocolabs.adsdk.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;

public class HttpPostOOMAsyncTask extends AsyncTask<String, Integer, String> {
	
	JSONObject sendParams;
	boolean isHighestPriority = false;
	private int timeout = 30000;
	
	public HttpPostOOMAsyncTask(JSONObject params, boolean isHighest, int timeout) {
		this.sendParams = params;
		this.isHighestPriority = isHighest;
		if (timeout != 0)
			this.timeout = timeout;
	}

	@Override
	protected String doInBackground(String... params) {
		if (isHighestPriority)
			Process.setThreadPriority(-19);
		String url = params[0];
		String result = "";
//		Log.v("Post", sendParams.toString());
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        post.setParams(httpParams);
		post.addHeader("Accept-Language", Locale.getDefault().toString());
//		post.setHeader("Content-Type", "application/json; charset=UTF-8");
		try {
			StringEntity se = new StringEntity(sendParams.toString(), HTTP.UTF_8);
			post.setEntity(se);
			se.setContentType("application/json");
			HttpResponse response = client.execute(post);
			
			HttpEntity entity = response.getEntity();
//			result = EntityUtils.toString(entity);
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()), 23 * 1024);
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		        sb.append(line);
		    }
		    result = sb.toString();
		    Log.v("ResultOnPost", result);
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
	}

}
