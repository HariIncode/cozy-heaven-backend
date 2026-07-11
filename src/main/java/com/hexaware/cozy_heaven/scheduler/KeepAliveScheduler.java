package com.hexaware.cozy_heaven.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeepAliveScheduler {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveScheduler.class);

    @Value("${app.self-ping-url:http://localhost:8080/}")
    private String selfPingUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Runs every 12 minutes (fixed rate in milliseconds)
    @Scheduled(cron = "0 0/12 * * * *")
    public void keepAlive() {
        try {
            String response = restTemplate.getForObject(selfPingUrl, String.class);
            logger.info("[KeepAlive] Ping successful → {}", response);
        } catch (Exception e) {
            logger.warn("[KeepAlive] Ping failed: {}", e.getMessage());
        }
    }
}