package com.aiqa.analytics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class SiteAnalyticsController {
    private final SiteVisitRepository visits;

    public SiteAnalyticsController(SiteVisitRepository visits) {
        this.visits = visits;
    }

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

        Map<String, Long> pageCounts = all.stream()
                .collect(Collectors.groupingBy(SiteVisit::getPath, Collectors.counting()));
        String mostVisitedPage = pageCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("—");

        Map<LocalDate, Long> dailyCounts = visits.findAllByVisitedAtAfterOrderByVisitedAtAsc(sevenDayStart).stream()
                .collect(Collectors.groupingBy(v -> v.getVisitedAt().atZone(zone).toLocalDate(), Collectors.counting()));

        List<DailyVisit> last7Days = new ArrayList<>();
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("EEE");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            last7Days.add(new DailyVisit(date.toString(), labelFormat.format(date), dailyCounts.getOrDefault(date, 0L)));
        }

        return new TrafficStats(total, todayVisits, uniqueVisitors, mostVisitedPage, last7Days);
    }

    public record TrafficStats(long totalVisits, long visitsToday, long uniqueVisitors,
                               String mostVisitedPage, List<DailyVisit> last7Days) {}
    public record DailyVisit(String date, String label, long visits) {}
}
