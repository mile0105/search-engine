package com.tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class CustomIndexFactory {

    private static String FILES_PATH = "D:\\Projects\\Intelligent Information Retrieval\\files\\";
    private static String CUSTOM_INDEX_PATH = "D:\\Projects\\Intelligent Information Retrieval\\index\\custom\\index.json";

    private List<String> stopwords = Arrays.asList("a", "able", "about",
            "across", "after", "all", "almost", "also", "am", "among", "an",
            "and", "any", "are", "as", "at", "be", "because", "been", "but",
            "by", "can", "cannot", "could", "dear", "did", "do", "does",
            "either", "else", "ever", "every", "for", "from", "get", "got",
            "had", "has", "have", "he", "her", "hers", "him", "his", "how",
            "however", "i", "if", "in", "into", "is", "it", "its", "just",
            "least", "let", "like", "likely", "may", "me", "might", "most",
            "must", "my", "neither", "no", "nor", "not", "of", "off", "often",
            "on", "only", "or", "other", "our", "own", "rather", "said", "say",
            "says", "she", "should", "since", "so", "some", "than", "that",
            "the", "their", "them", "then", "there", "these", "they", "this",
            "tis", "to", "too", "twas", "us", "wants", "was", "we", "were",
            "what", "when", "where", "which", "while", "who", "whom", "why",
            "will", "with", "would", "yet", "you", "your");

    private Map<String, List<FileNumberPositionPair>> index = new HashMap<>();
    private List<String> files = new ArrayList<>();


    public void createOrFetchIndex() throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File indexFile = new File(CUSTOM_INDEX_PATH);
            index = mapper.readValue(indexFile, new TypeReference<Map<String, List<FileNumberPositionPair>>>() {
            });
            files = Arrays.stream(new File(FILES_PATH).listFiles()).map(File::getName).collect(Collectors.toList());

            System.out.println("Index fetched");

        } catch (IOException e) {
            e.printStackTrace();
//            indexFiles();
//            serializeIndex();
        }
    }

    public List<String> search(String input) {
        if (input == null) {
            return Collections.emptyList();
        }
        String[] words = input.split("\\W+");

        if (words.length == 0) {
            return Collections.emptyList();
        }
        List<String> results = new ArrayList<>();

        Map<String, Map<Integer, List<Integer>>> occurencesPerWord = new LinkedHashMap<>();

        Map<String, Integer> result = new HashMap<>();

        for (String word : words) {

            String optimizedWord = word.toLowerCase();
            if (stopwords.contains(optimizedWord)) {
                continue;
            }

            List<FileNumberPositionPair> wordIndex = index.get(optimizedWord);

            if (wordIndex == null) {
                // some word is not found somewhere
                return Collections.emptyList();
            }

            for (FileNumberPositionPair pairs : wordIndex) {
                occurencesPerWord.computeIfAbsent(word, map -> new HashMap<>())
                        .computeIfAbsent(pairs.fileNumber, empty -> new ArrayList<>()).add(pairs.filePosition);
            }
        }

        List<Map<Integer, List<Integer>>> values = new ArrayList<>(occurencesPerWord.values());

        Set<Integer> finalInterSection = values.get(0).keySet();

        for (int i = 1; i < values.size(); i++) {

            Set<Integer> currentList = values.get(i).keySet();
            finalInterSection = getIntersectionBetweenTwoLists(finalInterSection, currentList);
        }

        Map<String, Map<Integer, List<Integer>>> occurencesPerWordResult = new LinkedHashMap<>();

        for (Map.Entry<String, Map<Integer, List<Integer>>> entry : occurencesPerWord.entrySet()) {
            for (Integer integer : entry.getValue().keySet()) {
                if (finalInterSection.contains(integer)) {
                    List<Integer> positions = entry.getValue().get(integer);
                    occurencesPerWordResult.computeIfAbsent(entry.getKey(), key -> new HashMap<>()).put(integer, positions);
                }
            }
        }

        if (words.length > 1) {

            int firstNonStopWordIndex = 0;
            while (firstNonStopWordIndex < words.length && stopwords.contains(words[firstNonStopWordIndex].toLowerCase())) {
                firstNonStopWordIndex++;
            }
            if (firstNonStopWordIndex == words.length) {
                return Collections.emptyList();
            }

            for (Integer fileIndex : finalInterSection) {
                int occurences = 0;
                String firstWord = words[firstNonStopWordIndex].toLowerCase();
                List<Integer> firstWordPositionsInFile = occurencesPerWordResult.get(firstWord).get(fileIndex);
                for (int position : firstWordPositionsInFile) {
                    int j;
                    for (j = firstNonStopWordIndex + 1; j < words.length; j++) {
                        String optimizedWord = words[j].toLowerCase();
                        if (stopwords.contains(optimizedWord)) {
                            position++;
                            continue;
                        }
                        List<Integer> wordPositionsInFile = occurencesPerWord.get(optimizedWord).get(fileIndex);
                        if (!wordPositionsInFile.contains(++position)) {
                            break;
                        }
                    }

                    if (j == words.length) {
                        occurences++;
                        // phrase exists in given file
                    }

                }
                if (occurences > 0) {
                    String fileName = files.get(fileIndex);
                    result.put(fileName, occurences);
                }

            }

        } else {

            String optimizedWord = words[0].toLowerCase();

            Map<Integer, List<Integer>> filesMap = occurencesPerWordResult.get(optimizedWord);

            for (Map.Entry<Integer, List<Integer>> file : filesMap.entrySet()) {
                String fileName = files.get(file.getKey());
                int sum = file.getValue().size();
                result.put(fileName, sum);
            }


        }
        Map<String, Integer> sorted = result.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        for (Map.Entry<String, Integer> entry : sorted.entrySet()) {
            String fileName = entry.getKey();
            int entryValue = entry.getValue();
            String hit = entryValue == 1 ? "hit" : "hits";
            results.add(fileName + ":    " + entryValue + "    " + hit);
        }

        return results;

    }


    private void indexFiles() throws IOException {
        File[] filesToIndex = new File(FILES_PATH).listFiles();
        System.out.println("Indexing " + filesToIndex.length + " files.....");
        for (File file : filesToIndex) {
            System.out.println("INDEXING FILE " + file.getAbsolutePath() + "......");
            indexTextFile(file);
            System.out.println("INDEXED FILE " + file.getAbsolutePath() + "");
        }
        System.out.println("Finished indexing");
    }

    private void indexTextFile(File file) throws IOException {
        int fileNumber = files.indexOf(file.getName());

        if (fileNumber == -1) {
            files.add(file.getPath());
            fileNumber = files.size() - 1;
        }

        PDFParser pdfParser = new PDFParser();

        String[] wordsInFile = pdfParser.parsePdf(file).split("\\W+");

        int filePosition = 0;

        for (String word : wordsInFile) {
            String optimizedWord = word.toLowerCase();
            filePosition++;
            if (stopwords.contains(optimizedWord)) {
                continue;
            }
            List<FileNumberPositionPair> wordIndex = index.computeIfAbsent(optimizedWord, k -> new LinkedList<>());

            wordIndex.add(new FileNumberPositionPair(fileNumber, filePosition));
        }
    }

    private static class FileNumberPositionPair {

        private int fileNumber;
        private int filePosition;

        public FileNumberPositionPair() {

        }

        public FileNumberPositionPair(int fileNumber, int filePosition) {
            this.fileNumber = fileNumber;
            this.filePosition = filePosition;
        }

        public int getFileNumber() {
            return fileNumber;
        }

        public int getFilePosition() {
            return filePosition;
        }

        public void setFileNumber(int fileNumber) {
            this.fileNumber = fileNumber;
        }

        public void setFilePosition(int filePosition) {
            this.filePosition = filePosition;
        }
    }

    private void serializeIndex() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File indexFile = new File(CUSTOM_INDEX_PATH);
        mapper.writeValue(indexFile, index);
    }

    private Set<Integer> getIntersectionBetweenTwoLists(Set<Integer> list1, Set<Integer> list2) {

        Set<Integer> result = new LinkedHashSet<>();
        for (Integer integer : list1) {
            if (list2.contains(integer)) {
                result.add(integer);
            }
        }
        return result;
    }

}
