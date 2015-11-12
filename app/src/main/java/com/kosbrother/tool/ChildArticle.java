package com.kosbrother.tool;

import com.novel.reader.entity.Article;

public class ChildArticle extends Article {

    private boolean isChecked;

    // public ChildArticle() {
    // this(1, 1, "", "", "", false);
    // }

    public ChildArticle(int id, int novelId, String text, String title, String subject, boolean isDownloaded, int num) {
        super(id, novelId, text, title, subject, isDownloaded, num);
        this.isChecked = false;
    }

    public void toggle() {
        this.isChecked = !this.isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean getChecked() {
        return this.isChecked;
    }

}
