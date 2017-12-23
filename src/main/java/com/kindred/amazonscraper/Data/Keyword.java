package com.kindred.amazonscraper.Data;

import java.util.ArrayList;

public class Keyword {
    public Keyword (String keyword, ArrayList<String> asins) {
        this.keyword = keyword;
        this.asins = asins;
    }

    public String keyword;
    public ArrayList<String> asins = new ArrayList<>();
}
