package com.novel.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.novel.reader.entity.Article;
import com.novel.reader.entity.Bookmark;
import com.novel.reader.entity.Novel;

public class SQLiteNovel extends SQLiteOpenHelper {

    public static final String DB_NAME            = "kosnovel.sqlite";                                   // 資料庫名稱
    private static final int   DATABASE_VERSION   = 5;                                                   // 資料庫版本
    private static SQLiteDatabase     db;
    private final Context      ctx;
    public static final File   DATABASE_FILE_PATH = android.os.Environment.getExternalStorageDirectory();

    // Define database schema
    public interface NovelSchema {
        String TABLE_NAME     = "novels";
        String ID             = "id";
        String NAME           = "name";
        String AUTHOR         = "author";
        String DESCRIPTION    = "description";
        String PIC            = "pic";
        String CATEGORY_ID    = "category_id";
        String ARTICLE_NUM    = "article_num";
        String LAST_UPDATE    = "last_update";
        String IS_SERIALIZING = "is_serializing";
        String IS_COLLECTED   = "is_collected";
        String IS_DOWNLOAD    = "is_downloaded";
    }

    public interface ArtcileSchema {
        String TABLE_NAME    = "articles";
        String ID            = "id";
        String NOVEL_ID      = "novel_id";
        String TEXT          = "text";
        String TITLE         = "title";
        String SUBJECT       = "subject";
        String IS_DOWNLOADED = "is_downloaded";
        String NUM           = "num";
    }

    public interface BookmarkSchema {
        String TABLE_NAME     = "bookmarks";
        String ID             = "id";
        String NOVEL_ID       = "novel_id";
        String ARTICLE_ID     = "article_id";
        String READ_RATE      = "read_rate";
        String NOVEL_NAME     = "novel_name";
        String ARTICLE_TITLE  = "article_title";
        String NOVEL_PIC      = "novel_pic";
        String IS_RECENT_READ = "is_recent_read";
    }

    public SQLiteNovel(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        ctx = context;

        // if (db == null)
        // db = this.getWritableDatabase();

        if (db == null || !db.isOpen()) {
            if (DATABASE_FILE_PATH != null) {
                try {
                    db = SQLiteDatabase.openDatabase(DATABASE_FILE_PATH + File.separator + "kosnovel/" + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
                    if (db.getVersion() < DATABASE_VERSION) {
                        onUpgrade(db, db.getVersion(), DATABASE_VERSION);
                        db = SQLiteDatabase.openDatabase(DATABASE_FILE_PATH + File.separator + "kosnovel/" + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
                    }
                } catch (Exception ex) {
                    try {
                        File cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "kosnovel");
                        if (!cacheDir.exists())
                            cacheDir.mkdirs();
                        db = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH + File.separator + "kosnovel/" + DB_NAME, null);
                        db.setVersion(DATABASE_VERSION);
                        onCreate(db);
                    } catch (Exception e) {
                        db = this.getWritableDatabase();
                        if (!isTableExists(db, NovelSchema.TABLE_NAME))
                            onCreate(db);
                    }
                }
            } else {
                db = this.getWritableDatabase();
                if (!isTableExists(db, NovelSchema.TABLE_NAME))
                    onCreate(db);
            }

        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (DATABASE_FILE_PATH != null) {
                if (oldVersion == 1) {
                    alterArticleTableAddNum(db);
                    moveDB();
                } else if (oldVersion < DATABASE_VERSION) {
                    alterArticleTableAddNum(db);
                }
            } else {
                alterArticleTableAddNum(db);
            }
        } catch (Exception e) {
            resetDB();
        }

    }

    private void alterArticleTableAddNum(SQLiteDatabase db) {
        if (!existsColumnInTable(db, ArtcileSchema.TABLE_NAME, ArtcileSchema.NUM)) {
            String upgradeQuery = "ALTER TABLE " + ArtcileSchema.TABLE_NAME + " ADD COLUMN " + ArtcileSchema.NUM + " INTEGER default 0";
            db.execSQL(upgradeQuery);
        }
    }

