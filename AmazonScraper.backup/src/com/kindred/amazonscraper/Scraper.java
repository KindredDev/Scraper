package com.kindred.amazonscraper;

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

        ArrayList<String> keywords = DAL.getKeywordsToSearch();

        for (String keyword : keywords) {
            Request req = new Request(keyword);
            queue.add(req);
            requests.add(req);
        }

        // Thread producer = new Thread(new Producer(queue, requests));
        // producer.start();

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
        System.out.println("runtime: " + runtime.toHours() + ":" + runtime.toMinutes() + ":" + runtime.toMillis()/1000 + "." + runtime.toMillis()%1000);

        for (Consumer con : consumers)
            con.quit = true;
    }
}

class Request {
    static final int PAGES_TO_SEARCH = 10;

    Request(String keyword) {
        this.keyword = keyword;
        asins = new HashSet<>(DAL.getAsinsForKeyword(keyword));
        search = new AmazonKeywordSearch(keyword);

        System.out.println(keyword + ": " + asins);
    }

    public boolean complete = false;
    private String keyword;
    private HashSet<String> asins;
    private AmazonKeywordSearch search;

    public boolean run() {
        try {
            HashSet<String> foundAsins = new HashSet<>();

            for (int i = 0; i < PAGES_TO_SEARCH; ++i) {
                if (foundAsins.size() == asins.size()) break;

                for (String asin : asins) {
                    AmazonKeywordSearch.Product prod = search.getResultIndexOfAsin(asin);

                    if (prod != null && !foundAsins.contains(asin)) {
                        foundAsins.add(asin);
                        DAL.writeAsinResult(asin, keyword, prod.index);
                    }
                }

                if (!search.nextPage()) break;
            }

            for (String asin : asins) {
                if (!foundAsins.contains(asin))
                    DAL.writeAsinResult(asin, keyword, -1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        complete = true;
        return true;
    }
}

class Producer implements Runnable {
    ConcurrentLinkedQueue<Request> queue;
    ArrayList<Request> requests;
    Producer(ConcurrentLinkedQueue<Request> queue, ArrayList<Request> requests) {
        this.queue = queue;
        this.requests = requests;
    }

    public void run() {
        try {

            ArrayList<String> keywords = DAL.getKeywordsToSearch();

            for (String keyword : keywords) {
                Request req = new Request(keyword);
                queue.add(req);
                requests.add(req);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
