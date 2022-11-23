package scrapers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import scrapers.impl.Geonode;
import scrapers.impl.Raw;
import scrapers.impl.RegexScraper;

public class Manager {
    HashSet<IScraper> scrapers = new HashSet<>();
    public Manager(){
        register();
    }

    private void register(){
        scrapers.add(new Geonode());
        scrapers.add(new Raw());
        scrapers.add(new RegexScraper());
    }

    public void startAll(){
        scrapers.stream().forEach( scraper -> {
            scraper.scrape();
            System.out.println("Started Scrapper::"+scraper.getClass().getSimpleName());
        });
    }

    public HashSet<String> getAllScraped(){
        HashSet<String> proxies = new HashSet<String>();
        scrapers.stream().forEach(scraper -> {
            while(!scraper.isdone()){
                System.out.print("\r");
            }
            proxies.addAll(scraper.getProxies());
        });
        return proxies;
    }

    public void saveAllProxies(File file) {
        try (FileWriter fw = new FileWriter(file)) {
            getAllScraped().forEach( proxy -> {
                if(proxy == null || proxy == " ") return;
                String[] boom = proxy.split(":");
                if(boom.length == 2 && boom[1].length() < 6) {
                    try {
                        fw.write(proxy+"\n");
                        fw.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
