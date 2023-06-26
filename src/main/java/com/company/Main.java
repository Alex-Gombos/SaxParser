package com.company;

import com.company.DataSet.CreateJson;
import com.company.SAXParser.WiktionarySaxHandler;
import com.company.SAXParser.WordDictionary;
import com.company.Wikipedia.ArticleCorpus;
import com.company.Wikipedia.MATCH;
import com.company.Wikipedia.TAG;
import com.company.Wikipedia.Dataset;
import org.json.JSONArray;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        // Load Wiktionary into memory
        // TODO REFACTORING

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        WiktionarySaxHandler wiktionarySaxHandler = new WiktionarySaxHandler();


        saxParser.parse("rowiktionary-latest-pages-articles.xml", wiktionarySaxHandler);
        wiktionarySaxHandler.ReadData();


        //Load Wikipedia corpus into memory

        Dataset dataset = new Dataset();
        ArticleCorpus articleCorpus = new ArticleCorpus();
        ArticleCorpus articleCorpusWithoutTrainArticles = new ArticleCorpus();
        dataset.read("wiki.txt", articleCorpus);
        dataset.read("wiki.txt", articleCorpusWithoutTrainArticles);


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

        List<String> nonMedicalWords = new ArrayList<>();
        nonMedicalWords.add("ministru");
        nonMedicalWords.add("miniștri");
        nonMedicalWords.add("wrestler");
        nonMedicalWords.add("bodyguard");




        ArticleCorpus listWithFilteredArticles = new ArticleCorpus();

        // Filter said articles
        Map<Integer, TAG> articleLabel = new HashMap<>();
        listWithFilteredArticles = dataset.filter(articleCorpus, MATCH.MATCH, articleLabel, tokenList, articleCorpusWithoutTrainArticles);

        System.out.println("non related articles");
        File outputNonTranArticles = new File("nonTrainArticles.txt");
        System.out.println(articleCorpusWithoutTrainArticles.getArticles().size());
        System.out.println(articleCorpus.getArticles().size());
        ArticleCorpus medicalArticleCorpusWithoutTrainArticles = new ArticleCorpus();
        medicalArticleCorpusWithoutTrainArticles = dataset.filter(articleCorpusWithoutTrainArticles, tokens ,MATCH.MATCH);
        dataset.out(outputNonTranArticles, dataset.getRandomArticles(200, medicalArticleCorpusWithoutTrainArticles));
        dataset.trimText();

        File yourFile = new File("filteredText.txt");
        if (yourFile.createNewFile()) {
            System.out.println("Created file");
        }

        WordDictionary wordDictionary = new WordDictionary();

        // load the wiktionary words written to the "parseOutPut.csv file" back into memory.
        // This is useful because the wiktionary dump needs to be parsed only one, and then we can just read from the CSV
        File yourFile1 = new File("parseOutPut.csv");

        if (yourFile1.createNewFile()) {
            System.out.println("Created file");
        }
        wordDictionary.createWiktionaryMap(yourFile1); // create a map for which the key is the actual word and the value is its root
        HashMap<String, String> dictionary = wordDictionary.getWiktionaryMap();
        List<List<String>> splitWords = dataset.splitIntoSentence(listWithFilteredArticles);

        // split articles into words, and change every
        // word to its root (if it exists in the database)
        //splitWords = wikipediaReader.findWordsInWiktionary(listWithFilteredArticles, dictionary);

        List<List<String>> stemmedWordsHun = dataset.stemmedWordsArticleList(listWithFilteredArticles);
        List<List<String>> stemmedWordsHunandWiktionary = dataset.findWordsInWiktionary(stemmedWordsHun, dictionary);

        Map<String, Double> mapWordsTFIDF = dataset.computeTFIDF(stemmedWordsHunandWiktionary);

        final boolean DESC = false;
        Map<String, Double> sortedTFIDF = dataset.sortByValueMap(mapWordsTFIDF, DESC);
        System.out.println(stemmedWordsHunandWiktionary.size());

        dataset.sampleNonRelatedArticles(articleCorpus, tokens, dictionary, sortedTFIDF);
        dataset.writeToFile(sortedTFIDF, "tfidfHun.txt");
        System.out.println("inainte de primul nonMedicalWPrds");
        List<String> nonMedicalWordsList = dataset.nonMedicalWords(dataset.filter(articleCorpusWithoutTrainArticles, nonMedicalWords, MATCH.MATCH));
        System.out.println("dupa de primul nonMedicalWPrds");

        System.out.println("inainte de al doilea nonMedicalWPrds");
        nonMedicalWordsList.addAll(dataset.nonMedicalWords(dataset.filter(articleCorpusWithoutTrainArticles, nonMedicalWords, MATCH.NOTMATCH)));
        System.out.println("dupa de al doilea  nonMedicalWPrds");

        CreateJson createJson = new CreateJson();
        JSONArray dataSet = createJson.createListofWordsSentence(stemmedWordsHunandWiktionary, sortedTFIDF, splitWords, articleLabel, nonMedicalWordsList);
        createJson.writeToFile(dataSet);

        dataset.wordsNotInExcel(stemmedWordsHunandWiktionary,splitWords);

        String inputFilePath = "tfidfHun.txt";
        String outputFilePath = "output_file.csv";
        double threshold = 0.03; // set your threshold here

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             FileWriter writer = new FileWriter(outputFilePath)) {

            writer.append("Words\n");

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                String word = parts[0];
                double value = Double.parseDouble(parts[1]);
                if (value > threshold) {
                    writer.append(word).append("\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
