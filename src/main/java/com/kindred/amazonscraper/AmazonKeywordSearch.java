package com.kindred.amazonscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;

/**
 * Created by Chris on 3/9/2017.
 */
public class AmazonKeywordSearch {
    private static final String baseUrl = "https://www.amazon.com/s/?keyword=";

    AmazonKeywordSearch(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            keyword = encodedKeyword;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.keyword = keyword;
        page = 1;

        readPage();
    }

    public String keyword;
    public Integer page;
    public String getUrl() {
        return baseUrl + keyword + (page == 1 ? "" : "&page="+page);
    }

    public HashMap<String, Product> productMap;

    private int _rawIdx = 0;
    private int _rawWithSponsoredIdx = 0;
    private int _sponsIdx = 0;

    public boolean readPage() {
        try {
            productMap = new HashMap<>();

            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("rerainc", "4yn1L#42".toCharArray());
                }
            };
            Authenticator.setDefault(authenticator);

            URL url = new URL(getUrl());

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("us-fl.proxymesh.com", 31280));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setRequestProperty("User-Agent", RandomUserAgent.getRandomUserAgent());

            int responseCode = connection.getResponseCode();
            if (responseCode != 200)
            {
                throw new Exception("Error in call. Response Code: " + responseCode + ". Response Body: " + connection.getResponseMessage());
            }

            StringBuilder html = new StringBuilder();
            BufferedReader rdr = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = rdr.readLine()) != null) {
                html.append(line + "\n");
            }

            if (html.toString().contains("did not match any products.")) return false;

            Document doc = Jsoup.parse(html.toString());
            Elements results = doc.select(".s-result-list li");

            for (Element r : results) {
                String asin = r.attr("data-asin");

                if (asin != null && asin.length() > 0) {
                    if (!productMap.containsKey(asin))
                    {
                        Product prod = new Product(asin);

                        Elements elements = r.select("img.s-sponsored-info-icon");
                        if (!elements.isEmpty()) prod.isSponsored = true;

                        elements = r.select("span.sx-amazonschoice-color");
                        if (!elements.isEmpty()) prod.isAmazonsChoice = true;

                        elements = r.select("span.sx-bestseller-color");
                        if (!elements.isEmpty()) prod.isBestSeller = true;

                        productMap.put(asin, prod);

                        prod.rawWithSponsoredIndex = ++_rawWithSponsoredIdx;
                        if (prod.isSponsored) prod.sponsoredIndex = ++_sponsIdx;
                        else prod.rawIndex = ++_rawIdx;
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean nextPage() {
        try {

            ++page;
            return readPage();

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public Product getProductForAsin(String asin) {
        if (productMap == null || !productMap.containsKey(asin)) return null;
        return productMap.get(asin);
    }

}
