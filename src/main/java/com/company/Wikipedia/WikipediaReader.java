package com.company.Wikipedia;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import javafx.util.Pair;

public class WikipediaReader {
    FileInputStream inputStream = null;
    FileOutputStream outputStream = null;
    Scanner sc = null;

    public void read(String uri, ArticleCorpus articleCorpus) throws IOException {
        try {
            inputStream = new FileInputStream(uri);
            sc = new Scanner(inputStream, StandardCharsets.UTF_8);
            ArticleLine articleLine = new ArticleLine();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                //System.out.println(line);
                if (!line.isEmpty()) {
                    articleLine.AddWord(line);
                } else {
                    articleCorpus.AppendArticle(articleLine);
                    articleLine = new ArticleLine();
                }

            }
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                try {
                    throw sc.ioException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }
    }

    public void out(File file, ArticleCorpus articleCorpus) {
        try {
            outputStream = new FileOutputStream(file);
            ArrayList<ArticleLine> arrayList = articleCorpus.getList();
            byte[] strToBytes;
            for (ArticleLine item : arrayList) {
                ArrayList<String> vector = item.getStrings();
                for (String item2 : vector) {
                    strToBytes = item2.getBytes();
                    outputStream.write(strToBytes);
                    System.out.println(item2);
                }
                System.out.println("NEW ARTICLE");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void filter(ArticleCorpus articleCorpus, List<String> tokensList) {
        String patternString = "\\b(" + StringUtils.join(tokensList, "|") + ")\\b"; // sa pun fara boundary
        Pattern pattern = Pattern.compile(patternString);
        //pattern.split() TODO inlocuieste cu split
        List<ArticleLine> removeList = new ArrayList<>();
        for (ArticleLine item : articleCorpus.getList()) {
            boolean matchFound = false;
            for (String item2 : item.getStrings()) {
                Matcher matcher = pattern.matcher(item2);
                if (matcher.find())
                    matchFound = true;
            }
            if (!matchFound) {
                removeList.add(item);
            }
        }
        articleCorpus.getList().removeAll(removeList);
    }

    public void prepareText(List<String> articleSentence){
        for(int i=0; i<articleSentence.size(); i++){
            articleSentence.set(i, articleSentence.get(i).replaceAll("\\(",""));
            articleSentence.set(i, articleSentence.get(i).replaceAll("\\(",""));
            articleSentence.set(i, articleSentence.get(i).replaceAll("\\)",""));
            articleSentence.set(i, articleSentence.get(i).replaceAll("\\.",""));
            articleSentence.set(i, articleSentence.get(i).replaceAll("\\+",""));
            articleSentence.set(i, articleSentence.get(i).replaceAll("-",""));
            articleSentence.set(i, articleSentence.get(i).replaceAll(";",""));
            articleSentence.set(i, articleSentence.get(i).replaceAll(",",""));
            articleSentence.set(i, articleSentence.get(i).replaceAll(":",""));
        }
    }

    public List<List<String>> splitWords(ArticleCorpus articleCorpus) {
        List<List<String>> splitWords = new ArrayList<>();
        List<String> items;
        for (ArticleLine item : articleCorpus.getList()) {
            StringBuilder stringBuilder = new StringBuilder();
            prepareText(item.getStrings());
            for(String item2:item.getStrings()){
                stringBuilder.append(item2).append(" ");
            }
            items = Arrays.asList(stringBuilder.toString().split(" "));
            splitWords.add(items);
        }
        return splitWords;
    }

    public List<List<String>> findWordsInWiktionary(ArticleCorpus articleCorpus, HashMap<String, String> wiktionaryWords) {
        List<String> tempList = new ArrayList<>();
        List<List<String>> list = splitWords(articleCorpus);
        List<List<String>> newList = new ArrayList<>();
        for(List<String> item:list){
            for(String item1:item) {
                if(wiktionaryWords.containsKey(item1)) {
                    //if (!tempList.contains(wiktionaryWords.get(item1))) remove duplicates
                        tempList.add(wiktionaryWords.get(item1));
                }
                else {
                    //if(!tempList.contains(item1)) remove duplicates
                        tempList.add(item1);
                }
            }
            newList.add(tempList);
            tempList = new ArrayList<>();
        }


        return newList;
    }

    public void countWords(List<List<String>> list, HashMap<String, Pair<List<String>, Integer>> wikipediaDict) {
        for(List<String> item:list){
            for(String item1:item) {
                if(!wikipediaDict.containsKey(item1)){
                    Pair<List<String>, Integer> pair = new Pair<>(item, 0);
                    wikipediaDict.put(item1, pair);
                }
                else{
                    if(wikipediaDict.get(item1).getKey()!=item) {
                        Pair<List<String>, Integer> pair = new Pair<>(item, wikipediaDict.get(item1).getValue()+1);
                        wikipediaDict.put(item1,pair);
                    }
                }
            }
        }
    }
    public void countWordsNew(List<List<String>> splitWords, HashMap<String, HashSet<Integer>> mapWords ) {
        for (List<String> strings : splitWords) {
            for (String item2 : strings) {
                HashSet<Integer> set = new HashSet<>();
                if(mapWords.containsKey(item2)){
                    set = mapWords.get(item2);
                }
                set.add(splitWords.indexOf(strings));
                mapWords.put(item2, set);
            }
        }
    }
    public static double log2(double N) {

        // calculate log2 N indirectly
        // using log() method

        return (Math.log(N) / Math.log(2));
    }
    public HashMap<String, Double> countWordsTFIDF(List<List<String>> splitWords, HashMap<String, Double> hashMapTFIDF) {
        HashMap<String, Vector<Integer>> mapWords = new HashMap<>();
        for (List<String> strings : splitWords) {
            for (String item2 : strings) {
                Vector<Integer> vector = new Vector<>();
                if(mapWords.containsKey(item2)){
                    vector = mapWords.get(item2);
                }
                vector.add(splitWords.indexOf(strings));
                mapWords.put(item2, vector);
            }
        }

        for(String key:mapWords.keySet()) {
           Collections.sort(mapWords.get(key));
           int i=mapWords.get(key).get(0);
           int timeInArticle = 0;
           for(Integer integer:mapWords.get(key)){
               if(integer==i){
                   timeInArticle++;
               }
               else {
                   i++;
               }
                   double tf = (double) timeInArticle/splitWords.get(i).size();
                   int articlesWithWord = 1;
                   int oldValue = mapWords.get(key).get(0);
                   for(Integer integer2:mapWords.get(key)){
                       if(oldValue!=integer2){
                           articlesWithWord++;
                           oldValue = integer2;
                       }
                   }
                   double idf =log2(splitWords.size()/(double)articlesWithWord);

                   System.out.println(tf + " " + idf);

                   double tfidf = tf*idf;
                   hashMapTFIDF.put(key, tfidf);
           }
        }
        return hashMapTFIDF;
    }
    public Map<String, Double> sortByValue(Map<String, Double> unsortMap, final boolean order) {
        List<Map.Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));

    }
}
