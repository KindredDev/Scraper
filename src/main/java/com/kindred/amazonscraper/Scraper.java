package com.kindred.amazonscraper;

import com.kindred.amazonscraper.Data.DAL;
import com.kindred.amazonscraper.Data.Keyword;
import com.kindred.amazonscraper.Data.Result;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Chris on 3/9/2017.
 */
public class Scraper {
    static final int THREAD_COUNT = 5;

    public static void main(String[] args) {

        Instant start = Instant.now();

        ConcurrentLinkedQueue<Request> queue = new ConcurrentLinkedQueue<>();
        ArrayList<Request> requests = new ArrayList<>();

        ArrayList<Keyword> keywords = DAL.getKeywords();

        for (Keyword keyword : keywords) {
            Request req = new Request(keyword);
            queue.add(req);
            requests.add(req);
        }

        Consumer[] consumers = new Consumer[THREAD_COUNT];
        Thread[] consumerThreads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; ++i) {
            consumers[i] = new Consumer(queue);
            consumerThreads[i] = new Thread(consumers[i]);
            consumerThreads[i].start();
        }

        try {
            Thread.currentThread().sleep(1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        while (true) {
            boolean quit = true;
            for (Request req : requests) {
                if (!req.complete) {
                    quit = false;
                    break;
                }
            }

            if (quit) break;

            try {
                Thread.currentThread().sleep(500);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        Instant stop = Instant.now();

        Duration runtime = Duration.between(start, stop);
        System.out.println("runtime: " + runtime.toHours() + ":" + runtime.toMinutes() + ":" + runtime.toMillis() / 1000 + "." + runtime.toMillis() % 1000);

        for (Consumer con : consumers)
            con.quit = true;
    }
}

class Request {
    static final int PAGES_TO_SEARCH = 10;

    Request(Keyword keyword) {
        this.keyword = keyword.keyword;
        asins = new HashSet<>(keyword.asins);
        System.out.println(keyword + ": " + asins);

        search = new AmazonKeywordSearch(this.keyword);
    }

    public boolean complete = false;
    private String keyword;
    private HashSet<String> asins;
    private AmazonKeywordSearch search;

    public boolean run() {
        try {
            if (asins.size() == 0) return true;

            HashSet<String> foundAsins = new HashSet<>();
            ArrayList<Result> results = new ArrayList<>();

            for (int i = 0; i < PAGES_TO_SEARCH; ++i) {
                for (String asin : asins) {
                    Product prod = search.getProductForAsin(asin);

                    if (prod != null && !foundAsins.contains(asin)) {
                        foundAsins.add(asin);
                        results.add(new Result(keyword, prod));
                    }
                }

                if (foundAsins.size() == asins.size() || !search.nextPage()) break;
            }

            for (String asin : asins) {
                if (!foundAsins.contains(asin))
                    results.add(new Result(keyword, asin));
            }

            DAL.writeResults(results);

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        complete = true;
        return true;
    }
}

class Consumer implements Runnable {
    ConcurrentLinkedQueue<Request> queue;
    Consumer(ConcurrentLinkedQueue<Request> queue) {
        this.queue = queue;
    }

    public boolean quit = false;

    public void run() {
        boolean running = false;

        while (!quit) {

            try {
                Request request;
                if ((request = queue.poll()) != null) {
                    running = true;
                    request.run();

                } else {
                    running = false;
                    Thread.currentThread().sleep(500);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }
}
