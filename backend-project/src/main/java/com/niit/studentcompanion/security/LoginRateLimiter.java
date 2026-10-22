package com.niit.studentcompanion.security;

import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LoginRateLimiter {

    private static final int MAX_FAILURES = 10;
    private static final int MAX_TRACKED_CLIENTS = 10_000;
    private static final long WINDOW_NANOS = Duration.ofMinutes(15).toNanos();

    private final Map<String, AttemptWindow> attempts = new LinkedHashMap<>(16, 0.75f, true);

    public synchronized void assertAllowed(String clientAddress) {
        String key = normalizeAddress(clientAddress);
        AttemptWindow window = attempts.get(key);
        if (window != null && isExpired(window)) {
            attempts.remove(key);
            return;
        }
        if (window != null && window.failures() >= MAX_FAILURES) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many login attempts. Try again later.");
        }
    }

    public synchronized void recordFailure(String clientAddress) {
        String key = normalizeAddress(clientAddress);
        long now = System.nanoTime();
        AttemptWindow existing = attempts.get(key);
        int failures = existing == null || isExpired(existing)
                ? 1
                : existing.failures() + 1;
        attempts.put(key, new AttemptWindow(now, failures));

        while (attempts.size() > MAX_TRACKED_CLIENTS) {
            Iterator<String> oldest = attempts.keySet().iterator();
            oldest.next();
            oldest.remove();
        }
    }

    public synchronized void clear(String clientAddress) {
        attempts.remove(normalizeAddress(clientAddress));
    }

    private boolean isExpired(AttemptWindow window) {
        return System.nanoTime() - window.startedAtNanos() >= WINDOW_NANOS;
    }

    private String normalizeAddress(String clientAddress) {
        return clientAddress == null || clientAddress.isBlank()
                ? "unknown"
                : clientAddress;
    }

    private record AttemptWindow(long startedAtNanos, int failures) {
    }
}
