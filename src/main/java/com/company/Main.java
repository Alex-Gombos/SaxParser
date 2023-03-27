package com.company;

import com.company.SAXParser.WiktionarySaxHandler;
import com.company.SAXParser.WordDictionary;
import com.company.Wikipedia.ArticleCorpus;
import com.company.Wikipedia.ArticleLine;
import com.company.Wikipedia.WikipediaReader;
import javafx.util.Pair;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.util.*;

public class Main {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        // Load Wiktionary into memory

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        WiktionarySaxHandler wiktionarySaxHandler = new WiktionarySaxHandler();


        saxParser.parse("rowiktionary-latest-pages-articles.xml", wiktionarySaxHandler);
        wiktionarySaxHandler.ReadData();


        //Load Wikipedia corpus into memory

        WikipediaReader wikipediaReader = new WikipediaReader();
        ArticleCorpus articleCorpus = new ArticleCorpus();
        wikipediaReader.read("wiki.txt", articleCorpus);


        // Choose words by which to filter the wikipedia articles

        List<String> tokens = new ArrayList<>();
        tokens.add("eritrocit");
        tokens.add("limfocit");
        tokens.add("monocit");
        tokens.add("hematopoeza");

        wikipediaReader.filter(articleCorpus, tokens); // Filter said articles

        File yourFile = new File("filteredText.txt");
        yourFile.createNewFile();
        wikipediaReader.out(yourFile, articleCorpus);   // print filtered articles

        WordDictionary wordDictionary = new WordDictionary();

        File yourFile1 = new File("parseOutPut.csv"); // load the wiktionary words written to the "parseOutPut.csv file" back
        //into memmory. This is useful because the wiktionary dump needs to be parsed only one, and the we can just read from the CSV

        yourFile.createNewFile();

        wordDictionary.createWiktionaryMap(yourFile1); // create a map for which the key is the actual word and the value is its root
        HashMap<String, String> dictionary = wordDictionary.getWiktionaryMap();
        List<List<String>> splitWords;
       // HashMap<String, Pair<List<String>, Integer>> wikipediaWords = new HashMap<>();
        splitWords = wikipediaReader.findWordsInWiktionary(articleCorpus, dictionary); // split articles into words, and change every
        // word to its root (if it exists in the database)

        System.out.println();
        System.out.println("Below is processed Wikipedia text");
        System.out.println();

        for (List<String> item:splitWords){
            for (String item2:item)
                System.out.print(item2 + " ");
            System.out.println();
        }

        //wikipediaReader.countWords(splitWords, wikipediaWords); // find which words occur in more than one article

        HashMap<String, HashSet<Integer>> mapWords = new HashMap<>();
        HashMap<String, Double> mapWordsTFIDF = new HashMap<>();
//        wikipediaReader.countWordsNew(splitWords, mapWords);
//
//        for(String key:mapWords.keySet()){
//            if(mapWords.get(key).size()>1){
//                System.out.println("Word: "+ key + " appears " + mapWords.get(key).size()+ " times");
//            }
//        }
        wikipediaReader.countWordsTFIDF(splitWords, mapWordsTFIDF);
        final boolean DESC = false;
        HashMap<String, Double> sortedTFIDF = (HashMap<String, Double>) wikipediaReader.sortByValue(mapWordsTFIDF, DESC);
        for(String key:sortedTFIDF.keySet()){
            System.out.println("Word " + key + " has a value of: " + sortedTFIDF.get(key));
        }
    }
}
