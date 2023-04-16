package com.company;

import com.company.DataSet.CreateJson;
import com.company.SAXParser.WiktionarySaxHandler;
import com.company.SAXParser.WordDictionary;
import com.company.Wikipedia.ArticleCorpus;
import com.company.Wikipedia.MATCH;
import com.company.Wikipedia.WikipediaReader;
import org.json.JSONArray;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
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
        tokens.add("gastric");
        //gastric, gastrina, pilor, colecist, colecistochinin, duoden, hepatic
        ArticleCorpus listWithFilteredArticles = new ArticleCorpus();

        String match = "match";
        String notMatch = "notMatch";

        // Filter said articles
        listWithFilteredArticles = wikipediaReader.filter(articleCorpus, tokens, MATCH.MATCH);

        File yourFile = new File("filteredText.txt");
        if(yourFile.createNewFile()){
            System.out.println("Created file");
        }

        WordDictionary wordDictionary = new WordDictionary();

        // load the wiktionary words written to the "parseOutPut.csv file" back into memmory.
        // This is useful because the wiktionary dump needs to be parsed only one, and the we can just read from the CSV
        File yourFile1 = new File("parseOutPut.csv");

        if(yourFile1.createNewFile()){
            System.out.println("Created file");
        }

        wordDictionary.createWiktionaryMap(yourFile1); // create a map for which the key is the actual word and the value is its root
        HashMap<String, String> dictionary = wordDictionary.getWiktionaryMap();
        List<List<String>> splitWords;

        // split articles into words, and change every
        // word to its root (if it exists in the database)
        splitWords = wikipediaReader.findWordsInWiktionary(listWithFilteredArticles, dictionary);

        Map<String, Double> mapWordsTFIDF = wikipediaReader.computeTFIDF(splitWords);

        final boolean DESC = false;
        Map<String, Double> sortedTFIDF = wikipediaReader.sortByValueMap(mapWordsTFIDF, DESC);
        for(String key:sortedTFIDF.keySet()){
            System.out.println("Word " + key + " has a value of: " + sortedTFIDF.get(key));
        }
        System.out.println(splitWords.size());

        wikipediaReader.sampleNonRelatedArticles(articleCorpus, tokens, dictionary, sortedTFIDF);
        wikipediaReader.writeToFile(sortedTFIDF, "tfidf.txt");

        CreateJson createJson = new CreateJson();
        JSONArray dataSet =  createJson.createListofWords(splitWords, sortedTFIDF);
        createJson.writeToFile(dataSet);
    }
}
