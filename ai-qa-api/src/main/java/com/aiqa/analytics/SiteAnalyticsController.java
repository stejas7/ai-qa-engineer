package com.aiqa.analytics;

import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Privacy-friendly product analytics for Auravis.
 * Stores anonymous browser/session identifiers and page paths only; raw IP addresses are not persisted.
 *
 * @author Tejas Shah
 */
@RestController
@RequestMapping("/api/analytics")
public class SiteAnalyticsController {
    private final SiteVisitRepository visits;

    public SiteAnalyticsController(SiteVisitRepository visits) {
        this.visits = visits;
    }

    /** Records a React route/page view. */
    @PostMapping("/visit")
    public Map<String, Object> recordVisit(@RequestBody VisitRequest request) {
        String path = normalizePath(request.path());
        String visitorId = normalizeVisitorId(request.visitorId());
        visits.save(new SiteVisit(path, visitorId, Instant.now()));
        return Map.of("recorded", true);
    }

    /** Returns aggregate traffic metrics used by the React Mission Dashboard. */
    @GetMapping("/stats")
    public TrafficStats stats() {
        List<SiteVisit> all = visits.findAll();
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);
        Instant todayStart = today.atStartOfDay(zone).toInstant();
        Instant sevenDayStart = today.minusDays(6).atStartOfDay(zone).toInstant();

        long total = all.size();
        long todayVisits = all.stream().filter(v -> !v.getVisitedAt().isBefore(todayStart)).count();
        long uniqueVisitors = all.stream().map(SiteVisit::getVisitorId).distinct().count();
        long uniqueToday = all.stream()
                .filter(v -> !v.getVisitedAt().isBefore(todayStart))
                .map(SiteVisit::getVisitorId)
                .distinct()
                .count();

        Map<String, Long> pageCounts = all.stream()
                .collect(Collectors.groupingBy(SiteVisit::getPath, Collectors.counting()));
        String mostVisitedPage = pageCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("—");

        List<PageVisit> topPages = pageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> new PageVisit(e.getKey(), e.getValue()))
                .toList();

        Map<LocalDate, Long> dailyCounts = visits.findAllByVisitedAtAfterOrderByVisitedAtAsc(sevenDayStart).stream()
                .collect(Collectors.groupingBy(v -> v.getVisitedAt().atZone(zone).toLocalDate(), Collectors.counting()));

        List<DailyVisit> last7Days = new ArrayList<>();
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("EEE");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            last7Days.add(new DailyVisit(date.toString(), labelFormat.format(date), dailyCounts.getOrDefault(date, 0L)));
        }

        return new TrafficStats(total, todayVisits, uniqueVisitors, uniqueToday, mostVisitedPage, last7Days, topPages);
    }

    /** Returns a small anonymous traffic trace for dashboard visibility. */
    @GetMapping("/recent")
    public List<RecentVisit> recent(@RequestParam(defaultValue = "20") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return visits.findAll(org.springframework.data.domain.PageRequest.of(
                        0, safeLimit, org.springframework.data.domain.Sort.by("visitedAt").descending()))
                .stream()
                .map(v -> new RecentVisit(v.getPath(), abbreviate(v.getVisitorId()), v.getVisitedAt()))
                .toList();
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) return "/";
        String clean = path.trim();
        if (!clean.startsWith("/")) clean = "/" + clean;
        return clean.length() > 120 ? clean.substring(0, 120) : clean;
    }

    private String normalizeVisitorId(String visitorId) {
        if (visitorId == null || visitorId.isBlank()) return UUID.randomUUID().toString();
        String clean = visitorId.replaceAll("[^A-Za-z0-9._-]", "");
        if (clean.isBlank()) return UUID.randomUUID().toString();
        return clean.length() > 64 ? clean.substring(0, 64) : clean;
    }

    private String abbreviate(String visitorId) {
        if (visitorId == null || visitorId.isBlank()) return "anonymous";
        return visitorId.length() <= 8 ? visitorId : visitorId.substring(0, 8) + "…";
    }

    public record VisitRequest(String path, String visitorId) {}
    public record TrafficStats(long totalVisits, long visitsToday, long uniqueVisitors, long uniqueVisitorsToday,
                               String mostVisitedPage, List<DailyVisit> last7Days, List<PageVisit> topPages) {}
    public record DailyVisit(String date, String label, long visits) {}
    public record PageVisit(String path, long visits) {}
    public record RecentVisit(String path, String visitor, Instant visitedAt) {}
}
