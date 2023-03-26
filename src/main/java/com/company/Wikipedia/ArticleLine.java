package com.company.Wikipedia;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ArticleLine{

    private ArrayList<String> strings = new ArrayList<>();

    public ArticleLine() {
    }

    public void AddWord(String line){ // TODO: sa schimbi in add line
        //this.add(line);
        strings.add(line);
    }

    public ArrayList<String> getStrings() {
        return strings;
    }

    public void print(){
        for(String item:strings){
            System.out.println(item);
        }
    }
}
