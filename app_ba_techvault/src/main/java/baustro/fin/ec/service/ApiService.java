package baustro.fin.ec.service;

import javax.swing.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApiService {

    private static final String BASE_URL = "http://localhost:7777";
    private static final long CACHE_TTL_MS = 20_000; // 20s
    private static final HttpClient client =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

    private static class CacheEntry {
        String value;
        long time;
        CacheEntry(String v,long t){ value=v; time=t; }
    }

    private static final Map<String,CacheEntry> cache = new ConcurrentHashMap<>();

    public static void fetchCount(String endpoint, JLabel label){

        long now = System.currentTimeMillis();

        CacheEntry entry = cache.get(endpoint);

        if(entry != null && (now - entry.time) < CACHE_TTL_MS){
            label.setText(entry.value);
            return;
        }

        SwingWorker<Void,Void> worker = new SwingWorker<>(){

            String value = "0";

            protected Void doInBackground(){
                try{

                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + endpoint))
                            .GET()
                            .timeout(Duration.ofSeconds(5))
                            .build();

                    HttpResponse<String> res =
                            client.send(req,HttpResponse.BodyHandlers.ofString());

                    value = res.body().replaceAll("[^0-9]","");

                }catch(Exception ignored){}

                return null;
            }

            protected void done(){
                label.setText(value);
                cache.put(endpoint,new CacheEntry(value,System.currentTimeMillis()));
            }
        };

        worker.execute();
    }

}