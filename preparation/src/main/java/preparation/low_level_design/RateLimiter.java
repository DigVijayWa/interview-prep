package preparation.low_level_design;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface RateLimiter {
    boolean allowRequest(String clientId);
}

class BucketBasedRateLimiter implements RateLimiter {

    private final Map<String, ClientBucket> map;
    private final int refillRatePerSec;
    private final int maxTokens;

    public BucketBasedRateLimiter(final int refillRatePerSec, final int maxTokens) {
        this.map = new ConcurrentHashMap<>();
        this.refillRatePerSec = refillRatePerSec;
        this.maxTokens = maxTokens;
    }

    @Override
    public boolean allowRequest(String clientId) {
        return map
                .computeIfAbsent(clientId, id -> new ClientBucket(refillRatePerSec, maxTokens))
                .isAllowed();
    }
}

class ClientBucket {
    private final int refillRatePerSec;
    private final int maxTokens;
    private double currentTokens;
    private long lastRefillTime;

    public ClientBucket(int refillRatePerSec, int maxTokens) {
        this.refillRatePerSec = refillRatePerSec;
        this.maxTokens = maxTokens;
        this.currentTokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized boolean isAllowed() {
        refill();
        if (currentTokens >= 1) {
            currentTokens -= 1;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long millisSinceLast = now - lastRefillTime;
        double tokensToAdd = (millisSinceLast / 1000.0) * refillRatePerSec;

        if (tokensToAdd > 0) {
            currentTokens = Math.min(maxTokens, currentTokens + tokensToAdd);
            lastRefillTime = now;
        }
    }
}
