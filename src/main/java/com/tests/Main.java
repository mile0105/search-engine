package com.tests;

import org.apache.lucene.queryParser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class Main {

    private static JTextField searchBar = new JTextField(30);
    private static JButton searchButton = new JButton("Search");
    private static JList jList = new JList();
    private static JScrollPane scrollPane = new JScrollPane(jList);

    public static void main(String[] args) {

        final CustomIndexFactory factory;
        try {
            factory = custom();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        JFrame frame = new JFrame("Search your query");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(600, 600);

        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(searchBar);
        frame.getContentPane().add(searchButton);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        frame.getContentPane().add(scrollPane);

        frame.setVisible(true);

        searchButton.addActionListener(actionEvent -> {
            String query = searchBar.getText();
            List<String> results = factory.search(query);
            jList.setListData(results.toArray());
//            jList = new JList(results.toArray());
//            frame.getContentPane().remove(scrollPane);
//            scrollPane = new JScrollPane(jList);

//            scrollPane.setLayout(new ScrollPaneLayout());
//            frame.getContentPane().add(scrollPane);
            frame.setVisible(true);
        });

    }

    private static void lucene(BufferedReader reader) throws IOException, ParseException {
        LuceneIndexFactory indexFactory = new LuceneIndexFactory();
        indexFactory.createIndex();


        SearchFactory searchFactory = new SearchFactory();

        while (true) {

            String input = reader.readLine();
            searchFactory.searchLucene(input);

        }
    }

    private static CustomIndexFactory custom() throws IOException {

        CustomIndexFactory indexFactory = new CustomIndexFactory();
        indexFactory.createOrFetchIndex();
        return indexFactory;
    }


}