    private boolean existsColumnInTable(SQLiteDatabase inDatabase, String inTable, String columnToCheck) {
        try {
            // query 1 row
            Cursor mCursor = inDatabase.rawQuery("SELECT * FROM " + inTable + " LIMIT 0", null);

            // getColumnIndex gives us the index (0 to ...) of the column - otherwise we get a -1
            if (mCursor.getColumnIndex(columnToCheck) != -1)
            {
            	mCursor.close();
                return true;
            }else{
            	mCursor.close();
            	return false;
            }
                

        } catch (Exception Exp) {
            // something went wrong. Missing the database? The table?
            // Log.d("... - existsColumnInTable","When checking whether a column exists in the table, an error occurred: " + Exp.getMessage());
            return false;
        }
    }

    public boolean isTableExists(SQLiteDatabase mDatabase, String tableName) {

        Cursor cursor = mDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        cursor.close();
        return false;
    }

    public boolean resetDB() {
        try {
            ctx.deleteDatabase(SQLiteNovel.DB_NAME);
            db = this.getWritableDatabase();
            File currentDB = ctx.getDatabasePath(SQLiteNovel.DB_NAME);
            File cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "kosnovel");
            if (!cacheDir.exists())
                cacheDir.mkdirs();
            File sdcardDB = new File(cacheDir, DB_NAME);
            if (sdcardDB.exists())
                sdcardDB.delete();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void moveDB() {
        try {
            File currentDB = ctx.getDatabasePath(SQLiteNovel.DB_NAME);
            File cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "kosnovel");
            if (!cacheDir.exists())
                cacheDir.mkdirs();
            File sdcardDB = new File(cacheDir, DB_NAME);
            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(sdcardDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                // currentDB;
                ctx.deleteDatabase(SQLiteNovel.DB_NAME);
                db = this.getWritableDatabase();
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NovelSchema.TABLE_NAME + " (" + NovelSchema.ID + " INTEGER PRIMARY KEY" + "," + NovelSchema.NAME
                + " TEXT NOT NULL" + "," + NovelSchema.AUTHOR + " TEXT NOT NULL" + "," + NovelSchema.DESCRIPTION + " TEXT NOT NULL" + "," + NovelSchema.PIC
                + " TEXT NOT NULL" + "," + NovelSchema.CATEGORY_ID + " INTEGER NOT NULL" + "," + NovelSchema.ARTICLE_NUM + " TEXT NOT NULL" + ","
                + NovelSchema.LAST_UPDATE + " TEXT NOT NULL" + "," + NovelSchema.IS_SERIALIZING + " INTEGER NOT NULL" + "," + NovelSchema.IS_COLLECTED
                + " INTEGER NOT NULL" + "," + NovelSchema.IS_DOWNLOAD + " INTEGER NOT NULL" + ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + ArtcileSchema.TABLE_NAME + " (" + ArtcileSchema.ID + " INTEGER PRIMARY KEY" + "," + ArtcileSchema.NOVEL_ID
                + " INTEGER NOT NULL" + "," + ArtcileSchema.TEXT + " TEXT NOT NULL" + "," + ArtcileSchema.TITLE + " TEXT NOT NULL" + ","
                + ArtcileSchema.SUBJECT + " TEXT NOT NULL" + "," + ArtcileSchema.IS_DOWNLOADED + " INTEGER NOT NULL" + "," + ArtcileSchema.NUM
                + " INTEGER default 0" + "," + "FOREIGN KEY(" + ArtcileSchema.NOVEL_ID + ") REFERENCES " + NovelSchema.TABLE_NAME + "(" + NovelSchema.ID
                + ") ON UPDATE CASCADE" + ");");
        // db.execSQL("CREATE TABLE IF NOT EXISTS " + ArtcileSchema.TABLE_NAME + " (" + ArtcileSchema.ID + " INTEGER PRIMARY KEY" + "," + ArtcileSchema.NOVEL_ID
        // + " INTEGER NOT NULL" + "," + ArtcileSchema.TEXT + " TEXT NOT NULL" + "," + ArtcileSchema.TITLE + " TEXT NOT NULL" + ","
        // + ArtcileSchema.SUBJECT + " TEXT NOT NULL" + "," + ArtcileSchema.IS_DOWNLOADED + " INTEGER NOT NULL" + "," + "FOREIGN KEY("
        // + ArtcileSchema.NOVEL_ID + ") REFERENCES " + NovelSchema.TABLE_NAME + "(" + NovelSchema.ID + ") ON UPDATE CASCADE" + ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + BookmarkSchema.TABLE_NAME + " (" + BookmarkSchema.ID + " INTEGER PRIMARY KEY" + ","
                + BookmarkSchema.NOVEL_ID + " INTEGER NOT NULL" + "," + BookmarkSchema.ARTICLE_ID + " INTEGER NOT NULL" + "," + BookmarkSchema.READ_RATE
                + " INTEGER NOT NULL" + "," + BookmarkSchema.NOVEL_NAME + " TEXT NOT NULL" + "," + BookmarkSchema.ARTICLE_TITLE + " TEXT NOT NULL" + ","
                + BookmarkSchema.NOVEL_PIC + " TEXT NOT NULL" + "," + BookmarkSchema.IS_RECENT_READ + " INTEGER NOT NULL" + ");");

    }

