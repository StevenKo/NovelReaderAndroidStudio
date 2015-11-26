package com.novel.reader.api;

import com.crashlytics.android.Crashlytics;
import com.novel.db.SQLiteNovel;
import com.novel.reader.ArticleActivity;
import com.novel.reader.MainActivity;
import com.novel.reader.entity.Article;
import com.novel.reader.entity.Bookmark;
import com.novel.reader.entity.Category;
import com.novel.reader.entity.GameAPP;
import com.novel.reader.entity.Novel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class NovelAPI {

    final static String HOST = "http://api.novelking.cc";
    public static final String TAG = "NOVEL_API";
    public static final boolean DEBUG = true;
    final static String GAME_HOST = "http://apply.inapp.tw";
    public static ArrayList<GameAPP> apps;

    public static void sendClickInfo(Context context, int appid) {
//    	String device_id = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);

//    	TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
//    	String device_id = telephonyManager.getDeviceId();
//    	
//    	String ip = DeviceUtils.getIPAddress(true);
//    	
//    	HttpClient httpclient = new DefaultHttpClient();
//        HttpPost httppost = new HttpPost("http://www.inapp.tw/adlog/");
//
//        try {
//            // Add your data
//            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//            nameValuePairs.add(new BasicNameValuePair("ip", ip));
//            nameValuePairs.add(new BasicNameValuePair("appid", appid+""));
//            nameValuePairs.add(new BasicNameValuePair("imei", device_id));
//            nameValuePairs.add(new BasicNameValuePair("bigtype", "3"));
//            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
//
//            // Execute HTTP Post Request
//            HttpResponse response = httpclient.execute(httppost);
//            HttpEntity httpEntity = response.getEntity();
//            InputStream is = httpEntity.getContent();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(
//                    is, "UTF-8"));
//            StringBuilder sb = new StringBuilder();
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                sb.append(line + "\n");
//            }
//            is.close();
//            String json = sb.toString();
//            
//        } catch (ClientProtocolException e) {
//        } catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 

//    	String message = getMessageFromServer("GET", "", null,GAME_HOST + "/adlog" + "?ip="+ip+"&appid="+appid+"&imei="+device_id+"&bigtype=3");
//		return message;
    }

    public static ArrayList<GameAPP> getAppInfo(Context context) {
        if (apps != null)
            return apps;
        apps = new ArrayList<GameAPP>();
        int id = 0;
        int appid = 0;
        String title = "AndroMoney理財幫手";
        String description = "榮獲Google Play 首頁之效率排行工具之記帳理財APP。";
        String imageUrl = "";
        String appStoreUrl = "https://play.google.com/store/apps/details?id=com.kpmoney.android";
        GameAPP app = new GameAPP(id, appid, title, description, imageUrl, appStoreUrl);
        apps.add(app);
        return apps;

//    	String message = getMessageFromServer("GET", "", null,GAME_HOST+"/api/spread?index=1&count=15");
//    	if (message == null) {
//    		return null;
//        } else{
//        	try {
//	        	 JSONArray articlesArray = new JSONArray(message.toString());
//	             for (int i = 0; i < articlesArray.length(); i++) {
//	            
//		             int id = articlesArray.getJSONObject(i).getInt("id");
//		             int appid = articlesArray.getJSONObject(i).getInt("appid");
//		             String title = articlesArray.getJSONObject(i).getString("title");
//		             String description = articlesArray.getJSONObject(i).getString("description");
//		             String imageUrl = articlesArray.getJSONObject(i).getString("imageUrl");
//		             String appStoreUrl = articlesArray.getJSONObject(i).getString("appStoreUrl");
//		             GameAPP app = new GameAPP(id,appid,title,description,imageUrl,appStoreUrl);
//		             apps.add(app);
//	             }
//             
//	             return apps;
//             }catch(Exception e){
//            	 return null;
//             }
//        }
    }

    public static ArrayList<Bookmark> getAllRecentReadBookmarks(Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        ArrayList<Bookmark> bookmarks = db.getAllRecentReadBookmarks();
        return bookmarks;
    }

    public static ArrayList<Bookmark> getAllBookmarks(Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        ArrayList<Bookmark> bookmarks = db.getAllBookmarks();
        return bookmarks;
    }

    public static Bookmark getNovelBookmark(int novel_id, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        Bookmark bookmark = db.getNovelBookmark(novel_id);
        return bookmark;
    }

    public static void updateBookmark(Bookmark bookmark, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        db.updateBookmark(bookmark);
    }

    public static void deleteBookmark(Bookmark bookmark, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        db.deleteBookmark(bookmark);
    }

    public static void deleteBookmarks(ArrayList<Bookmark> bookmarks, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        db.deleteBookmarks(bookmarks);
    }

    public static Bookmark insertBookmark(Bookmark bookmark, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        int id = (int) db.insertBookmark(bookmark);
        bookmark.setId(id);
        return bookmark;
    }

    public static Bookmark createRecentBookmark(Bookmark bookmark, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        Bookmark lastNovelBookmark = db.getNovelBookmark(bookmark.getNovelId());
        if (lastNovelBookmark != null)
            db.deleteBookmark(lastNovelBookmark);
        int id = (int) db.insertBookmark(bookmark);
        bookmark.setId(id);
        return bookmark;
    }

    public static ArrayList<Novel> getCollectedNovels(Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        ArrayList<Novel> novels = db.getCollectedNovels();
        return novels;
    }

    public static Boolean isNovelCollected(Context context, int novel_id) {
        SQLiteNovel db = new SQLiteNovel(context);
        Boolean isNovelCollected = db.isNovelCollected(novel_id);
        return isNovelCollected;
    }

    public static Boolean isNovelDownloaded(Context context, int novel_id) {
        SQLiteNovel db = new SQLiteNovel(context);
        Boolean isNovelDownloaded = db.isNovelDownloaded(novel_id);
        return isNovelDownloaded;
    }

    public static ArrayList<Novel> getDownloadedNovels(Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        ArrayList<Novel> novels = db.getDownloadNovels();
        return novels;
    }

    public static boolean downloadArticles(int novelId, ArrayList<Article> articles, Context context) {
        for (int i = 0; i < articles.size(); i++)
            downloadArticle(novelId, articles.get(i), context);
        return true;
    }

    public static boolean downloadArticle(int novelId, Article article, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);

        if (!db.isNovelExists(novelId)) {
            downloadOrUpdateNovelInfo(article.getNovelId(), context, false, true);
        } else if (!db.isNovelDownloaded(novelId)) {
            Novel novel = getNovel(novelId, context);
            novel.setIsDownload(true);
            db.updateNovel(novel);
        }

        String message = getMessageFromServer("GET", "/api/v1/articles/" + article.getId() + ".json", null);
        if (message == null) {
            return false;
        } else {
            try {
                JSONObject nObject;
                nObject = new JSONObject(message.toString());
                String text = nObject.getString("text");
                article.setText(text);
                article.setIsDownloaded(true);
                nObject = null;
                text = null;

            } catch (JSONException e) {

                e.printStackTrace();
                return false;
            }
        }
        message = null;

        if (db.isArticleExists(article.getId()))
            db.updateArticle(article);
        else
            db.insertArticle(article);

        article.setText("");
        return true;
    }

    public static void removeNovelFromCollected(Novel novel, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        db.removeNovelFromCollected(novel);
    }

    public static void removeNovelFromDownload(Novel novel, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        ArrayList<Article> articles = db.getNovelArticles(novel.getId(), true);
        db.deleteArticles(articles);
        db.removeNovelFromDownload(novel);
    }

    public static void removeArticle(Article article, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        db.deleteArticle(article);
    }

    public static void removeArticles(ArrayList<Article> articles, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        db.deleteArticles(articles);
    }

    public static void updateNovelsInfo(ArrayList<Novel> novels, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        for (Novel novel : novels) {
            novel.setIsDownload(db.isNovelDownloaded(novel.getId()));
            novel.setIsCollected(db.isNovelCollected(novel.getId()));
            db.updateNovel(novel);
        }
    }

    public static boolean collecNovel(final Novel novel, final Context context) {
        novel.setIsCollected(true);
        final SQLiteNovel db = new SQLiteNovel(context);
        novel.setIsDownload(db.isNovelDownloaded(novel.getId()));
        if (db.isNovelExists(novel.getId()))
            db.updateNovel(novel);
        else
            db.insertNovel(novel);

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                downloadOrUpdateNovelInfo(novel.getId(), context, true, db.isNovelDownloaded(novel.getId()));
                return params;
            }
        }.execute();

        return true;
    }

    public static boolean downloadOrUpdateNovelInfo(int novelId, Context context, boolean isCollected, boolean isDownload) {

        Novel n = null;
        // ArrayList<Article> articles = new ArrayList<Article>();

        String message = getMessageFromServer("GET", "/api/v1/novels/" + novelId + "/detail_for_save.json", null);
        if (message == null) {
            return false;
        } else {
            try {
                JSONObject nObject;
                nObject = new JSONObject(message.toString());
                nObject = nObject.getJSONObject("novel");

                int novel_id = nObject.getInt("id");
                String articleNum = nObject.getString("article_num");
                String author = nObject.getString("author");
                boolean isSerializing = nObject.getBoolean("is_serializing");
                String lastUpdate = nObject.getString("last_update");
                String name = nObject.getString("name");
                String pic = nObject.getString("pic");
                String description = nObject.getString("description");
                int category_id = nObject.getInt("category_id");

                n = new Novel(novel_id, name, author, description, pic, category_id, articleNum, lastUpdate, isSerializing, isCollected, isDownload);

                // JSONArray articlesArray = nObject.getJSONArray("articles");
                // for (int i = 0; i < articlesArray.length(); i++) {
                //
                // int article_id = articlesArray.getJSONObject(i).getInt("id");
                // String subject = articlesArray.getJSONObject(i).getString("subject");
                // String title = articlesArray.getJSONObject(i).getString("title");
                //
                // Article a = new Article(article_id, novelId, "", title, subject, false);
                // articles.add(a);
                // }

            } catch (JSONException e) {

                e.printStackTrace();
                return false;
            }
        }

        SQLiteNovel db = new SQLiteNovel(context);
        if (db.isNovelExists(novelId))
            db.updateNovel(n);
        else
            db.insertNovel(n);


        // for (int i = 0; i < articles.size(); i++) {
        // if (db.isArticleExists(articles.get(i).getId()))
        // db.updateArticle(articles.get(i));
        // else
        // db.insertArticle(articles.get(i));
        // }

        return true;
    }

    public static ArrayList<Novel> getCollectNovelsInfoFromServer(ArrayList<Novel> collectNovels) {
        String idLst = "";
        for (int i = 0; i < collectNovels.size(); i++)
            idLst = collectNovels.get(i).getId() + "," + idLst;
        if (idLst.length() > 0)
            idLst = idLst.substring(0, idLst.length() - 1);

        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/collect_novels_info.json?novels_id=" + idLst, null);
        if (message == null) {
            return null;
        } else {
            novels = parseNovel(message, novels);

            if(novels != null) {
                for (int i = 0; i < novels.size(); i++) {
                    novels.get(i).setLastViewDate(collectNovels.get(i).getLastViewDate());
                }
            }
            return novels;
        }
    }

    public static ArrayList<Novel> searchNovels(String keyword) {
        String query;
        try {
            query = URLEncoder.encode(keyword, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/search.json?search=" + query, null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }
    }

    public static Article getArticle(Article article, Context context) {

        SQLiteNovel db = new SQLiteNovel(context);
        if (db.isArticleExists(article.getId())) {
            Article articleFromDB = db.getArticle(article.getId());
            if (articleFromDB.getText().length() > 0)
                articleFromDB.setText(articleFromDB.getText());
            return articleFromDB;
        }


        String message = getMessageFromServer("GET", "/api/v1/articles/" + article.getId() + ".json", null);
        if (message == null) {
            return null;
        } else {
            try {
                JSONObject nObject;
                nObject = new JSONObject(message.toString());
                String text = nObject.getString("text");
                String title = nObject.getString("title");
                article.setText(text);
                article.setTitle(title);

            } catch (JSONException e) {

                e.printStackTrace();
                return null;
            }
        }
        return article;
    }

    public static Article getPreviousArticle(Article orginArticle, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        ArrayList<Article> articles = db.getNovelArticles(orginArticle.getNovelId(), true);

        for (int i = 1; i < articles.size(); i++) {
            if (articles.get(i).getId() == orginArticle.getId()) {
                return getArticle(articles.get(i - 1), context);
            }
        }

        String message = getMessageFromServer("GET", "/api/v1/articles/previous_article_by_num.json?novel_id=" + orginArticle.getNovelId() + "&article_id="
                + orginArticle.getId() + "&num=" + orginArticle.getNum(), null);
        if (message == null) {
            return null;
        } else {
            try {

                JSONObject nObject;
                nObject = new JSONObject(message.toString());
                String text = nObject.getString("text");
                int id = nObject.getInt("id");
                int novelId = nObject.getInt("novel_id");
                String title = nObject.getString("title");
                int num = nObject.getInt("num");
                return new Article(id, novelId, text, title, "", false, num);

            } catch (JSONException e) {

                e.printStackTrace();
                return null;
            }
        }
    }

    public static Article getNextArticle(Article orginArticle, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        ArrayList<Article> articles = db.getNovelArticles(orginArticle.getNovelId(), true);

        for (int i = 0; i < articles.size() - 1; i++) {
            if (articles.get(i).getId() == orginArticle.getId()) {
                return getArticle(articles.get(i + 1), context);
            }
        }

        String message = getMessageFromServer("GET", "/api/v1/articles/next_article_by_num.json?novel_id=" + orginArticle.getNovelId() + "&article_id="
                + orginArticle.getId() + "&num=" + orginArticle.getNum(), null);
        if (message == null) {
            return null;
        } else {
            try {

                JSONObject nObject;
                nObject = new JSONObject(message.toString());
                String text = nObject.getString("text");
                int id = nObject.getInt("id");
                int novelId = nObject.getInt("novel_id");
                String title = nObject.getString("title");
                int num = nObject.getInt("num");
                return new Article(id, novelId, text, title, "", false, num);

            } catch (JSONException e) {

                e.printStackTrace();
                return null;
            }
        }
    }

    public static ArrayList<Article> getDownloadedNovelArticles(int novelId, boolean isOrderUp, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        ArrayList<Article> as = db.getNovelArticles(novelId, isOrderUp);
        return as;
    }

    public static ArrayList<Article> getNovelArticles(int novelId, boolean isOrderUp, Context context) {
        ArrayList<Article> articles = new ArrayList<Article>();
        String message = getMessageFromServer("GET", "/api/v1/articles/articles_by_num.json?novel_id=" + novelId + "&order=" + isOrderUp, null);
        if (message == null) {
            return null;
        } else {
            try {
                JSONArray novelsArray;
                novelsArray = new JSONArray(message.toString());
                for (int i = 0; i < novelsArray.length(); i++) {

                    int id = novelsArray.getJSONObject(i).getInt("id");
                    String subject = novelsArray.getJSONObject(i).getString("subject");
                    String title = novelsArray.getJSONObject(i).getString("title");
                    int num = novelsArray.getJSONObject(i).getInt("num");

                    Article a = new Article(id, novelId, "", title, subject, false, num);
                    articles.add(a);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        SQLiteNovel db = new SQLiteNovel(context);
        articles = db.getArticleDownloadInfo(articles);

        return articles;
    }

    public static ArrayList<Novel> getThisMonthHotNovels(int myPage) {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/this_month_hot.json" + "?page=" + myPage, null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }

    }

    public static ArrayList<Novel> getThisWeekHotNovels(int myPage) {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/this_week_hot.json" + "?page=" + myPage, null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }
    }

    public static ArrayList<Novel> getHotNovels(int myPage) {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/hot.json" + "?page=" + myPage, null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }
    }

    // 經典小說
    public static ArrayList<Novel> getClassicNovels() {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/classic.json", null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }
    }

    // 經典武俠
    public static ArrayList<Novel> getClassicActionNovels() {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/classic_action.json", null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }
    }

    public static ArrayList<Novel> getCategoryRecommendNovels(int category_id, int page) {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/category_recommend.json?category_id=" + category_id + "&page=" + page, null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }
    }

    public static ArrayList<Novel> getCategoryThisWeekHotNovels(int category_id, int page) {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/category_this_week_hot.json?category_id=" + category_id + "&page=" + page, null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }
    }

    public static ArrayList<Novel> getCategoryHotNovels(int category_id, int page) {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/category_hot.json?category_id=" + category_id + "&page=" + page, null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }
    }

    public static Novel getNovel(int novelId, Context context) {

        SQLiteNovel db = new SQLiteNovel(context);
        if (db.isNovelExists(novelId)) {
            return db.getNovel(novelId);
        }

        Novel n = null;
        String message = getMessageFromServer("GET", "/api/v1/novels/" + novelId + ".json", null);
        if (message == null) {
            return null;
        } else {
            try {
                JSONObject nObject;
                nObject = new JSONObject(message.toString());
                int id = nObject.getInt("id");
                String articleNum = nObject.getString("article_num");
                String author = nObject.getString("author");
                boolean isSerializing = nObject.getBoolean("is_serializing");
                String lastUpdate = nObject.getString("last_update");
                String name = nObject.getString("name");
                String pic = nObject.getString("pic");
                String description = nObject.getString("description");
                int category_id = nObject.getInt("category_id");

                n = new Novel(id, name, author, description, pic, category_id, articleNum, lastUpdate, isSerializing, false, false);

            } catch (JSONException e) {

                e.printStackTrace();
                return null;
            }
        }
        return n;
    }

    public static ArrayList<Novel> getCategoryNovels(int category_id, int page) {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels.json?category_id=" + category_id + "&page=" + page, null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }
    }

    public static ArrayList<Novel> getCategoryLatestNovels(int category_id, int page) {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/category_latest_update.json?category_id=" + category_id + "&page=" + page, null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }
    }

    public static ArrayList<Novel> getLatestUpdateNovels(int page) {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/all_novel_update.json?&page=" + page, null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }
    }

    public static ArrayList<Novel> getNewUploadedNovels(int page) {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/new_uploaded_novels.json?&page=" + page, null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }
    }

    public static ArrayList<Novel> getCategoryFinish(int categoryId, int page) {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/category_finish.json?category_id=" + categoryId + "&page=" + page, null);
        if (message == null) {
            return null;
        } else {
            return parseNovel(message, novels);
        }
    }

    public static ArrayList<Category> getCategories() {
        return Category.getCategories();
    }

    public static String getMessageFromServer(String requestMethod, String apiPath, JSONObject json) {
        URL url;
        try {
            url = new URL(HOST + apiPath);
            if (DEBUG)
                Log.d(TAG, "URL: " + url);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);

            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            if (requestMethod.equalsIgnoreCase("POST"))
                connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.connect();

            if (requestMethod.equalsIgnoreCase("POST")) {
                OutputStream outputStream;

                outputStream = connection.getOutputStream();
                if (DEBUG)
                    Log.d("post message", json.toString());

                outputStream.write(json.toString().getBytes());
                outputStream.flush();
                outputStream.close();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder lines = new StringBuilder();
            ;
            String tempStr;

            while ((tempStr = reader.readLine()) != null) {
                lines = lines.append(tempStr);
            }
            if (DEBUG)
                Log.d(TAG, lines.toString());

            reader.close();
            connection.disconnect();

            return lines.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getMessageFromServer(String requestMethod, String apiPath, JSONObject json, String apiUrl) {
        URL url;
        try {
            if (apiUrl != null)
                url = new URL(apiUrl);
            else
                url = new URL(HOST + apiPath);

            if (DEBUG)
                Log.d(TAG, "URL: " + url);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);

            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            if (requestMethod.equalsIgnoreCase("POST"))
                connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.connect();

            if (requestMethod.equalsIgnoreCase("POST")) {
                OutputStream outputStream;

                outputStream = connection.getOutputStream();
                if (DEBUG)
                    Log.d("post message", json.toString());

                outputStream.write(json.toString().getBytes());
                outputStream.flush();
                outputStream.close();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder lines = new StringBuilder();
            ;
            String tempStr;

            while ((tempStr = reader.readLine()) != null) {
                lines = lines.append(tempStr);
            }
            if (DEBUG)
                Log.d("MOVIE_API", lines.toString());

            reader.close();
            connection.disconnect();

            return lines.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ArrayList<Novel> parseNovel(String message, ArrayList<Novel> novels) {
        try {
            JSONArray novelsArray;
            novelsArray = new JSONArray(message.toString());
            for (int i = 0; i < novelsArray.length(); i++) {

                int id = novelsArray.getJSONObject(i).getInt("id");
                if (novelsArray.getJSONObject(i).isNull("article_num"))
                    continue;
                String articleNum = novelsArray.getJSONObject(i).getString("article_num");
                String author = novelsArray.getJSONObject(i).getString("author");
                boolean isSerializing = novelsArray.getJSONObject(i).getBoolean("is_serializing");
                String lastUpdate = novelsArray.getJSONObject(i).getString("last_update");
                String name = novelsArray.getJSONObject(i).getString("name");
                String pic = novelsArray.getJSONObject(i).getString("pic");

                Novel novel = new Novel(id, name, author, "", pic, 0, articleNum, lastUpdate, isSerializing, false, false);
                novels.add(novel);
            }

        } catch (JSONException e) {
            Crashlytics.log(Log.ERROR, "NovelParseError", message);
            e.printStackTrace();
            return null;
        }
        return novels;
    }

    public static boolean sendUserEmail(String email) {
        String POST_PARAMS = "email=" + email;
        URL obj = null;
        HttpURLConnection con = null;
        try {
            obj = new URL("http://139.162.21.39" + "/api/v1/users.json");
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");

            // For POST only - BEGIN
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(POST_PARAMS.getBytes());
            os.flush();
            os.close();
            // For POST only - END

            int responseCode = con.getResponseCode();
            Log.i(TAG, "POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                Log.i(TAG, response.toString());
                return true;
            } else {
                Log.i(TAG, "POST request did not work.");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean sendRegistrationId(String regid, String deviceId, String country, int versionCode, String platform) {
        return false;
//    	try{
//			DefaultHttpClient httpClient = new DefaultHttpClient();
//			String url = HOST + "/api/v1/users.json?regid="+regid+"&device_id="+deviceId+"&platform="+platform+"&version="+versionCode+"&country="+country;
//			if(DEBUG)
//				Log.d(TAG, "URL : " + url);
//
//			HttpPost httpPost = new HttpPost(url);
//			HttpResponse response = httpClient.execute(httpPost);
//
//			StatusLine statusLine =  response.getStatusLine();
//			if (statusLine.getStatusCode() == 200){
//				return true;
//			}else{
//				return false;
//			}
//		}
//	    catch (Exception e) {
//			return false;
//		}
    }

    public static boolean sendNovel(int novel, String deviceId) {
        return false;
//    	try{
//			DefaultHttpClient httpClient = new DefaultHttpClient();
//			String url = HOST + "/api/v1/users/update_novel.json?novel="+novel+"&device_id="+deviceId;						
//			if(DEBUG)
//				Log.d(TAG, "URL : " + url);
//
//			HttpPut httpPut = new HttpPut(url);
//			HttpResponse response = httpClient.execute(httpPut);
//
//			StatusLine statusLine =  response.getStatusLine();
//			if (statusLine.getStatusCode() == 200){
//				return true;
//			}else{
//				return false;
//			}
//		} 
//	    catch (Exception e) {
//			return false;
//		} 

    }

    public static boolean sendDownloadedNovels(String novels, String deviceId) {
        return false;
//    	try{
//			DefaultHttpClient httpClient = new DefaultHttpClient();
//			String url = HOST + "/api/v1/users/update_downloaded_novels.json?novels="+novels+"&device_id="+deviceId;						
//			if(DEBUG)
//				Log.d(TAG, "URL : " + url);
//
//			HttpPut httpPut = new HttpPut(url);
//			HttpResponse response = httpClient.execute(httpPut);
//
//			StatusLine statusLine =  response.getStatusLine();
//			if (statusLine.getStatusCode() == 200){
//				return true;
//			}else{
//				return false;
//			}
//		} 
//	    catch (Exception e) {
//			return false;
//		} 
    }

    public static boolean sendCollectedNovels(String novels, String deviceId) {
        return false;
//    	try{
//			DefaultHttpClient httpClient = new DefaultHttpClient();
//			String url = HOST + "/api/v1/users/update_collected_novels.json?novels="+novels+"&device_id="+deviceId;						
//			if(DEBUG)
//				Log.d(TAG, "URL : " + url);
//
//			HttpPut httpPut = new HttpPut(url);
//			HttpResponse response = httpClient.execute(httpPut);
//
//			StatusLine statusLine =  response.getStatusLine();
//			if (statusLine.getStatusCode() == 200){
//				return true;
//			}else{
//				return false;
//			}
//		} 
//	    catch (Exception e) {
//			return false;
//		} 
    }

    public static Bookmark findBookMarkByArticle(Article article, Context context) {
        SQLiteNovel db = new SQLiteNovel(context);
        return db.findBookMarkByArticle(article);
    }


    public static ArrayList<Category> getRecommendCategoryWithNovels() {
        String message = getMessageFromServer("GET", "/api/v1/recommend_categories.json", null);
        if (message == null) {
            return new ArrayList<Category>();
        } else {
            return parseRecommendCategory(message);
        }
    }

    private static ArrayList<Category> parseRecommendCategory(String message) {
        ArrayList<Category> categories = new ArrayList<Category>();
        try {
            JSONArray categoriesArray;
            categoriesArray = new JSONArray(message.toString());
            for (int i = 0; i < categoriesArray.length(); i++) {

                int id = categoriesArray.getJSONObject(i).getInt("id");
                String categoryName = categoriesArray.getJSONObject(i).getString("name");
                ArrayList<Novel> novels = new ArrayList<Novel>();
                JSONArray novelsArray = categoriesArray.getJSONObject(i).getJSONArray("novels");
                for (int j = 0; j < novelsArray.length(); j++) {

                    int novelId = novelsArray.getJSONObject(j).getInt("id");
                    String articleNum = novelsArray.getJSONObject(j).getString("article_num");
                    String author = novelsArray.getJSONObject(j).getString("author");
                    boolean isSerializing = novelsArray.getJSONObject(j).getBoolean("is_serializing");
                    String lastUpdate = novelsArray.getJSONObject(j).getString("last_update");
                    String name = novelsArray.getJSONObject(j).getString("name");
                    String pic = novelsArray.getJSONObject(j).getString("pic");
                    Novel novel = new Novel(novelId, name, author, "", pic, 0, articleNum, lastUpdate, isSerializing, false, false);
                    novels.add(novel);

                }
                Category category = new Category(id, categoryName);
                category.novels = novels;
                categories.add(category);

            }

        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<Category>();
        }
        return categories;
    }

    public static void keepNovelLastViewDateIfInDB(int novelId, ArticleActivity articleActivity) {
        SQLiteNovel db = new SQLiteNovel(articleActivity);
        if (db.isNovelExists(novelId)) {
            db.updateNovelLastViewDate(novelId);
        }
    }


    public static ArrayList<Novel> getRecommendCategoryNovels(int recommendCateoryId) {
        ArrayList<Novel> novels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", "/api/v1/novels/recommend_category_novels.json?recommend_category_id=" + recommendCateoryId, null);
        if (message == null) {
            return novels;
        } else {
            return parseNovel(message, novels);
        }
    }

    public static void getNewestVersionAndLink(MainActivity.UpdateInfo info) {

        String message = getMessageFromServer("GET", "/api/version_check.json", null);
        if (message == null) {
        } else {
            try {
                JSONObject nObject;
                nObject = new JSONObject(message.toString());
                info.updateLink = nObject.getString("update_link");
                info.newest_version = nObject.getInt("version");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public static boolean restoreFromUserBackup(String email,Context context) {
        ArrayList<Novel> collectNovels = new ArrayList<Novel>();
        ArrayList<Novel> downloadNovels = new ArrayList<Novel>();
        String message = getMessageFromServer("GET", null, null,"http://139.162.21.39/api/v1/users/get_novels?email=chunyuko85@gmail.com");
        if (message == null) {
            return false;
        } else {
            try {
                JSONObject nObject;
                nObject = new JSONObject(message.toString());
                JSONArray collected_novels = nObject.getJSONArray("collected_novels");
                JSONArray downloaded_novels = nObject.getJSONArray("download_novels");

                for (int i = 0; i < collected_novels.length(); i++) {

                    int id = collected_novels.getJSONObject(i).getInt("id");
                    if (collected_novels.getJSONObject(i).isNull("article_num"))
                        continue;
                    String articleNum = collected_novels.getJSONObject(i).getString("article_num");
                    String author = collected_novels.getJSONObject(i).getString("author");
                    boolean isSerializing = collected_novels.getJSONObject(i).getBoolean("is_serializing");
                    String lastUpdate = collected_novels.getJSONObject(i).getString("last_update");
                    String name = collected_novels.getJSONObject(i).getString("name");
                    String pic = collected_novels.getJSONObject(i).getString("pic");

                    Novel novel = new Novel(id, name, author, "", pic, 0, articleNum, lastUpdate, isSerializing, false, false);
                    collectNovels.add(novel);
                }

                for (int i = 0; i < downloaded_novels.length(); i++) {

                    int id = downloaded_novels.getJSONObject(i).getInt("id");
                    if (downloaded_novels.getJSONObject(i).isNull("article_num"))
                        continue;
                    String articleNum = downloaded_novels.getJSONObject(i).getString("article_num");
                    String author = downloaded_novels.getJSONObject(i).getString("author");
                    boolean isSerializing = downloaded_novels.getJSONObject(i).getBoolean("is_serializing");
                    String lastUpdate = downloaded_novels.getJSONObject(i).getString("last_update");
                    String name = downloaded_novels.getJSONObject(i).getString("name");
                    String pic = downloaded_novels.getJSONObject(i).getString("pic");

                    Novel novel = new Novel(id, name, author, "", pic, 0, articleNum, lastUpdate, isSerializing, false, false);
                    downloadNovels.add(novel);
                }

                HashMap<Integer, Novel> novelHash = new HashMap<Integer, Novel>();

                for (int i = 0; i < collectNovels.size(); i++) {
                    Novel novel = collectNovels.get(i);
                    // 先確認key是否存在
                    if (novelHash.containsKey(novel.getId())) {
                        novelHash.get(novel.getId()).setIsCollected(true);
                    } else {
                        novel.setIsCollected(true);
                        novelHash.put(novel.getId(), novel);
                    }
                }

                for (int i = 0; i < downloadNovels.size(); i++) {
                    Novel novel = downloadNovels.get(i);
                    // 先確認key是否存在
                    if (novelHash.containsKey(novel.getId())) {
                        novelHash.get(novel.getId()).setIsDownload(true);
                    } else {
                        novel.setIsDownload(true);
                        novelHash.put(novel.getId(), novel);
                    }
                }
                SQLiteNovel db = new SQLiteNovel(context);
                db.deleteAllNovels();

                for (Integer key : novelHash.keySet()) {
                    Novel novel = novelHash.get(key);
                    db.insertNovel(novel);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i(TAG, message.toString());
            return true;
        }
    }

    public static boolean BackupToServer(String email, Context context) {
        ArrayList<Novel> collectNovels = getCollectedNovels(context);
        ArrayList<Novel> downloadNovels = getDownloadedNovels(context);

        String collect = "";
        String download = "";
        for(Novel n: collectNovels){
            collect += n.getId() + ",";
        }
        for(Novel n: downloadNovels){
            download += n.getId() + ",";
        }

        String POST_PARAMS = "email=" + email + "&collect_novels=" + collect + "&download_novels=" + download ;
        URL obj = null;
        HttpURLConnection con = null;
        try {
            obj = new URL("http://139.162.21.39" + "/api/v1/users/update_novel");
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("PUT");

            // For POST only - BEGIN
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(POST_PARAMS.getBytes());
            os.flush();
            os.close();
            // For POST only - END

            int responseCode = con.getResponseCode();
            Log.i(TAG, "PUT Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                Log.i(TAG, response.toString());
                return true;
            } else {
                Log.i(TAG, "POST request did not work.");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

    }
}
