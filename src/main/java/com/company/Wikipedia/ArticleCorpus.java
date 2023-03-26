package com.company.Wikipedia;

import java.util.ArrayList;

public class ArticleCorpus {
    private ArrayList<ArticleLine> list = new ArrayList<>();

    public ArticleCorpus(ArrayList<ArticleLine> list) {
        this.list = list;
    }

    public ArticleCorpus() {

    }

    public void AppendArticle(ArticleLine articleLine){
        list.add(articleLine);
    }

    public ArrayList<ArticleLine> getList() {
        return list;
    }
}
