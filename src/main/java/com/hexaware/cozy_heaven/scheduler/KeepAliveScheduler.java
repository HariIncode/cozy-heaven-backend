package com.hexaware.cozy_heaven.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@Slf4j
public class KeepAliveScheduler {
	
	@Value("${app.self-ping-url:http://localhost:8080/}")
	private String selfPingUrl;
	
	private final RestTemplate restTemplate = new RestTemplate();
	
	@Scheduled
	public void keepAlive() {
		try {
			String response = restTemplate.getForObject(selfPingUrl, String.class);
			log.info("[KeepAlive] Ping successful → {}", response);
		}catch (Exception e) {
			log.warn("[KeepAlive] Ping failed: {}", e.getMessage());
		}
	}

}
