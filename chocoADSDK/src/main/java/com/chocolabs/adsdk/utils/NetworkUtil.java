package com.chocolabs.adsdk.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {
	public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;
     
     
    public static boolean getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
 
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        if (null != activeNetwork) {
//            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
//                return TYPE_WIFI;
//             
//            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
//                return TYPE_MOBILE;
//        } 
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        return isConnected;
    }
     
    public static String getConnectionStatus(Context context) {
        int conn = getConnectionMethod(context);
        String status = null;
        if (conn == NetworkUtil.TYPE_WIFI) {
            status = "Wifi";
        } else if (conn == NetworkUtil.TYPE_MOBILE) {
            status = "3g";
        } else if (conn == NetworkUtil.TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }
    
    public static int getConnectionMethod(Context context) {
    	ConnectivityManager cm = (ConnectivityManager) context
                 .getSystemService(Context.CONNECTIVITY_SERVICE);
  
    	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    	if (null != activeNetwork) {
    		if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
    			return TYPE_WIFI;
              
    		if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
    			return TYPE_MOBILE;
         }
         return TYPE_NOT_CONNECTED;
    }
}
