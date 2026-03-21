package com.deepflow.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.serializer.SerializationException;

@Slf4j
@Configuration
public class CacheConfig implements CachingConfigurer {

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                logByExceptionType("조회", e, cache, key);
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                logByExceptionType("저장", e, cache, key);
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                logByExceptionType("삭제", e, cache, key);
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                logByExceptionType("초기화", e, cache, null);
            }

            private void logByExceptionType(String operation, RuntimeException e, Cache cache, Object key) {
                if (e instanceof RedisConnectionFailureException) {
                    log.warn("캐시 {} 실패, Redis 연결 불가: cache={}, key={}",
                            operation, cache.getName(), key);
                } else if (e instanceof SerializationException) {
                    log.error("캐시 {} 실패, 직렬화 오류: cache={}, key={}",
                            operation, cache.getName(), key, e);
                } else {
                    log.error("캐시 {} 실패: cache={}, key={}, 예외={}",
                            operation, cache.getName(), key, e.getClass().getSimpleName(), e);
                }
            }
        };
    }
}
