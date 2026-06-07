package com.deepflow.infra.config;

import com.deepflow.application.crew.dto.CrewHighlightInfo;
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

    // 캐시 버전을 키 앞에 붙여 배포나 응답 구조 변경 시 기존 캐시를 한 번에 우회
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

        // 세션 상세는 수정 가능성이 있어 기본 TTL 설정을 따름
        RedisCacheConfiguration sessionConfig = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(cacheVersion + "::")
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(sessionSerializer))
                .entryTtl(Duration.ofMinutes(cacheTtlMinutes));

        Jackson2JsonRedisSerializer<List<HourlyDistributionInfo>> hourlySerializer =
                new Jackson2JsonRedisSerializer<>(mapper,
                        mapper.getTypeFactory().constructCollectionType(List.class, HourlyDistributionInfo.class));

        // 시간대 분포는 집계 비용이 크고 당일 중 급격히 변하지 않아 하루 동안 재사용
        RedisCacheConfiguration hourlyConfig = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(cacheVersion + "::")
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(hourlySerializer))
                .entryTtl(Duration.ofHours(24));

        Jackson2JsonRedisSerializer<CrewHighlightInfo> highlightSerializer =
                new Jackson2JsonRedisSerializer<>(mapper, CrewHighlightInfo.class);

        // 크루 하이라이트는 공유와 리액션 이벤트에서 무효화되므로 짧은 TTL로 보조 만료
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
