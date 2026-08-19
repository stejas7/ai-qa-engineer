package com.aiqa.performance;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** M11 bounded load runner. It intentionally supports GET only and caps workload to protect targets. */
@Service
public class LoadTestService {
    private static final int MAX_REQUESTS = 500;
    private static final int MAX_CONCURRENCY = 25;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public LoadTestResult run(LoadTestRequest request) {
        URI target = validate(request.targetUrl());
        int total = Math.max(1, Math.min(request.requests(), MAX_REQUESTS));
        int concurrency = Math.max(1, Math.min(request.concurrency(), Math.min(MAX_CONCURRENCY, total)));
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger failures = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        Instant started = Instant.now();
        try {
            List<CompletableFuture<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < total; i++) {
                tasks.add(CompletableFuture.runAsync(() -> execute(target, latencies, failures), executor));
            }
            CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();
        } finally {
            executor.shutdown();
            try { executor.awaitTermination(5, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        long durationMs = Math.max(1, Duration.between(started, Instant.now()).toMillis());
        List<Long> sorted = new ArrayList<>(latencies); Collections.sort(sorted);
        double errorRate = failures.get() * 100.0 / total;
        double throughput = total * 1000.0 / durationMs;
        long p50 = percentile(sorted, .50), p95 = percentile(sorted, .95), p99 = percentile(sorted, .99);
        boolean sloPassed = p95 <= request.maxP95Ms() && errorRate <= request.maxErrorRatePercent();
        String risk = sloPassed ? "LOW" : errorRate > request.maxErrorRatePercent() ? "HIGH" : "MEDIUM";
        String summary = sloPassed
                ? "Performance gate passed: p95 latency and error rate are within the requested SLO."
                : "Performance gate failed: review latency/error evidence before release.";
        return new LoadTestResult(target.toString(), total, concurrency, durationMs, p50, p95, p99, throughput,
                failures.get(), errorRate, request.maxP95Ms(), request.maxErrorRatePercent(), sloPassed, risk, summary);
    }

    private void execute(URI target, List<Long> latencies, AtomicInteger failures) {
        long start = System.nanoTime();
        try {
            HttpRequest req = HttpRequest.newBuilder(target).GET().timeout(REQUEST_TIMEOUT).header("User-Agent", "AI-UAT-Engineer-M11").build();
            int status = client.send(req, HttpResponse.BodyHandlers.discarding()).statusCode();
            if (status < 200 || status >= 400) failures.incrementAndGet();
        } catch (Exception e) { failures.incrementAndGet(); }
        finally { latencies.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)); }
    }

    private URI validate(String raw) {
        URI uri = URI.create(raw);
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) || uri.getHost() == null)
            throw new IllegalArgumentException("targetUrl must be an absolute http/https URL");
        return uri;
    }

    private long percentile(List<Long> values, double percentile) {
        if (values.isEmpty()) return 0;
        int index = Math.max(0, (int)Math.ceil(percentile * values.size()) - 1);
        return values.get(Math.min(index, values.size() - 1));
    }

    public record LoadTestRequest(String targetUrl, int requests, int concurrency, long maxP95Ms, double maxErrorRatePercent) {}
    public record LoadTestResult(String targetUrl, int requests, int concurrency, long durationMs, long p50Ms, long p95Ms,
                                 long p99Ms, double throughputPerSecond, int failures, double errorRatePercent,
                                 long maxP95Ms, double maxErrorRatePercent, boolean sloPassed, String releaseRisk, String summary) {}
}
