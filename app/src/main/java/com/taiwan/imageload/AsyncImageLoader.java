package com.taiwan.imageload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

public class AsyncImageLoader {
	/** 
     * 内存图片软引用缓冲 
     */
	private final String IMAGE_PATH= "/data/data/com.jumplife.jomemain/Image";
    private HashMap<String, SoftReference<Bitmap>> imageCache = null;  
      
    public AsyncImageLoader() {  
        imageCache = new HashMap<String, SoftReference<Bitmap>>();  
    }  
      
    public Bitmap loadBitmap(final ImageView imageView, final String imageURL, final ImageCallBack imageCallBack) {  
        /**
         * 在内存缓存(reference)中
         * 則取得非Null Bitmap 
         * 否則查找記憶卡中是否有暫存 
         */
    	String[] array = imageURL.split("/");
        String bitmapName = array[3];
        
        if(imageCache.containsKey(bitmapName)) {  
            SoftReference<Bitmap> reference = imageCache.get(bitmapName);  
            Bitmap bitmap = reference.get();  
            if(bitmap != null) {  
                return bitmap;  
            }  
        }  
        else {  
            /** 
             * 加上一個缓存的查找 
             */  
            File cacheDir = new File(IMAGE_PATH);  
            File[] cacheFiles = cacheDir.listFiles();  
            int i = 0; 
            if(cacheFiles != null) {
	            for(; i < cacheFiles.length; i++) {  
	                if(bitmapName.equals(cacheFiles[i].getName())) {  
	                    break;  
	                }  
	            }  
	              
	            if(i < cacheFiles.length) {  
	                return BitmapFactory.decodeFile(IMAGE_PATH + bitmapName);  
	            } 
            }
        }  
          
        final Handler handler = new Handler() {  
            @Override  
            public void handleMessage(Message msg) {  
                // TODO Auto-generated method stub  
                imageCallBack.imageLoad(imageView, (Bitmap)msg.obj);  
            }  
        };  
          
        /*
         * 如果不在内存缓存中
         * 則開新thread 從網路上下載
         */
        new Thread() {  
            @Override  
            public void run() {  
                // TODO Auto-generated method stub  
                Bitmap bitmap = UrlImageLoader.returnBitMap(imageURL);
                String[] array = imageURL.split("/");
                String bitmapName = array[3];
                imageCache.put(bitmapName, new SoftReference<Bitmap>(bitmap));  
                Message msg = handler.obtainMessage(0, bitmap);  
                handler.sendMessage(msg);  
                  
                /*
                 * 在記憶卡裡加入這張圖片 
                 * 檢查路徑或圖片是否已經存在
                 * 不存在則加入成jpg格式
                 */
                File dir = new File(IMAGE_PATH);  
                if(!dir.exists()) {  
                    dir.mkdirs();  
                }  
                 
                File bitmapFile = new File(IMAGE_PATH + bitmapName);  
                if(!bitmapFile.exists()) {  
                    try  
                    {  
                        bitmapFile.createNewFile();  
                    }  
                    catch (IOException e)  
                    {  
                        // TODO Auto-generated catch block  
                        e.printStackTrace();  
                    }  
                }  
                FileOutputStream fos = null;  
                try  
                {  
                    fos = new FileOutputStream(bitmapFile);  
                    if(fos != null && bitmap != null)
                    	bitmap.compress(Bitmap.CompressFormat.PNG,   
                            100, fos);  
                    fos.close();  
                }  
                catch (FileNotFoundException e)  
                {  
                    // TODO Auto-generated catch block  
                    e.printStackTrace(); 
                }  
                catch (IOException e)  
                {  
                    // TODO Auto-generated catch block  
                    e.printStackTrace();
                } 
            }  
        }.start();  
          
        return null;  
    }  
      
    public interface ImageCallBack {  
        public void imageLoad(ImageView imageView, Bitmap bitmap);  
    }

}
