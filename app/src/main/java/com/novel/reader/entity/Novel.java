package com.novel.reader.entity;

public class Novel {
    int     id;
    String  name;
    String  author;
    String  description;
    String  pic;
    int     categoryId;
    String  articleNum;
    String  lastUpdate;
    boolean isSerializing;
    boolean isCollected;
    boolean isDownloaded;

    public Novel() {
        this(1, "", "", "", "", 1, "", "", true, false, false);
    }

    public Novel(int id, String name, String author, String description, String pic, int categoryId, String articleNum, String lastUpdate,
            boolean isSerializing, boolean isCollected, boolean isDownloaded) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.description = description;
        this.pic = pic;
        this.categoryId = categoryId;
        this.articleNum = articleNum;
        this.lastUpdate = lastUpdate;
        this.isSerializing = isSerializing;
        this.isCollected = isCollected;
        this.isDownloaded = isDownloaded;
    }

    public void setIsCollected(boolean b) {
        this.isCollected = b;
    }

    public void setIsDownload(boolean b) {
        this.isDownloaded = b;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public int getId() {
        return id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public boolean isSerializing() {
        return isSerializing;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getPic() {
        return pic;
    }

    public String getArticleNum() {
        return articleNum;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

}
