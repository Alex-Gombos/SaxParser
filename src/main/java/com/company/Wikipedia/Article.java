package com.company.Wikipedia;

import java.util.ArrayList;

public class Article {

    private ArrayList<String> strings = new ArrayList<>();

    public Article() {
    }

    public void AddWord(String line){
        strings.add(line);
    }

    public ArrayList<String> getStrings() {
        return strings;
    }

}
