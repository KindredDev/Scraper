package com.kindred.amazonscraper.Data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import com.kindred.amazonscraper.Data.Keyword;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import java.util.Arrays;

import java.util.List;

/**
 * Created by Chris on 3/9/2017.
 */
public class DAL {
    private static MongoClient client;
    public static MongoClient getClient() {
        if (client == null) {
            client = new MongoClient(new MongoClientURI("mongodb://scraper:Fe,tia1t.@ds127300.mlab.com:27300/scraper-db"));
        }

        return client;
    }

    public static ArrayList<Keyword> getKeywords() {

        MongoDatabase database = getClient().getDatabase("scraper-db");
        MongoCollection<Document> collection = database.getCollection("keywords");
        List<Document> keywords = collection.find().into(new ArrayList<>());

        ArrayList<Keyword> results = new ArrayList<>();
        for (Document keyword : keywords) {
            ArrayList<String> asins = (ArrayList<String>) keyword.get("asins");
            results.add(new Keyword(keyword.get("keyword").toString(), asins));
        }

        return results;
    }

    public static boolean writeResult(Result result) {
        MongoDatabase database = getClient().getDatabase("scraper-db");
        MongoCollection<Document> collection = database.getCollection("results");
        collection.insertOne(result.toDocument());
        return true;
    }

    public static boolean writeResults(ArrayList<Result> results) {
        MongoDatabase database = getClient().getDatabase("scraper-db");
        MongoCollection<Document> collection = database.getCollection("results");

        List<Document> docs = new ArrayList<>();
        for(Result r : results) docs.add(r.toDocument());

        collection.insertMany(docs);
        return true;
    }
}

