package preparation.low_level_design;

import java.util.Map;
import java.util.concurrent.*;

public class KeyValueStore {
    private final Map<String, KeyValuePair> datastore;
    private final long ttlInMillis;
    private final ScheduledExecutorService cleaner;

    public KeyValueStore(final long ttlInMillis) {
        this.datastore = new ConcurrentHashMap<>();
        this.ttlInMillis = ttlInMillis;

        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        this.cleaner.scheduleAtFixedRate(this::evictExpiredKeys, ttlInMillis, ttlInMillis, TimeUnit.MILLISECONDS);
    }

    public void put(final String key, final String value) {
        final KeyValuePair pair = new KeyValuePair(key, value, System.currentTimeMillis());
        datastore.put(key, pair);
    }

    public String get(final String key) {
        final KeyValuePair pair = datastore.get(key);
        if (pair == null || isExpired(pair)) {
            datastore.remove(key);
            return null;
        }
        return pair.value;
    }

    public void delete(final String key) {
        datastore.remove(key);
    }

    private void evictExpiredKeys() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, KeyValuePair> entry : datastore.entrySet()) {
            if (isExpired(entry.getValue(), now)) {
                datastore.remove(entry.getKey());
            }
        }
    }

    private boolean isExpired(KeyValuePair pair) {
        return isExpired(pair, System.currentTimeMillis());
    }

    private boolean isExpired(KeyValuePair pair, long now) {
        return now - pair.createdTime >= ttlInMillis;
    }

    public void shutdown() {
        cleaner.shutdown();
    }
}

public class KeyValuePair {
    final String key;
    final String value;
    long createdTime;

    public KeyValuePair(final String key, final String value, final long createdTime) {
        this.createdTime = createdTime;
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        KeyValuePair that = (KeyValuePair) obj;
        return createdTime == that.createdTime &&
            java.util.Objects.equals(key, that.key) &&
            java.util.Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(key, value, createdTime);
    }
}
