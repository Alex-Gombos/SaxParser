package com.company.Wikipedia;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WikipediaReader {

    // calculate log2 N indirectly
    // using log() method
    public static double log2(double N) {
        return (Math.log(N) / Math.log(2));
    }

    // load the corpus into memory; Corpus is stored in object ArticleCorpus
    public void read(String uri, ArticleCorpus articleCorpus) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(uri);
             Scanner sc = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            Article article = new Article();
            //read corpus line by line; each article is stored in a Article object
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.isEmpty()) {
                    // add read line to a article
                    article.AddWord(line);
                } else {
                    // add article to corpus Obect
                    articleCorpus.AppendArticle(article);
                    article = new Article();
                }

            }
            if (article.getStrings().size() > 0) {
                articleCorpus.AppendArticle(article);
            }
            if (sc.ioException() != null) {
                try {
                    throw sc.ioException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // print every article stored in memory
    public void out(File file, ArticleCorpus articleCorpus) {
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
            ArrayList<Article> arrayList = articleCorpus.getArticles();
            byte[] strToBytes;
            for (Article item : arrayList) {
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

    // filter the large corpus based on token words, and return a new Article corpus object with the filtered articles
    public ArticleCorpus filter(ArticleCorpus articleCorpus, List<String> tokensList, String mode) {
        String patternString = "(?i)(?:" + StringUtils.join(tokensList, "|") + ")";
        Pattern pattern = Pattern.compile(patternString);
        ArticleCorpus listWithFilteredArticles = new ArticleCorpus();
        for (Article item : articleCorpus.getArticles()) {
            for (String item2 : item.getStrings()) {
                Matcher matcher = pattern.matcher(item2);
                if(Objects.equals(mode, "match")) {
                    if (matcher.find()) {
                        listWithFilteredArticles.AppendArticle(item);
                        break;
                    }
                }
                else {
                    if (!matcher.find()) {
                        listWithFilteredArticles.AppendArticle(item);
                        break;
                    }
                }
            }
        }
        return listWithFilteredArticles;
    }

    // clean text
    public void prepareText(List<String> articleSentence) {
        for (int i = 0; i < articleSentence.size(); i++) {
            String sPatRemove = "[\\[\\]()\\.;:\"„,=→]+";
            String sText = articleSentence.get(i);
            sText = sText.replaceAll(sPatRemove, "");
            sText = sText.replaceAll("\\s+", " ");
            articleSentence.set(i, sText);
        }
    }

    // tokenize the text into words, List<String> holds all words in an article,
    // and List<List<String>> holds every article in the corpus
    public List<List<String>> splitWords(ArticleCorpus articleCorpus) {
        List<List<String>> splitWords = new ArrayList<>();
        for (Article item : articleCorpus.getArticles()) {
            prepareText(item.getStrings());
            List<String> items = new ArrayList<>();
            for (String item2 : item.getStrings()) {
                //Arrays.stream(item2.split(" ")).forEach(x->items.add(x));
                items.addAll(Arrays.asList(item2.split(" ")));
            }
            splitWords.add(items);
        }
        return splitWords;
    }

    // lemmatization
    // create a new list which holds the same words but changes them to their root (if it exists)
    public List<List<String>> findWordsInWiktionary(ArticleCorpus articleCorpus, HashMap<String, String> wiktionaryWords) {
        List<String> tempList = new ArrayList<>();
        List<List<String>> list = splitWords(articleCorpus);
        List<List<String>> newList = new ArrayList<>();
        for (List<String> item : list) {
            for (String item1 : item) {
                tempList.add(wiktionaryWords.getOrDefault(item1, item1));
                tempList.set(tempList.size()-1, tempList.get(tempList.size()-1).replaceAll("^\\s+", ""));
            }
            newList.add(tempList);
            tempList = new ArrayList<>();
        }

        return newList;
    }

    // Map each word(String) to a Vector<Integer> to keep in track in what article each word appears and how often
    public Map<String, Vector<Integer>> trackWordFrequencyInArticles(List<List<String>> splitWords) {
        Map<String, Vector<Integer>> mapWords = new TreeMap<>();
        for (List<String> strings : splitWords) {
            for (String item2 : strings) {
                Vector<Integer> vector = new Vector<>();
                if (mapWords.containsKey(item2)) {
                    vector = mapWords.get(item2);
                }
                vector.add(splitWords.indexOf(strings));
                mapWords.put(item2, vector);
            }
        }
        return mapWords;
    }

    // compute the TFIDF value for each word
    public Map<String, Double> computeTFIDF(List<List<String>> splitWords) {
        // key: word in its base form, value: Vector<Integer> with article index where word appears
        Map<String, Vector<Integer>> mapWords = trackWordFrequencyInArticles(splitWords);
        // matrix to keep track of every words TFIDF in every article
        List<List<Double>> tfidf = new ArrayList<>();
        // mapOccurrence stores how many times a word appears in which article
        for (Map.Entry<String, Vector<Integer>> entry : mapWords.entrySet()) {
            Map<Integer, Long> mapOccurrence = entry.getValue().stream()
                    .collect(Collectors.groupingBy(
                            Function.identity(),
                            TreeMap::new,
                            Collectors.mapping(Function.identity(), Collectors.counting())));
            // compute term-frequency for each words in every article
            List<Double> tf = mapOccurrence.entrySet().stream()
                    .map(en -> (double) en.getValue() / splitWords.get(en.getKey()).size()).collect(Collectors.toList());
            double idf = log2((double) splitWords.size() / mapOccurrence.size());
            tfidf.add(tf.stream().map(tfValue -> tfValue * idf).collect(Collectors.toList()));
        }

        // choose maximum TFIDF for each word(if it appears in more than one article)
        int i = 0;
        Map<String, Double> hashMapTFIDF = new HashMap<>();
        for (var entrySet : mapWords.entrySet()) {
            hashMapTFIDF.put(entrySet.getKey(),
                    tfidf.get(i).stream().mapToDouble(v -> v).max().orElseThrow(NoSuchElementException::new));
            i++;
        }

        return hashMapTFIDF;
    }

    public Map<String, Double> sortByValueMap(Map<String, Double> unsortMap, final boolean order) {
        List<Map.Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));

    }

    // sample 50 arbitrary articles that DO NOT contain any of the words selected for filtering
    // Compute a TDF-like parameter
    // remove words with high TDF
    public void sampleNonRelatedArticles(ArticleCorpus articleCorpus,
                                                 List<String> tokenList,
                                                 HashMap<String, String> wiktionaryWords,
                                         Map<String, Double> topicArticlesTFIDF){
        int baseArticles = 50;
        ArticleCorpus AllnonTopicArticleCorpus = filter(articleCorpus, tokenList, "notMatch");
        ArticleCorpus nonTopicArticleCorpus = new ArticleCorpus();

        int upperbound = AllnonTopicArticleCorpus.getArticles().size()-1;
        Random rand = new Random();
        int int_random = rand.nextInt(upperbound);
        while(baseArticles>0){
            nonTopicArticleCorpus.AppendArticle(AllnonTopicArticleCorpus.getArticles().get(int_random));
            baseArticles--;
            int_random = rand.nextInt(upperbound);
        }

        List<List<String>> splitWordsWiktionary = findWordsInWiktionary(nonTopicArticleCorpus, wiktionaryWords);

        Map<String, Double> nonTopicTFIDF = computeTFIDF(splitWordsWiktionary);
        final boolean DESC = false;
        Map<String, Double> sortedTFIDF = sortByValueMap(nonTopicTFIDF, DESC);


        for (var entrySet : sortedTFIDF.entrySet()) {
            topicArticlesTFIDF.remove(entrySet.getKey());
        }
    }

    public void writeToFile(Map<String, Double> topicArticles, final String outputFilePath) throws IOException {
        File file = new File(outputFilePath);

        BufferedWriter bf;
        bf = new BufferedWriter(new FileWriter(file));

        for (Map.Entry<String, Double> entry :
                topicArticles.entrySet()) {
            bf.write(entry.getKey() + ":" + entry.getValue());
            bf.newLine();
        }
        bf.flush();
    }
}
