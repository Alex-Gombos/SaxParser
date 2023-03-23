package com.company.Wikipedia;

import java.util.ArrayList;

public class Article {
    private ArrayList<ArticleLine> list = new ArrayList<>();

    public Article(ArrayList<ArticleLine> list) {
        this.list = list;
    }

    public Article() {

    }

    public void AppendArticle(ArticleLine articleLine){
        list.add(articleLine);
    }

    public ArrayList<ArticleLine> getList() {
        return list;
    }
}
