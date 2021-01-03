package com.tests;

import org.apache.lucene.queryParser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static JTextField searchBar = new JTextField(30);
    private static JButton searchButton = new JButton("Search");
    private static JList<SearchResult> jList = new JList<>();
    private static JScrollPane scrollPane = new JScrollPane(jList);

    private static JPanel panel = new JPanel(new BorderLayout());
    private static JLabel positionsLabel = new JLabel();

    private static SearchResult[] arrayResult = new SearchResult[0];

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
        panel.setPreferredSize(new Dimension(600, 400));
        frame.getContentPane().add(panel);

        frame.setVisible(true);


        searchButton.addActionListener(actionEvent -> {
            String query = searchBar.getText();
            List<SearchResult> results = factory.search(query);

            arrayResult = results.toArray(new SearchResult[0]);

            jList.setListData(arrayResult);

            jList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JList list = (JList)e.getSource();

                    int index = list.locationToIndex(e.getPoint());

                    arrayResult[index].getPositions().sort(Integer::compareTo);

                    String filePositions = arrayResult[index].getPositions().stream().map(Object::toString).collect(Collectors.joining(", "));

                    positionsLabel.setText("<html><p>" + filePositions + "</p></html>");
                    panel.add(positionsLabel, BorderLayout.NORTH);

                }
            });
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
