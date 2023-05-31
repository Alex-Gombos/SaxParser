package com.company.DataSet;

import com.company.Wikipedia.TAG;
import org.json.JSONArray;

import java.io.*;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.replace;

public class CreateJson {

    final double IDF_Cutoff = 0.01;
    public JSONArray createListofWordsSentence(List<List<String>> stemmedWords, Map<String, Double> sortedTFIDF,
                                       List<List<String>> splitIntoSentence, Map<Integer, TAG> articleLabel,
                                               List<String> nonMedicalWordsList){
        Map <String, Object> map1 = new HashMap<>();
        JSONArray respJSON = new JSONArray();
        int ids = 0;
        int indexStemmedWord = 0;
        int indexStemmedArticle = 0;

        Map<String, String> wordAndlabel = correctLabels();
        List<String> countLabel = new ArrayList<>();


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
                        if(!countLabel.contains("B-OTHER"))
                            countLabel.add("B-OTHER");
                        if(articleLabel.get(indexStemmedArticle)==TAG.GASTRO) {
                            nerTags.add("B-OTHER");
                            nerIDS.add(1);
                        }
                        else{
                            if(articleLabel.get(indexStemmedArticle)==TAG.OCIT) {
                                nerTags.add("B-OTHER");
                                nerIDS.add(2);
                            }
                            else {
                                nerTags.add("O");
                                nerIDS.add(0);
                            }
                        }

                        if(wordAndlabel.get(word)!=null) {
                            nerTags.set(nerTags.size() - 1, wordAndlabel.get(word));
                            if(!countLabel.contains(wordAndlabel.get(word)))
                                countLabel.set(countLabel.size()-1, wordAndlabel.get(word));
                        }
                        if(nonMedicalWordsList.contains(word)){
                            nerTags.set(nerTags.size() - 1, "O");
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
        System.out.println(countLabel.size());
        return  respJSON;
    }

    public Map<String, String> correctLabels(){
        String csvFile = "labelCorrection.csv";
        String line;
        String csvSplitBy = ",";
        Map<String, String> wordAndLabel = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                String[] data = line.split(csvSplitBy);
                if (data.length >= 3) {
                    String category = data[2].replace("\"", "").split("::")[0];
                    category = "B-" + category.toUpperCase();
                    wordAndLabel.put(data[1], category);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordAndLabel;
    }
    public void writeToFile(JSONArray jsonArray){
        try {
            FileWriter file = new FileWriter("outputSentence.txt");
            file.write(jsonArray.toString());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
