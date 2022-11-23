package scrapers.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import scrapers.IScraper;

public class Raw implements IScraper{
    private int workerDone = 0;
    private int links = 0;
    private ArrayList<String> proxies = new ArrayList<String>();
    private File urls = null;
    public Raw(){
        urls = new File("sources/raw-urls.txt");
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
        return links==workerDone;
    }
    @Override
    public ArrayList<String> getProxies() {
        return proxies;
    }

    class Checker implements Runnable{

        private String link;
        public Checker(String link){
            this.link = link;
        }
        @Override
        public void run() {
            try {
                Document scrapedproxies = Jsoup.connect(link).timeout(10000).get();
                proxies.addAll(Arrays.stream(scrapedproxies.text().split(" ")).distinct().collect(Collectors.toList()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            workerDone++;
        }

    }
}
