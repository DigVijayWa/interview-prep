package preparation.low_level_design;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RateLimiterSlidingWindow {

    static class Request {
        final String client;
        final Long timestamp;

        public Request(final String client, final Long timestamp) {
            this.client = client;
            this.timestamp = timestamp;
        }
    }

    static class ReteLimiterService {
        final Map<String, Queue<Request>> cache;
        private final int limit;

        public ReteLimiterService(final int limit) {
            this.cache = new ConcurrentHashMap<>();
            this.limit = limit;
        }

        private void eviction(final String client) {
            if(cache.containsKey(client)) {
                final Queue<Request> queue = cache.get(client);
                if (queue != null) {
                    List<Request> requests = queue.stream()
                            .filter(
                                    request -> System.currentTimeMillis() - request.timestamp > TimeUnit.SECONDS.toMillis(limit))
                            .collect(Collectors.toList());
                    queue.removeAll(requests);
                }
            }
        }

        public synchronized boolean allowed(final String client) {
            eviction(client);
            final List<Request> requests = new ArrayList<>(cache.getOrDefault(client, new LinkedList<>()));
            if(requests.size() + 1 <= limit) {
                cache.computeIfAbsent(client, key -> new LinkedList<>()).add(new Request(client, System.currentTimeMillis()));
                return true;
            }
            return false;
        }
    }
}
