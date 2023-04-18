package com.company;

import com.company.DataSet.CreateJson;
import com.company.SAXParser.WiktionarySaxHandler;
import com.company.SAXParser.WordDictionary;
import com.company.Wikipedia.ArticleCorpus;
import com.company.Wikipedia.MATCH;
import com.company.Wikipedia.TAG;
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
        List<String> tokens1 = new ArrayList<>();
        List<String> tokens2 = new ArrayList<>();

        tokens1.add("eritrocit");
        tokens1.add("limfocit");
        tokens1.add("monocit");
        tokens1.add("hematopoeza");

        tokens2.add("gastric");
        tokens2.add("gastrina");
        //tokens2.add("pilor");
        tokens2.add("colecist");
        tokens2.add("colecistochinin");
        //tokens2.add("hepatic");
        tokens2.add("duoden");

        tokens.addAll(tokens1);
        tokens.addAll(tokens2);

        List<List<String>> tokenList = new ArrayList<>();
        tokenList.add(tokens1);
        tokenList.add(tokens2);

        ArticleCorpus listWithFilteredArticles = new ArticleCorpus();

        // Filter said articles
        Map<Integer, TAG> articleLabel = new HashMap<>();
        //listWithFilteredArticles = wikipediaReader.filter(articleCorpus, tokens, MATCH.MATCH);
        listWithFilteredArticles = wikipediaReader.filter(articleCorpus, MATCH.MATCH, articleLabel, tokenList);

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
        List<List<String>> splitWords = wikipediaReader.splitWords(listWithFilteredArticles);

        // split articles into words, and change every
        // word to its root (if it exists in the database)
        //splitWords = wikipediaReader.findWordsInWiktionary(listWithFilteredArticles, dictionary);

        List<List<String>> stemmedWordsHun = wikipediaReader.stemmedWordsArticleList(listWithFilteredArticles);
        Map<String, Double> mapWordsTFIDF = wikipediaReader.computeTFIDF(stemmedWordsHun);

        final boolean DESC = false;
        Map<String, Double> sortedTFIDF = wikipediaReader.sortByValueMap(mapWordsTFIDF, DESC);
        for(String key:sortedTFIDF.keySet()){
            System.out.println("Word " + key + " has a value of: " + sortedTFIDF.get(key));
        }
        System.out.println(stemmedWordsHun.size());

        wikipediaReader.sampleNonRelatedArticles(articleCorpus, tokens, dictionary, sortedTFIDF);
        wikipediaReader.writeToFile(sortedTFIDF, "tfidfHun.txt");

        CreateJson createJson = new CreateJson();
        JSONArray dataSet =  createJson.createListofWords(stemmedWordsHun, sortedTFIDF, splitWords, articleLabel);
        createJson.writeToFile(dataSet);
    }
}
