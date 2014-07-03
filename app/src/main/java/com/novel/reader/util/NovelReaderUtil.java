package com.novel.reader.util;

import java.io.IOException;

import android.content.Context;

public class NovelReaderUtil {

	public static boolean isDisplayDefaultBookCover(String url){
		if(url.equals("") || url == null || url.equals("null") || url.equals("http://www.bestory.com/pics/0.jpg"))
			return true;
		
		return false;
	}
	
	public static String translateTextIfCN(Context context,String string){
		
		String retrun_string = null;
		
		int local = Setting.getSettingInt(Setting.keyTextLanguage, context);
		if(local == Setting.TEXT_CHINA){
			try {
				retrun_string = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(string);
			} catch (IOException e) {
				retrun_string = string;
			}
		}else{
			retrun_string = string;
		}
		
		return retrun_string;
	}

}
