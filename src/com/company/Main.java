package com.company;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        WiktionarySaxHandler wiktionarySaxHandler = new WiktionarySaxHandler();

        saxParser.parse("rowiktionary-latest-pages-articles.part-1.xml", wiktionarySaxHandler);

        wiktionarySaxHandler.ReadData();
    }
}
