package preparation.low_level_design;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class LFUCache {

    static class CacheNode {
        private final Object key;
        private String value;
        private int frequency;
        private long timestamp;
        public CacheNode(Object key, String value, int frequency, long timestamp) {
            this.key = key;
            this.value = value;
            this.frequency = frequency;
            this.timestamp = timestamp;
        }
    }

    static class LFUCacheService {
        private final Map<Object, CacheNode> cache;
        private final Queue<CacheNode> queue;
        private final int capacity;

        public LFUCacheService(final int capacity) {
            cache = new ConcurrentHashMap<>(capacity);
            queue = new PriorityQueue<>(capacity, (a, b) -> {
                if(a.frequency == b.frequency) {
                    return Long.compare(a.timestamp, b.timestamp);
                }
                return a.frequency - b.frequency;
            });
            this.capacity = capacity;
        }

        public String get(Object key) {
            // no capacity
            if(capacity == 0) {
                return null;
            }

            if(cache.containsKey(key)) {
                CacheNode node = cache.get(key);
                // remove the existing node from the queue.
                queue.remove(node);

                node.frequency++;
                node.timestamp = node.timestamp + 1;
                queue.offer(node);
                // just to be on  the safe side.
                cache.put(key, node);
                return node.value;
            }

            return null;
        }

        public void put(Object key, String value) {
            if(capacity == 0) {
                return;
            }

            if(cache.containsKey(key)) {
                CacheNode node = cache.get(key);
                queue.remove(node);
                node.frequency++;
                node.timestamp = node.timestamp + 1;
                node.value = value;
                queue.offer(node);
                cache.put(key, node);
            } else {
                if(cache.size() == capacity) {
                    CacheNode node = queue.poll();
                    if(node == null) {
                        throw new RuntimeException("Unexpected state reached :: queue Empty");
                    }
                    cache.remove(node.key);
                }

                CacheNode cacheNode = new CacheNode(key, value, 1, 1);
                cache.put(key, cacheNode);
                queue.offer(cacheNode);
            }
        }
    }

    public static void main(String[] args) {
        LFUCacheService cache = new LFUCacheService(2); // Capacity of 2

        cache.put(1, "10"); // Cache: {1: (10, freq=1, ts=1)}
        cache.put(2, "20"); // Cache: {1: (10, freq=1, ts=1), 2: (20, freq=1, ts=2)}

        System.out.println("Get 1: " + cache.get(1)); // 10. Cache: {1: (10, freq=2, ts=3), 2: (20, freq=1, ts=2)}
        System.out.println("Get 2: " + cache.get(2)); // 20. Cache: {1: (10, freq=2, ts=3), 2: (20, freq=2, ts=4)}

        cache.put(3, "30"); // Cache is full. Evict LFU.
        // Both 1 and 2 have freq 2.
        // Node 1 had ts=3, Node 2 had ts=4. So 1 is LRU among those.
        // Wait, no, Node 1 was updated at ts=3. Node 2 was updated at ts=4.
        // Node with key 1: freq=2, ts=3. Node with key 2: freq=2, ts=4.
        // The LRU tie-breaking means evict the one with the smallest timestamp.
        // So, Node 1 (ts=3) should be evicted.
        // Corrected: Node 1 (freq=2, ts=3) is evicted.
        // Cache: {2: (20, freq=2, ts=4), 3: (30, freq=1, ts=5)}
        // Re-evaluating eviction for the put(3,30) call:
        // Before put(3,30):
        // Node 1: key=1, val=10, freq=2, ts=3
        // Node 2: key=2, val=20, freq=2, ts=4
        // Both have freq 2. Node 1 has smaller timestamp (3 vs 4), so Node 1 is evicted.
        // Cache becomes: {2: (20, freq=2, ts=4), 3: (30, freq=1, ts=5)}

        System.out.println("Get 1: " + cache.get(1)); // -1 (1 was evicted)
        System.out.println("Get 2: " + cache.get(2)); // 20. Cache: {2: (20, freq=3, ts=6), 3: (30, freq=1, ts=5)}

        cache.put(4, "40"); // Cache is full. Evict LFU.
        // Node 2: freq=3, ts=6
        // Node 3: freq=1, ts=5
        // Node 3 is LFU (freq 1). Evict Node 3.
        // Cache: {2: (20, freq=3, ts=6), 4: (40, freq=1, ts=7)}
        System.out.println("Get 3: " + cache.get(3)); // -1 (3 was evicted)
        System.out.println("Get 4: " + cache.get(4)); // 40. Cache: {2: (20, freq=3, ts=6), 4: (40, freq=2, ts=8)}
    }
}
