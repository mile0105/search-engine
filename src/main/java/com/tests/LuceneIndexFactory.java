package com.tests;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

public class LuceneIndexFactory {

    private static IndexWriter indexWriter = null;

    private static String INDEX_PATH = "D:\\Projects\\Intelligent Information Retrieval\\index\\lucene";
    private static String FILES_PATH = "D:\\Projects\\Intelligent Information Retrieval\\files\\";

    public void createIndex() throws IOException {
        createIndexWriter();
        indexFiles();
        closeIndexWriter();
    }

    private void createIndexWriter() {
        try {
            File indexDirectory = new File(INDEX_PATH);
            FSDirectory dir = FSDirectory.open(indexDirectory);
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_34);
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_34, analyzer);
            indexWriter = new IndexWriter(dir, config);
        } catch (Exception ex) {
            System.out.println("Sorry cannot get the index writer");
        }
    }

    private void indexFiles() throws IOException {
        File[] filesToIndex = new File(FILES_PATH).listFiles();
        for (File file : filesToIndex) {
            System.out.println("INDEXING FILE " + file.getAbsolutePath() + "......");
            indexTextFile(file);
            System.out.println("INDEXED FILE " + file.getAbsolutePath() + "");
        }
    }

    private void indexTextFile(File file) throws CorruptIndexException, IOException {
        Document doc = new Document();

        PDFParser parser = new PDFParser();
        String content = parser.parsePdf(file);

        doc.add(new Field("content", content,
                Field.Store.YES, Field.Index.ANALYZED,
                Field.TermVector.WITH_POSITIONS_OFFSETS));
        doc.add(new Field("filename", file.getName(),
                Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("fullpath", file.getAbsolutePath(),
                Field.Store.YES, Field.Index.ANALYZED));

        System.out.println("Indexed" + file.getAbsolutePath());
        indexWriter.addDocument(doc);
    }

    /**
     * Closes the IndexWriter
     */
    private void closeIndexWriter() {
        try {
            indexWriter.optimize();
            indexWriter.close();
        } catch (Exception e) {
            System.out.println("Indexer Cannot be closed");
        }
    }

}
