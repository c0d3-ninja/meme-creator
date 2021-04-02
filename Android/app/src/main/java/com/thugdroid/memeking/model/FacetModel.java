package com.thugdroid.memeking.model;

public class FacetModel {
    public static String API_KEY_VALUE="value";
    public static String API_KEY_HIGHLIGHTED="highlighted";
    public static String API_KEY_COUNT="count";
    public static String API_KEY_HITS="facetHits";


    private String value, highlighted;
    private int count;

    public FacetModel(String value, String highlighted, int count) {
        this.value = value;
        this.highlighted = highlighted;
        this.count = count;
    }

    public String getValue() {
        return value;
    }

    public String getHighlighted() {
        return highlighted;
    }

    public int getCount() {
        return count;
    }
}
