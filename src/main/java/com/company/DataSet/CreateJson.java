package com.company.DataSet;

import com.company.Wikipedia.TAG;
import org.json.JSONArray;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CreateJson {

    final double IDF_Cutoff = 0.01;

    public JSONArray createListofWords(List<List<String>> stemmedWords, Map<String, Double> sortedTFIDF,
                                       List<List<String>> splitWords, Map<Integer, TAG> articleLabel){
        Map <String, Object> map1 = new HashMap<>();
        JSONArray respJSON = new JSONArray();
        int index = 0;
        for(int i=0; i<stemmedWords.size(); i++){
            List<String> wordList = new ArrayList<>();
            List<String> nerTags = new ArrayList<>();
            List<Integer> nerIDS = new ArrayList<>();
            List<Boolean> spaceAfter = new ArrayList<>();
            List<String> stemmedWordsGet = stemmedWords.get(i);
            List<String> wordsNotStemmedGet = splitWords.get(i);
            for (int j=0; j<stemmedWordsGet.size(); j++){
                String wordStemmed = stemmedWordsGet.get(j);
                String wordNotStemmed;
                if(j<wordsNotStemmedGet.size())
                    wordNotStemmed = wordsNotStemmedGet.get(j);
                else
                    wordNotStemmed = wordStemmed;
                wordList.add(wordNotStemmed);
                spaceAfter.add(true);
                if(sortedTFIDF.get(wordStemmed)!=null) {
                    if (sortedTFIDF.get(wordStemmed) > IDF_Cutoff) {
                        if(articleLabel.get(i)==TAG.GASTRO) {
                            nerTags.add("GASTRO");
                            nerIDS.add(1);
                        }
                        else{
                            if(articleLabel.get(i)==TAG.OCIT) {
                                nerTags.add("OCIT");
                                nerIDS.add(2);
                            }
                            else {
                                nerTags.add("O");
                                nerIDS.add(0);
                            }
                        }
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
