package org.schabi.newpipe.downloader.ratelimiting;

import org.schabi.newpipe.downloader.ratelimiting.limiter.RateLimiter;

import java.io.IOException;
import java.net.ProtocolException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RateLimitedClientWrapper {
    private static final boolean DEBUG_PRINT =
        "1".equals(System.getProperty("rateLimitClientDebugPrint",
            System.getenv("RATE_LIMIT_CLIENT_DEBUG_PRINT")));

    private static final int REQUEST_RATE_LIMITED_WAIT_MS = 5_000;
    private static final Map<Predicate<String>, RateLimiter> FORCED_RATE_LIMITERS = Map.ofEntries(
        Map.entry(host -> host.endsWith("youtube.com"),
            RateLimiter.create(1.6, Duration.ofSeconds(1))),
        Map.entry(host -> host.endsWith("bandcamp.com"),
            RateLimiter.create(1.6, Duration.ofSeconds(1)))
    );

    private final OkHttpClient client;
    private final Map<String, RateLimiter> hostRateLimiters = new LinkedHashMap<>();

    public RateLimitedClientWrapper(final OkHttpClient client) {
        this.client = client;
    }

    protected RateLimiter getRateLimiterFor(final Request request) {
        return hostRateLimiters.computeIfAbsent(request.url().host(), host ->
            FORCED_RATE_LIMITERS.entrySet()
                .stream()
                .filter(e -> e.getKey().test(host))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseGet(() ->
                    // Default rate limiter per domain
                    RateLimiter.create(5, Duration.ofSeconds(3))));
    }

    public Response executeRequestWithLimit(final Request request) throws IOException {
        Exception cause = null;
        for (int tries = 1; tries <= 3; tries++) {
            try {
                final double rateLimitedSec = getRateLimiterFor(request).acquire();
                if (DEBUG_PRINT) {
                    System.out.println(
                        "[RATE-LIMIT] Waited " + rateLimitedSec + "s for " + request.url());
                }

                final Response response = client.newCall(request).execute();
                if (response.code() != 429) { // 429 = Too many requests
                    return response;
                }
                cause = new IllegalStateException("HTTP 429 - Too many requests");
            } catch (final ProtocolException pre) {
                if (!pre.getMessage().startsWith("Too many follow-up")) { // -> Too many requests
                    throw pre;
                }
                cause = pre;
            }

            final int waitMs = REQUEST_RATE_LIMITED_WAIT_MS * tries;
            if (DEBUG_PRINT) {
                System.out.println(
                    "[TOO-MANY-REQUESTS] Waiting " + waitMs + "ms for " + request.url());
            }
            try {
                Thread.sleep(waitMs);
            } catch (final InterruptedException iex) {
                Thread.currentThread().interrupt();
            }
        }
        throw new IllegalStateException(
            "Retrying/Rate-limiting for " + request.url() + " failed", cause);
    }
}
