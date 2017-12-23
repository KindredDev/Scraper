package com.kindred.amazonscraper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Chris on 3/9/2017.
 */
public class DAL {
    private static ReentrantLock _lock = new ReentrantLock();
    private static final String _path = "results.data";

    public static ArrayList<Keyword> getKeywords() {
        ArrayList<Keyword> keywords = new ArrayList<>();

        return keywords;
    }

    public static ArrayList<String> getKeywordsToSearch() {
        ArrayList<String> list = new ArrayList<>();
        list.add("probiotic");
        list.add("probiotics");
        list.add("probiotics for men");
        list.add("probiotics for women");
        return list;

        // todo: attach to database
    }

    public static ArrayList<String> getAsinsForKeyword(String keyword) {
        ArrayList<String> list = new ArrayList<>();
        list.add("B00Y8MP4G6");
        list.add("B00Y8MP5VA");
        list.add("B000GWG8FS");
        list.add("B01LVYJK6U");
        return list;

        // todo: attach to database
    }

    public static boolean writeAsinResult(String asin, String keyword, Integer index) {

        String content = new Date() + "\t" + asin + "\t" + keyword + "\t" + index + "\n";

        FileWriter fw = null;
        BufferedWriter bw = null;

        _lock.lock();
        try {
            fw = new FileWriter(_path, true);
            bw = new BufferedWriter(fw);

            bw.write(content);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            _lock.unlock();
        }

        return true;

        // todo: write to database
    }

    public class Keyword {
        public String keyword;
        public ArrayList<String> asins = new ArrayList<>();
    }
}
