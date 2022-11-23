import java.io.File;
import java.util.Date;

import scrapers.Manager;

public class App {
    public static void main(String[] args) throws Exception {
        //scraping
        Manager manager = new Manager();
        manager.startAll();
        System.out.println("Scraped "+manager.getAllScraped().size()+" unique proxies");
        String filepath = "scraped/Result-"+(new Date().toString().replace(" ", "_"));
        File file = new File(filepath);
        if(!file.exists()){
            file.createNewFile();
        }
        manager.saveAllProxies(file);    
        System.exit(0);    
    }
}
