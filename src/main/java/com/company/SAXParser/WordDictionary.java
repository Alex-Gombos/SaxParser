package com.company.SAXParser;

import java.io.*;
import java.util.*;

public class WordDictionary {
    HashMap<String, String> dictionary = new HashMap<>();
    List<List<String>> records = new ArrayList<>();

    public void createWiktionaryMap(File file){
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String,String> getWiktionaryMap() {
        for (List<String> list : records) {
            if(list.size()>=2)
                dictionary.put(list.get(0), list.get(1));
        }
        return dictionary;
    }
}
