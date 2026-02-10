package com.portfolio.backend.service;

import com.portfolio.backend.entity.ProfileView;
import com.portfolio.backend.entity.ProfileShare;
import com.portfolio.backend.entity.User;
import com.portfolio.backend.repository.ProfileViewRepository;
import com.portfolio.backend.repository.ProfileShareRepository;
import com.portfolio.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileViewRepository profileViewRepository;

    @Autowired
    private ProfileShareRepository profileShareRepository;

    @Autowired
    private com.portfolio.backend.repository.ProjectRepository projectRepository;

    @Autowired
    private com.portfolio.backend.repository.SkillRepository skillRepository;

    @Autowired
    private com.portfolio.backend.repository.ExperienceRepository experienceRepository;

    @Autowired
    private com.portfolio.backend.repository.FileMetadataRepository fileMetadataRepository;

    @Autowired
    private com.portfolio.backend.repository.AuditLogRepository auditLogRepository;

    public void recordView(Long portfolioOwnerId, String ip, String userAgent, String referrer) {
        if (portfolioOwnerId == null)
            return;
        User owner = userRepository.findById(portfolioOwnerId).orElse(null);
        if (owner != null) {
            ProfileView view = new ProfileView();
            view.setPortfolioOwner(owner);
            view.setIpAddress(ip);
            view.setUserAgent(userAgent);
            view.setReferrerUrl(referrer);
            profileViewRepository.save(view);
        }
    }

    public void recordShare(Long portfolioOwnerId, String platform) {
        if (portfolioOwnerId == null)
            return;
        User owner = userRepository.findById(portfolioOwnerId).orElse(null);
        if (owner != null) {
            ProfileShare share = new ProfileShare();
            share.setPortfolioOwner(owner);
            share.setPlatform(platform);
            profileShareRepository.save(share);
        }
    }

    public com.portfolio.backend.dto.SuperAdminMetricsDTO getSuperAdminMetrics() {
        com.portfolio.backend.dto.SuperAdminMetricsDTO globalStats = new com.portfolio.backend.dto.SuperAdminMetricsDTO();

        // 1. User Metrics
        com.portfolio.backend.dto.SuperAdminMetricsDTO.UserMetrics userMetrics = new com.portfolio.backend.dto.SuperAdminMetricsDTO.UserMetrics();
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getStatus() == com.portfolio.backend.entity.UserStatus.ACTIVE).count();
        long completedProfiles = userRepository.findAll().stream().filter(u -> u.isRegistrationCompleted()).count();

        Map<String, Long> funnel = new HashMap<>();
        funnel.put("Total", totalUsers);
        funnel.put("Completed", completedProfiles);
        funnel.put("Active", activeUsers);
        userMetrics.setFunnel(funnel);

        // Retention (Mock logic for now: User created last week vs this week)
        userMetrics.setRetentionRate(85.5); // Example static value for now

        // Geography (Aggregation from ProfileViews)
        Map<String, Long> geo = new HashMap<>();
        profileViewRepository.findAll().forEach(v -> {
            if (v.getCountry() != null) {
                geo.put(v.getCountry(), geo.getOrDefault(v.getCountry(), 0L) + 1);
            }
        });
        userMetrics.setGeography(geo);

        // Activity Heatmap (7x24 Matrix)
        int[][] heatmap = new int[7][24];
        profileViewRepository.findAll().forEach(v -> {
            if (v.getViewedAt() != null) {
                int day = v.getViewedAt().getDayOfWeek().getValue() - 1; // 0-6
                int hour = v.getViewedAt().getHour(); // 0-23
                heatmap[day][hour]++;
            }
        });
        userMetrics.setActivityHeatmap(heatmap);
        globalStats.setUserMetrics(userMetrics);

        // 2. Engagement Metrics
        com.portfolio.backend.dto.SuperAdminMetricsDTO.EngagementMetrics engMetrics = new com.portfolio.backend.dto.SuperAdminMetricsDTO.EngagementMetrics();
        long totalViews = profileViewRepository.count();
        long totalShares = profileShareRepository.count();
        engMetrics.setSocialShareConversion(totalViews > 0 ? (double) totalShares / totalViews * 100 : 0);
        engMetrics.setAvgSessionDuration(124.5); // Mock
        engMetrics.setBounceRate(42.3); // Mock

        // Leaderboard
        List<com.portfolio.backend.dto.SuperAdminMetricsDTO.PortfolioLeaderboardEntry> leaderboard = new ArrayList<>();
        userRepository.findAll().forEach(u -> {
            long views = profileViewRepository.countByPortfolioOwnerId(u.getId());
            long shares = profileShareRepository.countByPortfolioOwnerId(u.getId());
            if (views > 0) {
                com.portfolio.backend.dto.SuperAdminMetricsDTO.PortfolioLeaderboardEntry entry = new com.portfolio.backend.dto.SuperAdminMetricsDTO.PortfolioLeaderboardEntry();
                entry.setUserId(u.getId());
                entry.setUsername(u.getUsername());
                entry.setFullName(u.getFullName());
                entry.setViews(views);
                entry.setShares(shares);
                leaderboard.add(entry);
            }
        });
        leaderboard.sort((a, b) -> Long.compare(b.getViews(), a.getViews()));
        engMetrics.setLeaderboard(leaderboard.stream().limit(10).toList());
        globalStats.setEngagementMetrics(engMetrics);

        // 3. Content Health
        com.portfolio.backend.dto.SuperAdminMetricsDTO.ContentHealth contentHealth = new com.portfolio.backend.dto.SuperAdminMetricsDTO.ContentHealth();
        contentHealth.setCompletionRate((double) completedProfiles / (totalUsers > 0 ? totalUsers : 1) * 100);

        Map<String, Long> freshness = new HashMap<>();
        freshness.put("Recent (<1w)", 0L);
        freshness.put("Stale (>1w)", 0L); // Simplified logic
        globalStats.setContentHealth(contentHealth);

        // 4. System Health
        com.portfolio.backend.dto.SuperAdminMetricsDTO.SystemHealth sysHealth = new com.portfolio.backend.dto.SuperAdminMetricsDTO.SystemHealth();
        sysHealth.setActiveUsers(activeUsers);
        sysHealth.setActiveUsersSparkline(List.of(12, 15, 14, 18, 20, 22, 21, 25)); // Mock
        sysHealth.setTotalStorageUsed(0L); // Should iterate fileMetadata
        globalStats.setSystemHealth(sysHealth);

        // Actionable Insights
        List<com.portfolio.backend.dto.SuperAdminMetricsDTO.ActionableInsight> insights = new ArrayList<>();
        userRepository.findAll().stream()
                .filter(u -> u.getLastActiveAt() != null
                        && u.getLastActiveAt().isBefore(LocalDateTime.now().minusDays(30)))
                .limit(5)
                .forEach(u -> {
                    com.portfolio.backend.dto.SuperAdminMetricsDTO.ActionableInsight ins = new com.portfolio.backend.dto.SuperAdminMetricsDTO.ActionableInsight();
                    ins.setType("STALE_USER");
                    ins.setMessage("User " + u.getUsername() + " has been inactive for 30+ days.");
                    ins.setTargetId(u.getId());
                    ins.setActionLabel("Re-engage");
                    insights.add(ins);
                });
        globalStats.setInsights(insights);

        return globalStats;
    }

    public List<Map<String, Object>> getDailyStats(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days).withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<ProfileView> views = profileViewRepository.findByPortfolioOwnerIdAndViewedAtAfter(userId, since);
        List<ProfileShare> shares = profileShareRepository.findByPortfolioOwnerIdAndSharedAtAfter(userId, since);

        Map<LocalDate, Integer> viewCounts = new TreeMap<>();
        Map<LocalDate, Integer> shareCounts = new TreeMap<>();

        // Initialize last X days
        for (int i = 0; i <= days; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            viewCounts.put(date, 0);
            shareCounts.put(date, 0);
        }

        if (views != null) {
            for (ProfileView v : views) {
                if (v.getViewedAt() != null) {
                    LocalDate date = v.getViewedAt().toLocalDate();
                    viewCounts.put(date, viewCounts.getOrDefault(date, 0) + 1);
                    if (!shareCounts.containsKey(date)) {
                        shareCounts.put(date, 0);
                    }
                }
            }
        }

        if (shares != null) {
            for (ProfileShare s : shares) {
                if (s.getSharedAt() != null) {
                    LocalDate date = s.getSharedAt().toLocalDate();
                    shareCounts.put(date, shareCounts.getOrDefault(date, 0) + 1);
                    if (!viewCounts.containsKey(date)) {
                        viewCounts.put(date, 0);
                    }
                }
            }
        }

        List<Map<String, Object>> stats = new ArrayList<>();
        for (LocalDate date : viewCounts.keySet()) {
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", date.toString());
            dayStat.put("views", viewCounts.getOrDefault(date, 0));
            dayStat.put("shares", shareCounts.getOrDefault(date, 0));
            stats.add(dayStat);
        }

        return stats;
    }
}
