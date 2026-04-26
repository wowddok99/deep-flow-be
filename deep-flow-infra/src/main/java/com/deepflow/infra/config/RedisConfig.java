package com.deepflow.infra.config;

import com.deepflow.application.session.dto.CrewHighlightInfo;
import com.deepflow.application.session.dto.SessionDetailInfo;
import com.deepflow.application.stats.dto.HourlyDistributionInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${app.cache.ttl-minutes:60}")
    private long cacheTtlMinutes;

    @Value("${app.cache.version:v1}")
    private String cacheVersion;

    private ObjectMapper cacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = cacheObjectMapper();

        Jackson2JsonRedisSerializer<SessionDetailInfo> sessionSerializer =
                new Jackson2JsonRedisSerializer<>(mapper, SessionDetailInfo.class);

        RedisCacheConfiguration sessionConfig = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(cacheVersion + "::")
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(sessionSerializer))
                .entryTtl(Duration.ofMinutes(cacheTtlMinutes));

        Jackson2JsonRedisSerializer<List<HourlyDistributionInfo>> hourlySerializer =
                new Jackson2JsonRedisSerializer<>(mapper,
                        mapper.getTypeFactory().constructCollectionType(List.class, HourlyDistributionInfo.class));

        RedisCacheConfiguration hourlyConfig = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(cacheVersion + "::")
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(hourlySerializer))
                .entryTtl(Duration.ofHours(24));

        Jackson2JsonRedisSerializer<CrewHighlightInfo> highlightSerializer =
                new Jackson2JsonRedisSerializer<>(mapper, CrewHighlightInfo.class);

        RedisCacheConfiguration highlightConfig = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(cacheVersion + "::")
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(highlightSerializer))
                .entryTtl(Duration.ofHours(1));

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration("sessions", sessionConfig)
                .withCacheConfiguration("hourlyDistribution", hourlyConfig)
                .withCacheConfiguration("crewHighlight", highlightConfig)
                .build();
    }
}
