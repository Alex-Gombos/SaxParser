package com.company.Wikipedia;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class WikipediaReader {
    FileInputStream inputStream = null;
    FileOutputStream outputStream = null;
    Scanner sc = null;

    public void read(String uri, Article article) throws IOException {
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
                    article.AppendArticle(articleLine);
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

    public void out(File file, Article article) {
        try {
            outputStream = new FileOutputStream(file);
            ArrayList<ArticleLine> arrayList = article.getList();
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

    public void filter(Article article, List<String> arrayList){
        String patternString = "\\b(" + StringUtils.join(arrayList, "|") + ")\\b";
        Pattern pattern = Pattern.compile(patternString);
        List<ArticleLine> removeList = new ArrayList<>();
        for(ArticleLine item: article.getList()) {
            boolean matchFound = false;
            for(String item2: item.getStrings()) {
                Matcher matcher = pattern.matcher(item2);
                if(matcher.find())
                    matchFound = true;
            }
            if(!matchFound){
                removeList.add(item);
            }
        }
        article.getList().removeAll(removeList);
    }

}