package net.consensys.tools.ipfs.ipfsstore.configuration.health;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

@Configuration("HealthCheckScheduler")
@EnableScheduling
@Slf4j
public class HealthCheckScheduler {

  private Map<String, HealthCheck> healthChecks;

  public HealthCheckScheduler() {
    healthChecks = new HashMap<>();
  }

  /**
   * Register the health check service
   * @param name        Name of the service
   * @param healthCheck Bean implementing HealthCheck
   */
  public void registerHealthCheck(String name, HealthCheck healthCheckService) {
    log.debug("register HealthCheck {}", name);
    healthChecks.put(name, healthCheckService);
  }

  @Scheduled(initialDelay = 10000, fixedDelayString = "${ipfs-store.healthcheck.pollInterval:30000}")
  public void checkHealth() {
    healthChecks.values().forEach(HealthCheck::check);
  }
}
