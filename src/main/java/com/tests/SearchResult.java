package com.tests;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
    private String fileName;
    private List<Integer> positions;

    public SearchResult(String fileName) {
        this.fileName = fileName;
        positions = new ArrayList<>();
    }

    public SearchResult(String fileName, List<Integer> positions) {
        this.fileName = fileName;
        this.positions = positions;
    }

    public String getFileName() {
        return fileName;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public Integer getNumberOfHits() {
        if (positions == null) {
            positions = new ArrayList<>();
        }
        return positions.size();
    }

    public void addPosition(int position) {
        if (positions == null) {
            positions = new ArrayList<>();
        }
        positions.add(position);
    }

    @Override
    public String toString() {
        String hits = getNumberOfHits() == 1? "hit": "hits";

        return fileName + ":      " + getNumberOfHits() + " " + hits + "   ";
    }
}