    public boolean deleteNovel(Novel novel) {
        Cursor cursor = db.rawQuery("DELETE FROM novels WHERE `novels`.`id` = ?", new String[] { novel.getId() + "" });
        cursor.moveToFirst();
        cursor.close();
        return true;
    }

    public boolean deleteBookmark(Bookmark bookmark) {
        Cursor cursor = db.rawQuery("DELETE FROM bookmarks WHERE `bookmarks`.`id` = ?", new String[] { bookmark.getId() + "" });
        cursor.moveToFirst();
        cursor.close();
        return true;
    }
    
    public boolean deleteBookmarks(ArrayList<Bookmark> bookmarks) {

        if (bookmarks.size() == 0)
            return false;

        String idLst = "";
        for (int i = 0; i < bookmarks.size(); i++)
            idLst = bookmarks.get(i).getId() + "," + idLst;
        idLst = idLst.substring(0, idLst.length() - 1);

        Cursor cursor = null;
        cursor = db.rawQuery("DELETE FROM " + BookmarkSchema.TABLE_NAME + " WHERE id in (" + idLst + ")", null);
        cursor.moveToFirst();
        cursor.close();
        return true;

    }

    public boolean deleteArticle(Article article) {
        Cursor cursor = db.rawQuery("DELETE FROM articles WHERE `articles`.`id` = ?", new String[] { article.getId() + "" });
        cursor.moveToFirst();
        cursor.close();
        return true;
    }

    public boolean deleteArticles(ArrayList<Article> articles) {

        if (articles.size() == 0)
            return false;

        String idLst = "";
        for (int i = 0; i < articles.size(); i++)
            idLst = articles.get(i).getId() + "," + idLst;
        idLst = idLst.substring(0, idLst.length() - 1);

        Cursor cursor = null;
        cursor = db.rawQuery("DELETE FROM " + ArtcileSchema.TABLE_NAME + " WHERE id in (" + idLst + ")", null);
        cursor.moveToFirst();
        cursor.close();
        return true;

    }

    public long insertBookmark(Bookmark bookmark) {
        ContentValues args = new ContentValues();
        args.put(BookmarkSchema.NOVEL_ID, bookmark.getNovelId());
        args.put(BookmarkSchema.ARTICLE_ID, bookmark.getArticleId());
        args.put(BookmarkSchema.READ_RATE, bookmark.getReadRate());
        args.put(BookmarkSchema.NOVEL_NAME, bookmark.getNovelName());
        args.put(BookmarkSchema.ARTICLE_TITLE, bookmark.getArticleTitle());
        args.put(BookmarkSchema.NOVEL_PIC, bookmark.getNovelPic());
        args.put(BookmarkSchema.IS_RECENT_READ, getSQLiteBoolean(bookmark.isRecentRead()));
        return db.insert(BookmarkSchema.TABLE_NAME, null, args);
    }

    // public Bookmark getNovelRecentBookmark(int novel_id) {
    // Cursor cursor = null;
    // cursor = db.rawQuery("SELECT * FROM " + BookmarkSchema.TABLE_NAME + " WHERE is_recent_read = 0 and novel_id = " + novel_id, null);
    // Bookmark bookmark = null;
    // while (cursor.moveToNext()) {
    // int ID = cursor.getInt(0);
    // int NOVEL_ID = cursor.getInt(1);
    // int ARTICLE_ID = cursor.getInt(2);
    // int READ_RATE = cursor.getInt(3);
    // String NOVEL_NAME = cursor.getString(4);
    // String ARTICLE_TITLE = cursor.getString(5);
    // String NOVEL_PIC = cursor.getString(6);
    // Boolean IS_RECENT_READ = cursor.getInt(7) > 0;
    //
    // bookmark = new Bookmark(ID, NOVEL_ID, ARTICLE_ID, READ_RATE, NOVEL_NAME, ARTICLE_TITLE, NOVEL_PIC, IS_RECENT_READ);
    // }
    // return bookmark;
    // }

