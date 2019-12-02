package com.example.newsgateway;

import java.io.Serializable;
import java.util.ArrayList;

public class RestoreLayout implements Serializable {

    int cSource;
    int cArticle;
    ArrayList<Source> sourceList = new ArrayList<Source>();
    ArrayList<Article> articleList = new ArrayList<Article>();
    ArrayList<String> categories = new ArrayList<String>();


    public int getcSource() {
        return cSource;
    }

    public void setcSource(int cSource) {
        this.cSource = cSource;
    }

    public int getcArticle() {
        return cArticle;
    }

    public void setcArticle(int cArticle) {
        this.cArticle = cArticle;
    }

    public ArrayList<Source> getSourceList() {
        return sourceList;
    }

    public void setSourceList(ArrayList<Source> sourceList) {
        this.sourceList = sourceList;
    }

    public ArrayList<Article> getArticleList() {
        return articleList;
    }

    public void setArticleList(ArrayList<Article> articleList) {
        this.articleList = articleList;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }
}
