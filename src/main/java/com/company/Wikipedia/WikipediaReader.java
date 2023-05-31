package com.company.Wikipedia;

import dk.dren.hunspell.Hunspell;
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
            int count = 0;
            for (Article item : arrayList) {
                if(count>=200)
                    break;
                ArrayList<String> vector = item.getStrings();
                for (String item2 : vector) {
                    strToBytes = item2.getBytes();
                    outputStream.write(strToBytes);
                }
                strToBytes = "\n".getBytes();
                outputStream.write(strToBytes);
                outputStream.write(strToBytes);
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ArticleCorpus filter(ArticleCorpus articleCorpus, MATCH mode, Map<Integer, TAG> articleLabel,
                                List<List<String>> tokenList, ArticleCorpus articleCorpusWithoutTrainArticles) {
        ArticleCorpus listWithFilteredArticles = new ArticleCorpus();
        int count = 0;
        for(List<String> tokens:tokenList){
            String patternString = "(?i)(?:" + StringUtils.join(tokens, "|") + ")";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher("");
            int articleIndex = 0;
            for(Article article : articleCorpus.getArticles()){
                boolean foundWord = false;
                for (String line : article.getStrings()) {
                    matcher.reset(line);
                    if (matcher.find()) {
                        foundWord = true;
                        break;
                    }
                }
                if(mode == MATCH.NOTMATCH){
                    if(!foundWord){
                        count++;
                        listWithFilteredArticles.AppendArticle(article);
                        articleCorpusWithoutTrainArticles.RemoveArticle(articleIndex);
                        articleIndex--;
                        if(count>=250)
                            break;
                    }
                }
                else {
                    if(foundWord) {
                        listWithFilteredArticles.AppendArticle(article);
                        articleCorpusWithoutTrainArticles.RemoveArticle(articleIndex);
                        articleIndex--;
                        if(tokenList.indexOf(tokens) == 0){
                            articleLabel.put(listWithFilteredArticles.getArticles().indexOf(article), TAG.OCIT);
                        }
                        else{
                            articleLabel.put(listWithFilteredArticles.getArticles().indexOf(article), TAG.GASTRO);
                        }
                    }
                }
                articleIndex++;
            }
        }
        return listWithFilteredArticles;
    }
    // filter the large corpus based on token words, and return a new Article corpus object with the filtered articles
    public ArticleCorpus filter(ArticleCorpus articleCorpus, List<String> tokensList, MATCH mode) {
        String patternString = "(?i)(?:" + StringUtils.join(tokensList, "|") + ")";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher("");
        ArticleCorpus listWithFilteredArticles = new ArticleCorpus();
        int count = 0 ;
        for (Article article : articleCorpus.getArticles()) {
            boolean foundWord = false;
            for (String line : article.getStrings()) {
                matcher.reset(line);
                if (matcher.find()) {
                    foundWord = true;
                    break;
                }
            }
            if(mode == MATCH.NOTMATCH){
                if(!foundWord){
                    count++;
                    listWithFilteredArticles.AppendArticle(article);
                    if(count>=250)
                        break;
                }
            }
            else {
                if(foundWord)
                    listWithFilteredArticles.AppendArticle(article);
            }
        }
        return listWithFilteredArticles;
    }

    // clean text
    public void prepareText(List<String> articleSentence) {
        for (int i = 0; i < articleSentence.size(); i++) {
            String sPatRemove = "[\\[\\]();:\"„”=→]+";
            String sWord = articleSentence.get(i);
            sWord = sWord.replaceAll(sPatRemove, "");
            sWord = sWord.replaceAll("\\s+", " ");
            sWord = sWord.replace("ţ", "ț").replace("ş", "ș").replace("Ţ", "Ț").replace("Ş", "Ș");
            sWord = sWord.replaceAll("\\u25AA", "");
            sWord = sWord.replaceAll(",(?!\\s)", ", ");
            sWord = sWord.replaceAll("\\.(?!\\s)", ". ");
            articleSentence.set(i, sWord);
        }
    }

    public List<List<String>> addCharactersAsItems(List<List<String>> splitWords) {
        List<List<String>> newSplitWords = new ArrayList<>();
        for (List<String> article : splitWords) {
            List<String> items = new ArrayList<>();
            for (String word : article) {
                int last = word.length() - 1;
                if (last >= 0 && (word.charAt(last) == ',' || word.charAt(last) == '.')) {
                    // If the last character is a comma or period, add it as a separate element
                    items.add(word.substring(0, last));
                    items.add(word.substring(last));
                } else {
                    items.add(word);
                }
            }
            newSplitWords.add(items);
        }
        return newSplitWords;
    }

    public List<List<String>> splitIntoSentence(ArticleCorpus articleCorpus) {
        List<List<String>> split = new ArrayList<>();
        for (Article article : articleCorpus.getArticles()) {
            prepareText(article.getStrings());
            for (String line : article.getStrings()) {
                List<String> sentence = new ArrayList<>(Arrays.asList(line.split(" ")));
                split.add(sentence);
            }
        }
        return addCharactersAsItems(split);
    }

    // tokenize the text into words, List<String> holds all words in an article,
    // and List<List<String>> holds every article in the corpus
    public List<List<String>> splitWords(ArticleCorpus articleCorpus) {
        List<List<String>> splitWords = new ArrayList<>();
        for (Article item : articleCorpus.getArticles()) {
            prepareText(item.getStrings());
            List<String> items = new ArrayList<>();
            for (String item2 : item.getStrings()) {
                items.addAll(Arrays.asList(item2.split(" ")));
            }
            splitWords.add(items);
        }
        //return splitWords;

        return addCharactersAsItems(splitWords);
    }

    // lemmatization
    // create a new list which holds the same words but changes them to their root (if it exists)

    public List<List<String>> findWordsInWiktionary(List<List<String>> articleList, HashMap<String, String> wiktionaryWords) {
        List<String> tempList = new ArrayList<>();
        List<List<String>> newList = new ArrayList<>();
        for (List<String> article : articleList) {
            for (String word : article) {
                tempList.add(wiktionaryWords.getOrDefault(word, word));
                tempList.set(tempList.size()-1, tempList.get(tempList.size()-1).replaceAll("^\\s+", ""));
            }
            newList.add(tempList);
            tempList = new ArrayList<>();
        }
        return newList;
    }

    public String shortestStem(List<String>stems){
        String shortest = stems.get(0);
        for(String word:stems){
            if(word.length()<shortest.length())
                shortest = word;
        }
        return shortest;
    }

    public List<List<String>> stemmedWordsArticleList(ArticleCorpus articleCorpus) throws FileNotFoundException, UnsupportedEncodingException {
        Hunspell hunspell = Hunspell.getInstance();
        Hunspell.Dictionary dictionary = hunspell.getDictionary("ro_RO");
        List<List<String>> articleList = splitWords(articleCorpus);
        List<String> tempArticle = new ArrayList<>();
        List<List<String>> newArticleList = new ArrayList<>();
        List<String>stems = new ArrayList<>();
        tempArticle = new ArrayList<>();
        for(List<String> article:articleList){
            for(String word:article){
                stems = dictionary.stem(word);
                if (stems.size() == 0) {
                    tempArticle.add(word);
                } else {
                    String stem = shortestStem(stems);
                    tempArticle.add(stem);
                }
                tempArticle.set(tempArticle.size()-1, tempArticle.get(tempArticle.size()-1).replaceAll("^\\s+", ""));
            }
            stems.clear();
            newArticleList.add(tempArticle);
            tempArticle = new ArrayList<>();
        }
        return newArticleList;
    }

    // Map each word(String) to a Vector<Integer> to keep in track in what article each word appears and how often
    public Map<String, Vector<Integer>> trackWordFrequencyInArticles(List<List<String>> splitWords) {
        Map<String, Vector<Integer>> mapWords = new TreeMap<>();
        List<String> articleList = new ArrayList<>();
        for (int i=0; i<splitWords.size(); i++) {
            for (String word : splitWords.get(i)) {
                Vector<Integer> vector = new Vector<>();
                if (mapWords.containsKey(word)) {
                    vector = mapWords.get(word);
                }
                vector.add(i);
                mapWords.put(word, vector);
            }
        }
        return mapWords;
    }

    // compute the TFIDF value for each word
    public Map<String, Double> computeTFIDF(List<List<String>> splitWords) {
        // key: word in its base form, value: Vector<Integer> with article index where word appears
        Map<String, Vector<Integer>> mapWords = trackWordFrequencyInArticles(splitWords);
        // matrix to keep track of every word TFIDF in every article
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
                                         Map<String, Double> topicArticlesTFIDF) throws FileNotFoundException, UnsupportedEncodingException {
        int baseArticles = 200;
        ArticleCorpus AllnonTopicArticleCorpus = filter(articleCorpus, tokenList, MATCH.NOTMATCH);
        ArticleCorpus nonTopicArticleCorpus = new ArticleCorpus();

        int upperbound = AllnonTopicArticleCorpus.getArticles().size()-1;
        Random rand = new Random();
        int int_random = rand.nextInt(upperbound);
        while(baseArticles>0){
            nonTopicArticleCorpus.AppendArticle(AllnonTopicArticleCorpus.getArticles().get(int_random));
            baseArticles--;
            int_random = rand.nextInt(upperbound);
        }

        //List<List<String>> splitWordsWiktionary = findWordsInWiktionary(nonTopicArticleCorpus, wiktionaryWords);
        List<List<String>> splitWordsWiktionary = stemmedWordsArticleList(nonTopicArticleCorpus);
        List<List<String>> splitWordsHunandWiktionary = findWordsInWiktionary(splitWordsWiktionary, wiktionaryWords);

        Map<String, Double> nonTopicTFIDF = computeTFIDF(splitWordsHunandWiktionary);
        final boolean DESC = false;
        Map<String, Double> sortedTFIDF = sortByValueMap(nonTopicTFIDF, DESC);


        for (var key : sortedTFIDF.keySet()) {
            topicArticlesTFIDF.remove(key);
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

    public List<String> nonMedicalWords(ArticleCorpus articleCorpus){
        List<List<String>> splitWords = splitWords(articleCorpus);
        List<String> nonMedicalWordsList = new ArrayList<>();
        int count = 200;
        for(List<String> article:splitWords){
            if (count <= 0)
                break;
            nonMedicalWordsList.addAll(article);
            for(String word:article){
                System.out.println(word);
            }
            count--;
        }
        return nonMedicalWordsList;
    }

    public void trimText(){
        String inputFile = "nonTrainArticles.txt"; // Path to the input file
        String outputFile = "nonTrainArticles2.txt"; // Path to the output file
        int maxWordsPerParagraph = 250;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            String line;
            int wordCount = 0;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    writer.newLine(); // Preserve empty lines
                    wordCount = 0;
                } else {
                    String[] words = line.split("\\s+");
                    if (wordCount + words.length <= maxWordsPerParagraph) {
                        writer.write(line);
                        writer.newLine();
                        wordCount += words.length;
                    } else {
                        int wordsToAdd = maxWordsPerParagraph - wordCount;
                        for (int i = 0; i < wordsToAdd; i++) {
                            writer.write(words[i]);
                            if (i < wordsToAdd - 1) {
                                writer.write(" ");
                            }
                        }
                        writer.newLine();
                        wordCount = maxWordsPerParagraph;
                    }
                }
            }

            reader.close();
            writer.close();

            System.out.println("Paragraph trimming completed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArticleCorpus getRandomArticles(int numberOfArticles, ArticleCorpus articleCorpus){
        ArticleCorpus randomArticlesCorpus = new ArticleCorpus();

        int upperbound = articleCorpus.getArticles().size()-1;
        Random rand = new Random();
        int int_random = rand.nextInt(upperbound);
        while(numberOfArticles >0){
            randomArticlesCorpus.AppendArticle(articleCorpus.getArticles().get(int_random));
            numberOfArticles--;
            int_random = rand.nextInt(upperbound);
        }

        return randomArticlesCorpus;
    }
}
