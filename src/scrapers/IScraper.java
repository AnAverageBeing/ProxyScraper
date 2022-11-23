package scrapers;

import java.util.ArrayList;

public interface IScraper {
    public void scrape();
    public boolean isdone();
    public ArrayList<String> getProxies();
}
