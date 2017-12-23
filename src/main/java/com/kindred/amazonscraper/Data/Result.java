package com.kindred.amazonscraper.Data;

import com.kindred.amazonscraper.Product;
import org.bson.Document;

import java.util.Date;

/**
 * Created by christophercabrera on 3/13/17.
 */
public class Result {
    public Date timestamp;
    public String keyword;
    public String asin;

    public Integer rawIndex = null;
    public Integer rawWithSponsoredIndex = null;
    public Integer sponsoredIndex = null;

    public boolean isSponsored = false;
    public boolean isAmazonsChoice = false;
    public boolean isBestSeller = false;

    public Float rating = null;
    public Integer reviewsCount = null;

    public Result(String keyword, Product product) {
        this.timestamp = new Date();
        this.keyword = keyword;
        this.asin = product.asin;
        this.rawIndex = product.rawIndex;
        this.rawWithSponsoredIndex = product.rawWithSponsoredIndex;
        this.sponsoredIndex = product.sponsoredIndex;
        this.isSponsored = product.isSponsored;
        this.isAmazonsChoice = product.isAmazonsChoice;
        this.isBestSeller = product.isBestSeller;
        this.rating = product.rating;
        this.reviewsCount = product.reviewsCount;
    }
    public Result(String keyword, String asin) {
        this.timestamp = new Date();
        this.keyword = keyword;
        this.asin = asin;
    }

    public Document toDocument() {
        return new Document("timestamp", timestamp)
                .append("keyword", keyword)
                .append("asin", asin)
                .append("rawIndex", rawIndex)
                .append("rawWithSponsoredIndex", rawWithSponsoredIndex)
                .append("sponsoredIndex", sponsoredIndex)
                .append("isSponsored", isSponsored)
                .append("isAmazonsChoice", isAmazonsChoice)
                .append("isBestSeller", isBestSeller)
                .append("rating", rating)
                .append("reviewsCount", reviewsCount);
    }
}
