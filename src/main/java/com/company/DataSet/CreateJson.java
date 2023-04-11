package com.company.DataSet;

import org.json.JSONArray;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CreateJson {

    public JSONArray createListofWords(List<List<String>> splitWords, Map<String, Double> sortedTFIDF){
        Map <String, Object> map1 = new HashMap<>();
        JSONArray respJSON = new JSONArray();
        int index = 0;
        for(List<String> article:splitWords){
            List<String> wordList = new ArrayList<>();
            List<String> nerTags = new ArrayList<>();
            List<Integer> nerIDS = new ArrayList<>();
            List<Boolean> spaceAfter = new ArrayList<>();
            for (String word:article){
                wordList.add(word);
                spaceAfter.add(true);
                if(sortedTFIDF.get(word)!=null) {
                    if (sortedTFIDF.get(word) > 0.08) {
                        nerTags.add("Medical");
                        nerIDS.add(1);
                    } else {
                        nerTags.add("O");
                        nerIDS.add(0);
                    }
                }
                else{
                    nerTags.add("O");
                    nerIDS.add(0);
                }
            }
            map1.put("id", index);
            index++;
            map1.put("tokens", wordList);
            map1.put("ner_tags", nerTags);
            map1.put("ner_ids", nerIDS);
            map1.put("space_after", spaceAfter);

            respJSON.put(map1);
        }
        return  respJSON;
    }

    public void writeToFile(JSONArray jsonArray){
        try {
            FileWriter file = new FileWriter("output.txt");
            file.write(jsonArray.toString());
            file.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
