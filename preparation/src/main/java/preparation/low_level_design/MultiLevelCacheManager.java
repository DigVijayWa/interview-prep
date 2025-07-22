package org.example.lld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiLevelCacheManager {

    static class CacheNode {
        private String key;
        private String value;
        private long timestamp;
        private CacheNode next;
        private CacheNode prev;

        public CacheNode() {

        }

        public CacheNode(String key, String value) {
            this.key = key;
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
    }

    static class CacheService {
        private final Map<String, CacheNode> cache;
        private final CacheNode head;
        private final CacheNode tail;
        private final int capacity;

        public CacheService(final int capacity) {
            this.cache = new ConcurrentHashMap<>(capacity);
            this.head = new CacheNode();
            this.tail = new CacheNode();
            this.capacity = capacity;

            head.next = tail;
            tail.prev = head;
        }

        public void put(final String key, final String value) {
            if(cache.containsKey(key)) {
                CacheNode node = cache.get(key);
                remove(node);
                node.value = value;
                node.timestamp = System.currentTimeMillis();
                addFront(node);
            } else {
                if(cache.size() == capacity) {
                    //the cache is full and we need to evict.
                    removeLast();
                }
                final CacheNode node = new CacheNode(key, value);
                addFront(node);
                cache.put(key, node);
            }
        }

        public String get(final String key) {
            if(!cache.containsKey(key)) {
                return null;
            }
            final CacheNode node = cache.get(key);
            remove(node);
            node.timestamp = System.currentTimeMillis();
            addFront(node);
            return node.value;
        }

        public boolean containsKey(final String key) {
            return cache.containsKey(key);
        }

        public boolean remove(final String key) {
            if(cache.containsKey(key)) {
                CacheNode node = cache.get(key);
                remove(node);
                cache.remove(key);
                return true;
            }
            return false;
        }

        private void remove(final CacheNode node) {
            node.next.prev = node.prev;
            node.prev.next = node.next;
        }

        private void addFront(final CacheNode node) {
            node.next = head.next;
            head.next.prev = node;

            head.next = node;
            node.prev = head;
        }

        private void removeLast() {
            final CacheNode node = tail.prev;
            remove(node);
        }
    }

    static class MultiLevelCacheService {
        private final List<CacheService> multiLevelCache;
        // 0 Lowest 1 Highest
        // 0 Fastest 1 Slowest
        private final int levels;
        public MultiLevelCacheService(final int levels, final int capacity) {
            this.multiLevelCache = new ArrayList<>(levels);
            for(int i = 0; i < levels; i++) {
                multiLevelCache.add(new CacheService(capacity));
            }
            this.levels = levels;
        }

        public void put(final String key, final String value) {
            //put the key value pair in lowest level
            for(int i=0; i<levels; i++) {
                if(this.multiLevelCache.get(i).containsKey(key)) {
                    this.multiLevelCache.get(i).put(key, value);
                    promote(i, key);
                    return;
                }
            }
            this.multiLevelCache.get(levels-1).put(key, value);
        }

        public String get(final String key) {
            for(int i=0; i<levels; i++) {
                if(this.multiLevelCache.get(i).containsKey(key)) {
                    final String value =  this.multiLevelCache.get(i).get(key);
                    promote(i, key);
                    return value;
                }
            }
            return null;
        }

        private void promote(final int index, final String key) {
            for(int i=index; i>0; i--) {
                final String value = this.multiLevelCache.get(i).get(key);
                this.multiLevelCache.get(i).remove(key);
                this.multiLevelCache.get(i-1).put(key, value);
            }
        }
    }
}
