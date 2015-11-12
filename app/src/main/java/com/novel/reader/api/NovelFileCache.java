package com.novel.reader.api;

import java.io.File;
import java.net.URLEncoder;

import android.content.Context;

public class NovelFileCache {

    private File cacheDir;

    public NovelFileCache(Context context, String novelName) {
        // Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "KosNovelReader/" + URLEncoder.encode(novelName));
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String name) {
        // I identify images by hashcode. Not a perfect solution, good for the demo.
        // String filename = String.valueOf(url.hashCode());
        // Another possible solution (thanks to grantland)
        String filename = URLEncoder.encode(name);
        File f = new File(cacheDir, filename);
        return f;

    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }

}