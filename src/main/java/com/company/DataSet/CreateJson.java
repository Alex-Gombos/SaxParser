package com.company.DataSet;

import com.company.Wikipedia.TAG;
import org.json.JSONArray;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CreateJson {

    final double IDF_Cutoff = 0.01;
    public JSONArray createListofWordsSentence(List<List<String>> stemmedWords, Map<String, Double> sortedTFIDF,
                                       List<List<String>> splitIntoSentence, Map<Integer, TAG> articleLabel){
        Map <String, Object> map1 = new HashMap<>();
        JSONArray respJSON = new JSONArray();
        int ids = 0;
        int indexStemmedWord = 0;
        int indexStemmedArticle = 0;
        for(List<String> sentence:splitIntoSentence){
            List<String> wordList = new ArrayList<>();
            List<String> nerTags = new ArrayList<>();
            List<Integer> nerIDS = new ArrayList<>();
            List<Boolean> spaceAfter = new ArrayList<>();
            for(String word:sentence){
                wordList.add(word);
                spaceAfter.add(true);
                if(indexStemmedArticle==314)
                    System.out.println(word);
                String wordStemmed = stemmedWords.get(indexStemmedArticle).get(indexStemmedWord);
                if(sortedTFIDF.get(wordStemmed)!=null){
                    if (sortedTFIDF.get(wordStemmed) > IDF_Cutoff) {
                        if(articleLabel.get(indexStemmedArticle)==TAG.GASTRO) {
                            nerTags.add("B-GASTRO");
                            nerIDS.add(1);
                        }
                        else{
                            if(articleLabel.get(indexStemmedArticle)==TAG.OCIT) {
                                nerTags.add("B-HEMA");
                                nerIDS.add(2);
                            }
                            else {
                                nerTags.add("O");
                                nerIDS.add(0);
                            }
                        }
                    }
                    else{
                        nerTags.add("O");
                        nerIDS.add(0);
                    }
                }
                else{
                    nerTags.add("O");
                    nerIDS.add(0);
                }
                indexStemmedWord++;
                if(indexStemmedWord==stemmedWords.get(indexStemmedArticle).size()){
                    indexStemmedWord = 0;
                    indexStemmedArticle++;
                }
            }
            spaceAfter.add(false);
            map1.put("id", ids);
            ids++;
            map1.put("tokens", wordList);
            map1.put("ner_tags", nerTags);
            map1.put("ner_ids", nerIDS);
            map1.put("space_after", spaceAfter);

            respJSON.put(map1);
        }
        System.out.println(indexStemmedArticle);
        return  respJSON;
    }
    public void writeToFile(JSONArray jsonArray){
        try {
            FileWriter file = new FileWriter("outputSentence.txt");
            file.write(jsonArray.toString());
            file.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
