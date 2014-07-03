package com.novel.reader.entity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class Category {

    static String message = "[{\"id\":10,\"name\":\"\u540d\u8457\u53e4\u5178\"},{\"id\":11,\"name\":\"\u79d1\u666e\u5176\u5b83\"},{\"id\":14,\"name\":\"\u7384\u5e7b\u9b54\u6cd5\"},{\"id\":15,\"name\":\"\u6b66\u4fe0\u4fee\u771f\"},{\"id\":16,\"name\":\"\u90fd\u5e02\u8a00\u60c5\"},{\"id\":17,\"name\":\"\u6b77\u53f2\u8ecd\u4e8b\"},{\"id\":18,\"name\":\"\u6e38\u6232\u7af6\u6280\"},{\"id\":19,\"name\":\"\u79d1\u5e7b\u5c0f\u8aaa\"},{\"id\":20,\"name\":\"\u6050\u6016\u9748\u7570\"},{\"id\":21,\"name\":\"\u7f8e\u6587\u6563\u6587\"},{\"id\":22,\"name\":\"\u540c\u4eba\u5c0f\u8aaa\"},{\"id\":23,\"name\":\"\u8f15\u5c0f\u8aaa\"},{\"id\":24,\"name\":\"\u7a7f\u8d8a\u91cd\u751f\"}]";
    int           id;
    String        name;
    public ArrayList<Novel> novels;

    public Category() {
        this(-1, "");
    }

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCateName() {
        return name;
    }

    public void setCateName(String name) {
        this.name = name;
    }

    public static String getCategoryName(int id) {
        HashMap hash = new HashMap();
        JSONArray categoryArray;
        try {
            categoryArray = new JSONArray(message.toString());
            for (int i = 0; i < categoryArray.length(); i++) {
                int category_id = categoryArray.getJSONObject(i).getInt("id");
                String name = categoryArray.getJSONObject(i).getString("name");
                hash.put(category_id, name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return (String) hash.get(id);
    }

    public static ArrayList<Category> getCategories() {
        ArrayList<Category> cateogries = new ArrayList<Category>();
        JSONArray categoryArray;
        try {
            categoryArray = new JSONArray(message.toString());
            for (int i = 0; i < categoryArray.length(); i++) {
                int category_id = categoryArray.getJSONObject(i).getInt("id");
                String name = categoryArray.getJSONObject(i).getString("name");
                Category cat = new Category(category_id, name);
                cateogries.add(cat);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cateogries;
    }
}
