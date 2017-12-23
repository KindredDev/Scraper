package com.kindred.amazonscraper;

public class Product {
    public Product(String asin) {
        this.asin = asin;
    }

    public String asin;

    public Integer rawIndex;
    public Integer rawWithSponsoredIndex;
    public Integer sponsoredIndex = null;

    public boolean isSponsored = false;

    public boolean isAmazonsChoice = false;
    public boolean isBestSeller = false;

    public Float rating = null;
    public Integer reviewsCount = null;
}
