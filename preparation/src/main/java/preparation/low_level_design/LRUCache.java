package preparation.low_level_design;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LRUCache {
    static class CacheNode {
        private String key;
        private String value;
        private long timestamp;
        private CacheNode next;
        private CacheNode prev;

        public CacheNode() {
        }

        public CacheNode(String key, String value, long timestamp) {
            this.key = key;
            this.value = value;
            this.timestamp = timestamp;
            this.next = null;
            this.prev = null;
        }
    }

    static class LRUCacheService {
        private final Map<String, CacheNode> cache;
        private final CacheNode head;
        private final CacheNode tail;
        private final int capacity;


        public LRUCacheService(final int capacity) {
            this.cache = new ConcurrentHashMap<>(capacity);
            this.head = new CacheNode();
            this.tail = new CacheNode();
            this.capacity = capacity;

            head.next = tail;
            tail.prev = head;
        }

        public String get(final String key) {
            if(cache.containsKey(key)) {
                CacheNode node = cache.get(key);
                remove(node);

                node.timestamp = System.currentTimeMillis();
                add(node);
                cache.put(key, node);
                return node.value;
            }
            return null;
        }

        public void put(final String key, final String value) {
            if(cache.containsKey(key)) {
                CacheNode node = cache.get(key);
                remove(node);
                node.timestamp = System.currentTimeMillis();
                node.value = value;
                add(node);
                cache.put(key, node);
            } else {
                if(cache.size() == capacity) {
                    //eviction logic.
                    final CacheNode node = removeFromLast();
                    cache.remove(node.key);
                }
                CacheNode node = new CacheNode(key, value,  System.currentTimeMillis());
                cache.put(key, node);
                add(node);
            }
        }

        private void remove(final CacheNode node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        private CacheNode removeFromLast() {
            CacheNode node = tail.prev;
            remove(node);
            return node;
        }

        private void add(CacheNode node) {
            //between 1 and 3
            head.next.prev = node;
            node.next = head.next;

            //between head and 3
            node.prev = head;
            head.next = node;
        }

        public void printCache() {
            CacheNode current = head.next;
            System.out.print("Cache (MRU -> LRU): ");
            while (current != tail) {
                System.out.print("(" + current.key + "=" + current.value + ") ");
                current = current.next;
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        System.out.println("Testing LRU Cache with capacity 3:");
        LRUCacheService cache = new LRUCacheService(3);

        System.out.println("\n--- Put operations ---");
        cache.put("key1", "value1");
        cache.printCache(); // Expected: (key1=value1)
        cache.put("key2", "value2");
        cache.printCache(); // Expected: (key2=value2) (key1=value1)
        cache.put("key3", "value3");
        cache.printCache(); // Expected: (key3=value3) (key2=value2) (key1=value1)

        System.out.println("\n--- Get operations ---");
        String value = cache.get("key2");
        System.out.println("Get key2: " + value); // Expected: value2
        cache.printCache(); // Expected: (key2=value2) (key3=value3) (key1=value1) - key2 moved to front

        value = cache.get("key1");
        System.out.println("Get key1: " + value); // Expected: value1
        cache.printCache(); // Expected: (key1=value1) (key2=value2) (key3=value3) - key1 moved to front

        value = cache.get("key4");
        System.out.println("Get key4: " + value); // Expected: null
        cache.printCache(); // Expected: (key1=value1) (key2=value2) (key3=value3) - no change

        System.out.println("\n--- Put causing eviction ---");
        cache.put("key4", "value4"); // key3 should be evicted
        cache.printCache(); // Expected: (key4=value4) (key1=value1) (key2=value2)

        cache.put("key5", "value5"); // key2 should be evicted
        cache.printCache(); // Expected: (key5=value5) (key4=value4) (key1=value1)

        System.out.println("\n--- Update existing key ---");
        cache.put("key4", "newValue4"); // key4 updated and moved to front
        cache.printCache(); // Expected: (key4=newValue4) (key5=value5) (key1=value1)

        value = cache.get("key5");
        System.out.println("Get key5: " + value);
        cache.printCache(); // Expected: (key5=value5) (key4=newValue4) (key1=value1)

        System.out.println("\n--- Edge cases ---");
        LRUCacheService smallCache = new LRUCacheService(1);
        smallCache.put("A", "ValA");
        smallCache.printCache(); // Expected: (A=ValA)
        smallCache.put("B", "ValB"); // A evicted
        smallCache.printCache(); // Expected: (B=ValB)
        System.out.println("Get A: " + smallCache.get("A")); // Expected: null
        smallCache.printCache(); // Expected: (B=ValB)
    }
}
