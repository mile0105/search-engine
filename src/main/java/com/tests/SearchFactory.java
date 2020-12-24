package com.tests;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

public class SearchFactory {

    private static String LUCENE_INDEX_PATH = "D:\\Projects\\Intelligent Information Retrieval\\index\\lucene";
    private static String CUSTOM_INDEX_PATH = "D:\\Projects\\Intelligent Information Retrieval\\index\\custom";

    public void searchLucene(String input) throws IOException, ParseException {
        System.out.println("Results for ' " + input + " ': ");

        IndexSearcher searcher = new IndexSearcher(FSDirectory.open(new File(LUCENE_INDEX_PATH)));
        Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_34);

        QueryParser queryParser = new QueryParser(Version.LUCENE_34, "content", standardAnalyzer);
        QueryParser fileParser = new QueryParser(Version.LUCENE_34, "fullpath", standardAnalyzer);

        Query query = queryParser.parse(input);
        Query queryFile = fileParser.parse(input);
        TopDocs hits = searcher.search(query, 100);
        ScoreDoc[] document = hits.scoreDocs;

        System.out.println("Number of hits in the file content: " + hits.totalHits);
        for (int i = 0; i < document.length; i++) {
            Document doc = searcher.doc(document[i].doc);
            String filePath = doc.get("fullpath");
            System.out.println(filePath);
        }

        TopDocs fileNameHits = searcher.search(queryFile, 100);
        ScoreDoc[] fileNameScores = fileNameHits.scoreDocs;
        System.out.println("Number of hits in the file name: " + fileNameHits.totalHits);

        for (int i = 0; i < fileNameScores.length; i++) {
            Document doc = searcher.doc(fileNameScores[i].doc);
            String filename = doc.get("filename");
            System.out.println(filename);
        }
    }

}
