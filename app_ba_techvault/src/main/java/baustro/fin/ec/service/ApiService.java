package baustro.fin.ec.service;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApiService {

    private static final String BASE_URL = "http://localhost:7777";

    private static final long CACHE_TTL_MS = 20000;

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static class CacheEntry {
        String value;
        long time;
        long latency;
        boolean ok;

        CacheEntry(String v,long t,long l,boolean o){
            value=v;
            time=t;
            latency=l;
            ok=o;
        }
    }

    private static final Map<String,CacheEntry> cache = new ConcurrentHashMap<>();


    public static void fetchCount(String endpoint, JLabel label){

        long now = System.currentTimeMillis();

        CacheEntry entry = cache.get(endpoint);

        if(entry != null && (now-entry.time) < CACHE_TTL_MS){

            updateLabel(label,entry);

            return;
        }

        label.setText("⟳");
        label.setForeground(new Color(120,170,255));

        SwingWorker<Void,Void> worker = new SwingWorker<>(){

            String value="N/D";
            long latency=0;
            boolean ok=false;

            protected Void doInBackground(){

                long start = System.currentTimeMillis();

                try{

                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + endpoint))
                            .GET()
                            .timeout(Duration.ofSeconds(5))
                            .build();

                    HttpResponse<String> res =
                            client.send(req,HttpResponse.BodyHandlers.ofString());

                    latency = System.currentTimeMillis()-start;

                    String body = res.body();

                    String digits = body.replaceAll("[^0-9]","");

                    if(!digits.isEmpty()){

                        value = digits;

                    }else{

                        value="0";
                    }
                    ok = true;

                }catch(Exception e){

                    value="ERR";
                    ok=false;
                }

                return null;
            }

            protected void done(){

                cache.put(endpoint,new CacheEntry(
                        value,
                        System.currentTimeMillis(),
                        latency,
                        ok
                ));

                updateLabel(label,new CacheEntry(value,System.currentTimeMillis(),latency,ok));
            }
        };

        worker.execute();
    }

    private static void updateLabel(JLabel label,CacheEntry e){

        SwingUtilities.invokeLater(() -> {

            if(!e.ok){

                label.setText("ERR");
                label.setForeground(new Color(255,95,95));

            }else if(e.value.equals("0")){

                label.setText("0");
                label.setForeground(new Color(180,180,180));

            }else{

                label.setText(e.value);
                label.setForeground(new Color(120,255,160));
            }

            label.setToolTipText("Latencia: "+e.latency+" ms");
        });
    }
}
