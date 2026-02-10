package com.portfolio.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SuperAdminMetricsDTO {
    private UserMetrics userMetrics;
    private EngagementMetrics engagementMetrics;
    private ContentHealth contentHealth;
    private SystemHealth systemHealth;
    private List<ActionableInsight> insights;

    @Data
    public static class UserMetrics {
        private double retentionRate; // WoW
        private Map<String, Long> funnel; // Started, Completed, Active
        private Map<String, Long> geography; // Country to Count
        private int[][] activityHeatmap; // 7x24 matrix
    }

    @Data
    public static class EngagementMetrics {
        private double avgSessionDuration; // in seconds
        private double bounceRate; // percentage
        private List<PortfolioLeaderboardEntry> leaderboard;
        private double socialShareConversion; // Views -> Shares %
    }

    @Data
    public static class ContentHealth {
        private double completionRate;
        private Map<String, Long> freshness; // <1w, <1m, >1m
        private Map<String, Long> mediaUsage; // images, docs, etc.
    }

    @Data
    public static class SystemHealth {
        private long activeUsers;
        private List<Integer> activeUsersSparkline;
        private double errorRate;
        private long totalStorageUsed; // bytes
        private Map<String, Double> latencies; // p50, p95, p99
    }

    @Data
    public static class PortfolioLeaderboardEntry {
        private Long userId;
        private String username;
        private String fullName;
        private long views;
        private long shares;
    }

    @Data
    public static class ActionableInsight {
        private String type; // STALE_USER, INCOMPLETE_PROFILE, etc.
        private String message;
        private Long targetId;
        private String actionLabel;
    }
}
