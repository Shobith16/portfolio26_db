package com.portfolio.backend.service;

import com.portfolio.backend.entity.ProfileView;
import com.portfolio.backend.entity.User;
import com.portfolio.backend.repository.ProfileViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitorTrackingService {

    private final ProfileViewRepository profileViewRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public void trackView(User user, String ipAddress, String userAgent, String referrer) {
        ProfileView view = new ProfileView();
        view.setPortfolioOwner(user);
        view.setIpAddress(ipAddress);
        view.setUserAgent(userAgent);
        view.setReferrerUrl(referrer);

        // 1. Parse User Agent (Simple Logic)
        parseUserAgent(view, userAgent);

        // 2. Fetch Geolocation
        fetchGeolocation(view, ipAddress);

        profileViewRepository.save(view);
    }

    private void parseUserAgent(ProfileView view, String ua) {
        if (ua == null)
            return;

        String uaLower = ua.toLowerCase();

        // Browser
        if (uaLower.contains("chrome"))
            view.setBrowser("Chrome");
        else if (uaLower.contains("firefox"))
            view.setBrowser("Firefox");
        else if (uaLower.contains("safari"))
            view.setBrowser("Safari");
        else if (uaLower.contains("edge"))
            view.setBrowser("Edge");
        else
            view.setBrowser("Other");

        // OS
        if (uaLower.contains("windows"))
            view.setOs("Windows");
        else if (uaLower.contains("mac os"))
            view.setOs("macOS");
        else if (uaLower.contains("linux"))
            view.setOs("Linux");
        else if (uaLower.contains("android"))
            view.setOs("Android");
        else if (uaLower.contains("iphone") || uaLower.contains("ipad"))
            view.setOs("iOS");
        else
            view.setOs("Other");

        // Device Type
        if (uaLower.contains("mobile") || uaLower.contains("android") || uaLower.contains("iphone")) {
            view.setDeviceType("Mobile");
        } else if (uaLower.contains("tablet") || uaLower.contains("ipad")) {
            view.setDeviceType("Tablet");
        } else {
            view.setDeviceType("Desktop");
        }
    }

    private void fetchGeolocation(ProfileView view, String ip) {
        if (ip == null || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
            view.setCity("Localhost");
            view.setCountry("Localhost");
            return;
        }

        try {
            String url = "http://ip-api.com/json/" + ip;
            org.springframework.core.ParameterizedTypeReference<Map<String, Object>> typeRef = new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
            };
            org.springframework.http.ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(url,
                    org.springframework.http.HttpMethod.GET, null, typeRef);
            Map<String, Object> response = responseEntity.getBody();
            if (response != null && "success".equals(response.get("status"))) {
                view.setCity((String) response.get("city"));
                view.setCountry((String) response.get("country"));
            }
        } catch (Exception e) {
            log.error("Error fetching geolocation for IP: {}", ip, e);
        }
    }

    public void updateDuration(Long viewId, Integer duration) {
        profileViewRepository.findById(viewId).ifPresent(view -> {
            view.setTimeOnPage(duration);
            profileViewRepository.save(view);
        });
    }
}
