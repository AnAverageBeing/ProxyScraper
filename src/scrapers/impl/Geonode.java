package scrapers.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import scrapers.IScraper;

public class Geonode implements IScraper {
    private int workerDone = 0;
    private boolean isDone = false;
    private ArrayList<String> proxies = new ArrayList<String>();
    private final String api = "https://proxylist.geonode.com/api/proxy-list?sort_by=lastChecked&protocols=socks4&limit=500";
    @Override
    public void scrape() {
        int page = 1;
        int total;
        JSONParser parser = new JSONParser();
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(25);
            URL apiurl = new URL(api+"&page="+page);
            String data = getData(apiurl);
            JSONObject jsondata = (JSONObject) parser.parse(data);
            total = Integer.parseInt(jsondata.get("total").toString());
             for(page = 1; page <= (total/500) ; page++){
                executorService.execute(new Worker(page));
            }
            new Thread( () -> {
                long timenow = System.currentTimeMillis();
                while(workerDone<((total/500)-1))
                {   
                    if(System.currentTimeMillis()-timenow > 10000){
                        isDone = true;
                        break;
                    }
                }
                executorService.shutdown();
                isDone=true;
            }).start();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }    
    }

    @Override
    public boolean isdone() {
        return isDone;
    }

    @Override
    public ArrayList<String> getProxies() {
        return proxies;
    }

    public static String getData(URL url) throws IOException {
        try (InputStream input = url.openStream()) {
            InputStreamReader isr = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder json = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }
            return json.toString();
        }
    }

    class Worker implements Runnable{
        int page;
        public Worker(int page){
            this.page = page;
        }
        @Override
        public void run() {
            JSONParser parser = new JSONParser();
            URL apiurl;
            try {
                apiurl = new URL(api+"&page="+page);
                String data = getData(apiurl);
                JSONObject jsondata = (JSONObject) parser.parse(data);
                JSONArray dataarr = (JSONArray) jsondata.get("data");
                Stream<JSONObject> jsonstream =  dataarr.stream();

                jsonstream.forEach( (proxyinfo) -> {
                    String ip = ((JSONObject) proxyinfo).get("ip").toString();
                    int port = Integer.parseInt(((JSONObject) proxyinfo).get("port").toString());
                    proxies.add(ip+":"+port);
                });

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            workerDone++;
        }
    }
}
