package scrapers.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import open.java.toolkit.Regex;
import open.java.toolkit.http.Request;
import scrapers.IScraper;

public class RegexScraper implements IScraper{
    private int workerDone = 0;
    private int links = 0;
    private ArrayList<String> proxies = new ArrayList<String>();
    private File urls = null;
    
    public RegexScraper() {
        urls = new File("sources/urls.txt");
        if(!urls.exists()){
            try {
                urls.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void scrape() {
        ExecutorService executorService = Executors.newFixedThreadPool(25);
        try (Stream<String> stream = Files.lines(Path.of(urls.toURI()))) {
            stream.forEach( link -> {
                links++;
                executorService.execute(new Checker(link));
            });
        } catch (Exception e){
            e.printStackTrace();
        }
        new Thread( () -> {
            while(true){
                if(this.isdone()){
                    executorService.shutdownNow();
                    break;
                }
            }
        }).start();
    }

    @Override
    public boolean isdone() {
        return workerDone==links;
    }

    @Override
    public ArrayList<String> getProxies() {
        return proxies;
    }
    
    class Checker implements Runnable {
        private String link;
        public Checker(String link) {
            this.link = link;
        }
        @Override
        public void run() {
            String body = (String) Request.send(link, "get", null).body();
            proxies = Regex.getMatches(body, "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(?=[^\\d])\\s*:?\\s*(\\d{2,5})");
            workerDone++;
        }
    }
}
