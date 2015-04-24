package com.chocolabs.adsdk.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class HttpGetOOMAsyncTask extends AsyncTask<String, Process, String> {
	
	Context context;
	
	public static int iFileSize   = 0;
	public static double dReadSum   = 0;
	public static boolean bIsDownload = false;
	
	private ProgressDialog progressDialog;
	
	public HttpGetOOMAsyncTask(Context context) {
		this.context = context;
	}
	
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
//		progressDialog = ProgressDialog.show(context, context.getResources().getString(R.string.wait_for_loading), context.getResources().getString(R.string.loading));
	}

	@Override
	protected String doInBackground(String... params) {

		String result = null;
		String url = params[0];
		Log.v("HttpGet", url);

		try {
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
			HttpClient client = new DefaultHttpClient(httpParams);
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()), 23 * 1024);
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		        sb.append(line);
		    }
		    result = sb.toString();
//            entity.writeTo(out);
//            out.close();
//            result = out.toString();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
//		progressDialog.cancel();
		this.cancel(true);
		context = null;
	}

}
