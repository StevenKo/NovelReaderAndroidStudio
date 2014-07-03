package com.taiwan.imageload;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class UrlImageLoader {
    /** 
     * 通过图片url返回图片Bitmap 
     * @param url 
     * @return 
     */  
    public static Bitmap returnBitMap(String path) {  
        URL url = null;  
        Bitmap bitmap = null;
        
        try {  
            url = new URL(path);  
        } catch (MalformedURLException e) {  
            e.printStackTrace(); 
            url = null;
        }  
        try {  
        	if(url != null) {
	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();//利用HttpURLConnection对象,我们可以从网络中获取网页数据.  
	            conn.setDoInput(true);  
	            conn.connect();  
	            InputStream is = conn.getInputStream(); //得到网络返回的输入流  
	            bitmap = BitmapFactory.decodeStream(is);  
	            is.close();  
        	}
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return bitmap;  
    } 

}
