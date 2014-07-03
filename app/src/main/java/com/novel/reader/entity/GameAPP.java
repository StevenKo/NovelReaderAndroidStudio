package com.novel.reader.entity;

import java.util.ArrayList;

public class GameAPP {
	public int     id;
	public int     appid;
	public String  title;
	public String  description;
	public String  imageUrl;
    public String  appStoreUrl;
    public int showedTime = 0;
   
    public GameAPP(int id, int appid, String title, String description, String imageUrl, String appStoreUrl) {
        this.id = id;
        this.appid = appid;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.appStoreUrl = appStoreUrl;
    }
    
    public static ArrayList<GameAPP> lessShowedTimeAPP(ArrayList<GameAPP> apps){
    	
    	int time = 100000;
    	for(GameAPP item : apps ){
		   if(item.showedTime < time)
			   time = item.showedTime;
		}
    	ArrayList<GameAPP> lessShowedTimeApps = new ArrayList<GameAPP>();
    	for(GameAPP item : apps ){
 		   if(item.showedTime == time)
 			  lessShowedTimeApps.add(item);
 		}
    	
    	
		return lessShowedTimeApps;
    }
}
