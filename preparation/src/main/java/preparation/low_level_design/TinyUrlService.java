package preparation.low_level_design;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.example.lld.TinyUrlService.URL.DEFAULT_TTL;

public class TinyUrlService {

    static class URL {
        private String longUrl;
        private String shortUrl;
        private int ttl;
        private int frequency;
        private LocalDateTime updatedAt;

        public static final int DEFAULT_TTL = 60;
        public URL(String longUrl, String shortUrl, int ttl) {
            this.longUrl = longUrl;
            this.shortUrl = shortUrl;
            this.ttl = ttl;
            this.frequency = 1;
            this.updatedAt = LocalDateTime.now();
        }

        public String getLongUrl() {
            return longUrl;
        }

        public void setLongUrl(String longUrl) {
            this.longUrl = longUrl;
        }

        public String getShortUrl() {
            return shortUrl;
        }

        public void setShortUrl(String shortUrl) {
            this.shortUrl = shortUrl;
        }

        public int getTtl() {
            return ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }

        public int getFrequency() {
            return frequency;
        }

        public void setFrequency(int frequency) {
            this.frequency = frequency;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    static class DataStore {
        final Map<String, URL> shortUrlMap;
        final Map<String, URL> longUrlMap;
        final AtomicLong idCounter;
        final ScheduledExecutorService scheduledExecutorService;

        public DataStore() {
            shortUrlMap = new ConcurrentHashMap<>();
            longUrlMap = new ConcurrentHashMap<>();
            idCounter = new AtomicLong();
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            this.scheduledExecutorService.scheduleWithFixedDelay(this::scheduledCleanup, 0, DEFAULT_TTL, TimeUnit.SECONDS);
        }

        public String getShortUrl(final String longUrl) {
            if(longUrlMap.containsKey(longUrl)) {
                final URL url = longUrlMap.get(longUrl);
                url.setUpdatedAt(LocalDateTime.now());
                url.setFrequency(url.getFrequency() + 1);
                longUrlMap.put(longUrl, url);
                return url.getShortUrl();
            }

            final String shortUrl = Base62Service.encode(idCounter.incrementAndGet());
            final URL url = new URL(longUrl, shortUrl, DEFAULT_TTL);
            shortUrlMap.put(shortUrl, url);
            longUrlMap.put(longUrl, url);
            return shortUrl;
        }

        public String getLongUrl(final String shortUrl) {
            if(!shortUrlMap.containsKey(shortUrl)) {
                throw new IllegalArgumentException("Invalid shortUrl");
            }
            final URL url = shortUrlMap.get(shortUrl);
            url.setUpdatedAt(LocalDateTime.now());
            url.setFrequency(url.getFrequency() + 1);
            shortUrlMap.put(shortUrl, url);
            return url.getLongUrl();
        }

        private synchronized void scheduledCleanup() {
            // iterate through longUrlMap
           longUrlMap.entrySet()
                   .removeIf(
                           entry ->
                                   entry.getValue().getUpdatedAt().isBefore(LocalDateTime.now().minusSeconds(DEFAULT_TTL)));

            //iterate through shortUrlMap
            shortUrlMap.entrySet()
                    .removeIf(
                            entry ->
                                    entry.getValue().getUpdatedAt().isBefore(LocalDateTime.now().minusSeconds(DEFAULT_TTL)));
        }

    }

    static class URLService {
        private final DataStore dataStore;

        URLService() {
            dataStore = new DataStore();
        }

        public String getShortUrl(final String longUrl) {
            return dataStore.getShortUrl(longUrl);
        }

        public String getLongUrl(final String shortUrl) {
           return dataStore.getLongUrl(shortUrl);
        }
    }

    static class Base62Service {
        final static String ENCODE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        final static int CODE = 62;

        public static String encode(Long counter) {
            StringBuilder result = new StringBuilder();
            while(counter > 0) {
                int remainder = (int) (counter % CODE);
                result.append(ENCODE.charAt(remainder));
                counter = counter / CODE;
            }

            return result.reverse().toString();
        }

        public static long decode(String str) {
            long result = 0;
            for (int i = 0; i < str.length(); i++) {
                result = result * CODE + ENCODE.indexOf(str.charAt(i));
            }
            return result;
        }
    }
}
