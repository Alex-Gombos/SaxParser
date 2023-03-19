package com.company;

import com.company.Wikipedia.Article;
import com.company.Wikipedia.WikipediaReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
//        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
//        SAXParser saxParser = saxParserFactory.newSAXParser();
//        WiktionarySaxHandler wiktionarySaxHandler = new WiktionarySaxHandler();
//
//        saxParser.parse("rowiktionary-latest-pages-articles.part-1.xml", wiktionarySaxHandler);
//
//        wiktionarySaxHandler.ReadData();
        WikipediaReader wikipediaReader = new WikipediaReader();
        Article article = new Article();
        wikipediaReader.read("wiki.txt", article);
//        File yourFile = new File("score.txt");
//        yourFile.createNewFile();
//        wikipediaReader.out(yourFile, articleObj);

//        ArrayList<ArticleWord> arrayList = articleObj.getList();
//        arrayList.get(0).print();


        List<String> tokens = new ArrayList<String>();
        tokens.add("eritrocit");
        tokens.add("limfocit");
        tokens.add("monocit");
        tokens.add("hematopoeza");

        wikipediaReader.filter(article, tokens);
        File yourFile = new File("score.txt");
        yourFile.createNewFile();
        wikipediaReader.out(yourFile, article);

    }
}
