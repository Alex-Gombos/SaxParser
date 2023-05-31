package com.company.Wikipedia;

import java.util.ArrayList;

public class ArticleCorpus {
    private ArrayList<Article> list = new ArrayList<>();

    public ArticleCorpus() {

    }
    public void AppendArticle(Article article){
        list.add(article);
    }

    public void RemoveArticle(int articleIndex){
        list.remove(articleIndex);
    }

    public ArrayList<Article> getArticles() {
        return list;
    }
}
