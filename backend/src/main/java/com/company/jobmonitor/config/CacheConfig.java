package com.company.jobmonitor.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for caching functionality using Caffeine cache. Provides performance optimization
 * for frequently accessed data.
 *
 * @author JobMonitor Development Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

  /**
   * Creates and configures the cache manager with Caffeine implementation.
   *
   * @return CacheManager configured with optimized cache settings
   */
  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    cacheManager.setCaffeine(caffeineCacheBuilder());
    return cacheManager;
  }

  /**
   * Builds Caffeine cache with performance-optimized settings.
   *
   * @return Caffeine builder with configured parameters
   */
  @Bean
  public Caffeine<Object, Object> caffeineCacheBuilder() {
    return Caffeine.newBuilder()
        .maximumSize(1000) // Maximum cache entries
        .expireAfterWrite(10, TimeUnit.MINUTES) // TTL for write operations
        .expireAfterAccess(5, TimeUnit.MINUTES) // TTL for access operations
        .recordStats(); // Enable statistics for monitoring
  }
}
