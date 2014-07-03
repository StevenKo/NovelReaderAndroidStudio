package com.novel.reader.entity;

public class Bookmark {
    int     id;
    int     novelId;
    int     articleId;
    int     rate;
    String  novelName;
    String  articleTitle;
    String  novelPic;
    boolean is_recent_read;

    public Bookmark() {
        this(1, 1, 1, 23, "test", "test2", "test", true);
    }

    public Bookmark(int id, int novelId, int articleId, int rate, String novelName, String articleTitle, String novelPic, Boolean is_recent_read) {
        this.id = id;
        this.novelId = novelId;
        this.articleId = articleId;
        this.rate = rate;
        this.novelName = novelName;
        this.articleTitle = articleTitle;
        this.novelPic = novelPic;
        this.is_recent_read = is_recent_read;
    }

    public boolean isRecentRead() {
        return is_recent_read;
    }

    public String getNovelPic() {
        return this.novelPic;
    }

    public String getArticleTitle() {
        return this.articleTitle;
    }

    public String getNovelName() {
        return this.novelName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getNovelId() {
        return novelId;
    }

    public int getArticleId() {
        return articleId;
    }

    public int getReadRate() {
        return rate;
    }

}