    public boolean updateBookmark(Bookmark bookmark) {
        Cursor cursor = db.rawQuery(
                "UPDATE bookmarks SET `novel_id` = ?, `article_id` = ?, `read_rate` = ? , `novel_name` = ?, `article_title` = ? WHERE `bookmarks`.`id` = ?",
                new String[] { bookmark.getNovelId() + "", bookmark.getArticleId() + "", bookmark.getReadRate() + "", bookmark.getNovelName(),
                        bookmark.getArticleTitle(), bookmark.getId() + "" });
        cursor.moveToFirst();
        cursor.close();
        return true;
    }

    public ArrayList<Bookmark> getAllRecentReadBookmarks() {
        Cursor cursor = null;
        ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
        cursor = db.rawQuery("SELECT * FROM " + BookmarkSchema.TABLE_NAME + " WHERE is_recent_read != 0 ORDER BY id DESC", null);

        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            int NOVEL_ID = cursor.getInt(1);
            int ARTICLE_ID = cursor.getInt(2);
            int READ_RATE = cursor.getInt(3);
            String NOVEL_NAME = cursor.getString(4);
            String ARTICLE_TITLE = cursor.getString(5);
            String NOVEL_PIC = cursor.getString(6);
            Boolean IS_RECENT_READ = cursor.getInt(7) > 0;

            Bookmark bookmark = new Bookmark(ID, NOVEL_ID, ARTICLE_ID, READ_RATE, NOVEL_NAME, ARTICLE_TITLE, NOVEL_PIC, IS_RECENT_READ);
            bookmarks.add(bookmark);
        }
        cursor.close();
        return bookmarks;
    }

    public ArrayList<Bookmark> getAllBookmarks() {
        Cursor cursor = null;
        ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
        cursor = db.rawQuery("SELECT * FROM " + BookmarkSchema.TABLE_NAME + " WHERE is_recent_read = 0 ORDER BY id DESC", null);

        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            int NOVEL_ID = cursor.getInt(1);
            int ARTICLE_ID = cursor.getInt(2);
            int READ_RATE = cursor.getInt(3);
            String NOVEL_NAME = cursor.getString(4);
            String ARTICLE_TITLE = cursor.getString(5);
            String NOVEL_PIC = cursor.getString(6);
            Boolean IS_RECENT_READ = cursor.getInt(7) > 0;

            Bookmark bookmark = new Bookmark(ID, NOVEL_ID, ARTICLE_ID, READ_RATE, NOVEL_NAME, ARTICLE_TITLE, NOVEL_PIC, IS_RECENT_READ);
            bookmarks.add(bookmark);
        }
        cursor.close();
        return bookmarks;
    }
    
    public ArrayList<Bookmark> getLastBookmarks(int num) {
        Cursor cursor = null;
        ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
        cursor = db.rawQuery("SELECT * FROM " + BookmarkSchema.TABLE_NAME + " WHERE is_recent_read = 0 ORDER BY id DESC LIMIT "+num, null);

        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            int NOVEL_ID = cursor.getInt(1);
            int ARTICLE_ID = cursor.getInt(2);
            int READ_RATE = cursor.getInt(3);
            String NOVEL_NAME = cursor.getString(4);
            String ARTICLE_TITLE = cursor.getString(5);
            String NOVEL_PIC = cursor.getString(6);
            Boolean IS_RECENT_READ = cursor.getInt(7) > 0;

            Bookmark bookmark = new Bookmark(ID, NOVEL_ID, ARTICLE_ID, READ_RATE, NOVEL_NAME, ARTICLE_TITLE, NOVEL_PIC, IS_RECENT_READ);
            bookmarks.add(bookmark);
        }
        cursor.close();
        return bookmarks;
    }
    
    public ArrayList<Bookmark> getLastRecentBookmarks(int num) {
        Cursor cursor = null;
        ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
        cursor = db.rawQuery("SELECT * FROM " + BookmarkSchema.TABLE_NAME + " WHERE is_recent_read = 1 ORDER BY id LIMIT "+num, null);

        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            int NOVEL_ID = cursor.getInt(1);
            int ARTICLE_ID = cursor.getInt(2);
            int READ_RATE = cursor.getInt(3);
            String NOVEL_NAME = cursor.getString(4);
            String ARTICLE_TITLE = cursor.getString(5);
            String NOVEL_PIC = cursor.getString(6);
            Boolean IS_RECENT_READ = cursor.getInt(7) > 0;

            Bookmark bookmark = new Bookmark(ID, NOVEL_ID, ARTICLE_ID, READ_RATE, NOVEL_NAME, ARTICLE_TITLE, NOVEL_PIC, IS_RECENT_READ);
            bookmarks.add(bookmark);
        }
        cursor.close();
        return bookmarks;
    }

    // public ArrayList<Bookmark> getNovelBookmarks(int novelId) {
    // Cursor cursor = null;
    // ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
    // cursor = db.rawQuery("SELECT * FROM " + BookmarkSchema.TABLE_NAME + " WHERE novel_id = \'" + novelId + "\' AND is_recent_read = 0", null);
    //
    // while (cursor.moveToNext()) {
    // int ID = cursor.getInt(0);
    // int NOVEL_ID = cursor.getInt(1);
    // int ARTICLE_ID = cursor.getInt(2);
    // int READ_RATE = cursor.getInt(3);
    // String NOVEL_NAME = cursor.getString(4);
    // String ARTICLE_TITLE = cursor.getString(5);
    // String NOVEL_PIC = cursor.getString(6);
    // Boolean IS_RECENT_READ = cursor.getInt(7) > 0;
    //
    // Bookmark bookmark = new Bookmark(ID, NOVEL_ID, ARTICLE_ID, READ_RATE, NOVEL_NAME, ARTICLE_TITLE, NOVEL_PIC, IS_RECENT_READ);
    // bookmarks.add(bookmark);
    // }
    // return bookmarks;
    // }

    public boolean updateNovel(Novel novel) {
        Cursor cursor = db
                .rawQuery(
                        "UPDATE novels SET `article_num` = ?, `last_update` = ?, `is_serializing` = ? , `is_collected` = ? , `is_downloaded` = ? WHERE `novels`.`id` = ?",
                        new String[] { novel.getArticleNum(), novel.getLastUpdate(), getSQLiteBoolean(novel.isSerializing()) + "",
                                getSQLiteBoolean(novel.isCollected()) + "", getSQLiteBoolean(novel.isDownloaded()) + "", novel.getId() + "" });
        cursor.moveToFirst();
        cursor.close();
        return true;
    }

    public boolean updateArticle(Article article) {
        Cursor cursor = db.rawQuery("UPDATE articles SET `text` = ?, `is_downloaded` = ? WHERE `articles`.`id` = ?", new String[] { article.getText(),
                getSQLiteBoolean(article.isDownload()) + "", article.getId() + "" });
        cursor.moveToFirst();
        cursor.close();
        return true;
    }

    public Novel getNovel(int novel_id) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + NovelSchema.TABLE_NAME + " WHERE id = \'" + novel_id + "\'", null);
        Novel novel = null;
        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            String NAME = cursor.getString(1);
            String AUTHOR = cursor.getString(2);
            String DESCRIPTION = cursor.getString(3);
            String PIC = cursor.getString(4);
            int CATEGORY_ID = cursor.getInt(5);
            String ARTICLE_NUM = cursor.getString(6);
            String LAST_UPDATE = cursor.getString(7);
            Boolean IS_SERIALIZING = cursor.getInt(8) > 0;
            Boolean IS_COLLECTED = cursor.getInt(9) > 0;
            Boolean IS_DOWNLOADED = cursor.getInt(10) > 0;
            novel = new Novel(ID, NAME, AUTHOR, DESCRIPTION, PIC, CATEGORY_ID, ARTICLE_NUM, LAST_UPDATE, IS_SERIALIZING, IS_COLLECTED, IS_DOWNLOADED);
        }
        cursor.close();
        return novel;
    }

    public ArrayList<Article> getArticleDownloadInfo(ArrayList<Article> articles) {

        if (articles.size() == 0)
            return articles;

        HashMap hash = new HashMap();

        String idLst = "";
        for (int i = 0; i < articles.size(); i++)
            idLst = articles.get(i).getId() + "," + idLst;
        idLst = idLst.substring(0, idLst.length() - 1);

        Cursor cursor = null;
        cursor = db.rawQuery("SELECT id,is_downloaded FROM " + ArtcileSchema.TABLE_NAME + " WHERE id in (" + idLst + ")", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            Boolean is_downloaded = cursor.getInt(1) > 0;
            hash.put(id, is_downloaded);
        }

        for (int i = 0; i < articles.size(); i++) {
            Boolean value = (Boolean) hash.get(articles.get(i).getId());
            if (value != null)
                articles.get(i).setIsDownloaded(value);
        }
        cursor.close();
        return articles;
    }

    public ArrayList<Article> getNovelArticles(int novel_id, boolean isOrderUp) {
        Cursor cursor = null;
        ArrayList<Article> articles = new ArrayList<Article>();
        if (isOrderUp)
            cursor = db.rawQuery("SELECT id,novel_id,title,subject,is_downloaded,num FROM " + ArtcileSchema.TABLE_NAME + " WHERE novel_id = \'" + novel_id
                    + "\' order by num ASC", null);
        else
            cursor = db.rawQuery("SELECT id,novel_id,title,subject,is_downloaded,num FROM " + ArtcileSchema.TABLE_NAME + " WHERE novel_id = \'" + novel_id
                    + "\' ORDER BY num DESC", null);

        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            int NOVEL_ID = cursor.getInt(1);
            String TITLE = cursor.getString(2);
            String SUBJECT = cursor.getString(3);
            boolean IS_DOWNLOADED = cursor.getInt(4) > 0;
            int NUM = cursor.getInt(5);
            Article article = new Article(ID, NOVEL_ID, "", TITLE, SUBJECT, IS_DOWNLOADED, NUM);
            articles.add(article);
        }
        cursor.close();
        return articles;
    }

    public boolean isArticleExists(int articleId) {
        Cursor cursor = db.rawQuery("select 1 from " + ArtcileSchema.TABLE_NAME + " where id = " + articleId, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public boolean isNovelExists(int novelId) {
        Cursor cursor = db.rawQuery("select 1 from " + NovelSchema.TABLE_NAME + " where id = " + novelId, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public Article getArticle(int article_id) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + ArtcileSchema.TABLE_NAME + " WHERE id = \'" + article_id + "\'", null);
        Article article = null;
        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            int NOVEL_ID = cursor.getInt(1);
            String TEXT = cursor.getString(2);
            String TITLE = cursor.getString(3);
            String SUBJECT = cursor.getString(4);
            boolean IS_DOWNLOADED = cursor.getInt(5) > 0;
            int NUM = cursor.getInt(6);
            article = new Article(ID, NOVEL_ID, TEXT, TITLE, SUBJECT, IS_DOWNLOADED, NUM);
        }
        cursor.close();
        return article;
    }

    public long insertArticle(Article article) {

        ContentValues args = new ContentValues();
        args.put(ArtcileSchema.ID, article.getId());
        args.put(ArtcileSchema.NOVEL_ID, article.getNovelId());
        args.put(ArtcileSchema.TEXT, article.getText());
        args.put(ArtcileSchema.TITLE, article.getTitle());
        args.put(ArtcileSchema.SUBJECT, article.getSubject());
        args.put(ArtcileSchema.IS_DOWNLOADED, getSQLiteBoolean(article.isDownload()));
        args.put(ArtcileSchema.NUM, article.getNum());
        return db.insert(ArtcileSchema.TABLE_NAME, null, args);
    }
    
    public ArrayList<Novel> getLastCollectNovels(int num){
    	Cursor cursor = null;
        ArrayList<Novel> novels = new ArrayList<Novel>();
        cursor = db.rawQuery("SELECT * FROM " + NovelSchema.TABLE_NAME + " WHERE is_collected != 0 ORDER BY id DESC LIMIT "+num, null);
        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            String NAME = cursor.getString(1);
            String AUTHOR = cursor.getString(2);
            String DESCRIPTION = cursor.getString(3);
            String PIC = cursor.getString(4);
            int CATEGORY_ID = cursor.getInt(5);
            String ARTICLE_NUM = cursor.getString(6);
            String LAST_UPDATE = cursor.getString(7);
            Boolean IS_SERIALIZING = cursor.getInt(8) > 0;
            Boolean IS_COLLECTED = cursor.getInt(9) > 0;
            Boolean IS_DOWNLOADED = cursor.getInt(10) > 0;
            Novel novel = new Novel(ID, NAME, AUTHOR, DESCRIPTION, PIC, CATEGORY_ID, ARTICLE_NUM, LAST_UPDATE, IS_SERIALIZING, IS_COLLECTED, IS_DOWNLOADED);
            novels.add(novel);
        }
        cursor.close();
        return novels;
    }
    
    public ArrayList<Novel> getLastDownloadNovels(int num){
    	Cursor cursor = null;
        ArrayList<Novel> novels = new ArrayList<Novel>();
        cursor = db.rawQuery("SELECT * FROM " + NovelSchema.TABLE_NAME + " WHERE is_downloaded != 0 ORDER BY id DESC LIMIT "+num, null);
        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            String NAME = cursor.getString(1);
            String AUTHOR = cursor.getString(2);
            String DESCRIPTION = cursor.getString(3);
            String PIC = cursor.getString(4);
            int CATEGORY_ID = cursor.getInt(5);
            String ARTICLE_NUM = cursor.getString(6);
            String LAST_UPDATE = cursor.getString(7);
            Boolean IS_SERIALIZING = cursor.getInt(8) > 0;
            Boolean IS_COLLECTED = cursor.getInt(9) > 0;
            Boolean IS_DOWNLOADED = cursor.getInt(10) > 0;
            Novel novel = new Novel(ID, NAME, AUTHOR, DESCRIPTION, PIC, CATEGORY_ID, ARTICLE_NUM, LAST_UPDATE, IS_SERIALIZING, IS_COLLECTED, IS_DOWNLOADED);
            novels.add(novel);
        }
        cursor.close();
        return novels;
    }

    public ArrayList<Novel> getCollectedNovels() {
        Cursor cursor = null;
        ArrayList<Novel> novels = new ArrayList<Novel>();
        cursor = db.rawQuery("SELECT * FROM " + NovelSchema.TABLE_NAME + " WHERE is_collected != 0", null);
        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            String NAME = cursor.getString(1);
            String AUTHOR = cursor.getString(2);
            String DESCRIPTION = cursor.getString(3);
            String PIC = cursor.getString(4);
            int CATEGORY_ID = cursor.getInt(5);
            String ARTICLE_NUM = cursor.getString(6);
            String LAST_UPDATE = cursor.getString(7);
            Boolean IS_SERIALIZING = cursor.getInt(8) > 0;
            Boolean IS_COLLECTED = cursor.getInt(9) > 0;
            Boolean IS_DOWNLOADED = cursor.getInt(10) > 0;
            Novel novel = new Novel(ID, NAME, AUTHOR, DESCRIPTION, PIC, CATEGORY_ID, ARTICLE_NUM, LAST_UPDATE, IS_SERIALIZING, IS_COLLECTED, IS_DOWNLOADED);
            novels.add(novel);
        }
        cursor.close();
        return novels;
    }

    public ArrayList<Novel> getDownloadNovels() {
        Cursor cursor = null;
        ArrayList<Novel> novels = new ArrayList<Novel>();
        cursor = db.rawQuery("SELECT * FROM " + NovelSchema.TABLE_NAME + " WHERE is_downloaded = 1", null);
        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            String NAME = cursor.getString(1);
            String AUTHOR = cursor.getString(2);
            String DESCRIPTION = cursor.getString(3);
            String PIC = cursor.getString(4);
            int CATEGORY_ID = cursor.getInt(5);
            String ARTICLE_NUM = cursor.getString(6);
            String LAST_UPDATE = cursor.getString(7);
            Boolean IS_SERIALIZING = cursor.getInt(8) > 0;
            Boolean IS_COLLECTED = cursor.getInt(9) > 0;
            Boolean IS_DOWNLOADED = cursor.getInt(10) > 0;
            Novel novel = new Novel(ID, NAME, AUTHOR, DESCRIPTION, PIC, CATEGORY_ID, ARTICLE_NUM, LAST_UPDATE, IS_SERIALIZING, IS_COLLECTED, IS_DOWNLOADED);
            novels.add(novel);
        }
        cursor.close();
        return novels;
    }

    public long insertNovel(Novel novel) {

        ContentValues args = new ContentValues();
        args.put(NovelSchema.ID, novel.getId());
        args.put(NovelSchema.NAME, novel.getName());
        args.put(NovelSchema.AUTHOR, novel.getAuthor());
        args.put(NovelSchema.DESCRIPTION, novel.getDescription());
        args.put(NovelSchema.PIC, novel.getPic());
        args.put(NovelSchema.CATEGORY_ID, novel.getCategoryId());
        args.put(NovelSchema.ARTICLE_NUM, novel.getArticleNum());
        args.put(NovelSchema.LAST_UPDATE, novel.getLastUpdate());
        args.put(NovelSchema.IS_SERIALIZING, getSQLiteBoolean(novel.isSerializing()));
        args.put(NovelSchema.IS_COLLECTED, getSQLiteBoolean(novel.isCollected()));
        args.put(NovelSchema.IS_DOWNLOAD, getSQLiteBoolean(novel.isDownloaded()));

        return db.insert(NovelSchema.TABLE_NAME, null, args);
    }

    static int getSQLiteBoolean(boolean b) {
        if (b)
            return 1;
        else
            return 0;
    }

    public Boolean isNovelCollected(int novel_id) {
        Cursor cursor = db.rawQuery("select 1 from " + NovelSchema.TABLE_NAME + " where id = " + novel_id + " and is_collected = 1", null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public Boolean isNovelDownloaded(int novel_id) {
        Cursor cursor = db.rawQuery("select 1 from " + NovelSchema.TABLE_NAME + " where id = " + novel_id + " and is_downloaded = 1", null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    // public Boolean isNovelBookmarked(int novel_id) {
    // Cursor cursor = db.rawQuery("select 1 from " + BookmarkSchema.TABLE_NAME + " where novel_id = " + novel_id + " and is_recent_read = 1", null);
    // boolean exists = (cursor.getCount() > 0);
    // cursor.close();
    // return exists;
    // }

    public Bookmark getNovelBookmark(int novel_id) {
        Cursor cursor = null;
        Bookmark bookmark = null;
        cursor = db.rawQuery("SELECT * FROM " + BookmarkSchema.TABLE_NAME + " where novel_id = " + novel_id + " and is_recent_read = 1", null);

        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            int NOVEL_ID = cursor.getInt(1);
            int ARTICLE_ID = cursor.getInt(2);
            int READ_RATE = cursor.getInt(3);
            String NOVEL_NAME = cursor.getString(4);
            String ARTICLE_TITLE = cursor.getString(5);
            String NOVEL_PIC = cursor.getString(6);
            Boolean IS_RECENT_READ = cursor.getInt(7) > 0;

            bookmark = new Bookmark(ID, NOVEL_ID, ARTICLE_ID, READ_RATE, NOVEL_NAME, ARTICLE_TITLE, NOVEL_PIC, IS_RECENT_READ);
        }
        cursor.close();
        return bookmark;
    }

    public Boolean removeNovelFromCollected(Novel novel) {
        novel.setIsCollected(false);
        return updateNovel(novel);
    }

    public Boolean removeNovelFromDownload(Novel novel) {
        novel.setIsDownload(false);
        return updateNovel(novel);
    }

	public Bookmark findBookMarkByArticle(Article article) {
		Cursor cursor = null;
        cursor = db.rawQuery("SELECT * FROM " + BookmarkSchema.TABLE_NAME + " WHERE is_recent_read = 0 AND novel_id = " + article.getNovelId() + " AND article_id = " + article.getId() +" ORDER BY id DESC LIMIT 1", null);
        Bookmark bookmark = null;
		while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            int NOVEL_ID = cursor.getInt(1);
            int ARTICLE_ID = cursor.getInt(2);
            int READ_RATE = cursor.getInt(3);
            String NOVEL_NAME = cursor.getString(4);
            String ARTICLE_TITLE = cursor.getString(5);
            String NOVEL_PIC = cursor.getString(6);
            Boolean IS_RECENT_READ = cursor.getInt(7) > 0;

            bookmark = new Bookmark(ID, NOVEL_ID, ARTICLE_ID, READ_RATE, NOVEL_NAME, ARTICLE_TITLE, NOVEL_PIC, IS_RECENT_READ);
        }
        cursor.close();
        
		return bookmark;
	}

}
