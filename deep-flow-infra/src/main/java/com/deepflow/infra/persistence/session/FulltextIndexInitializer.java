package com.deepflow.infra.persistence.session;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * local, test 환경에서 MySQL FULLTEXT INDEX 누락 시 부팅 시점에 보완
 *
 * ddl-auto=update 가 FULLTEXT INDEX 를 만들지 않으므로 운영에서는 마이그레이션으로 대체 필요
 */
@Slf4j
@Component
@Profile({"local", "test"})
public class FulltextIndexInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(String... args) {
        // 로컬과 테스트는 마이그레이션 없이도 검색 기능을 바로 확인할 수 있도록 인덱스 보정
        ensureIndex("focus_log", "ft_focus_log_title_summary",
                "ALTER TABLE focus_log ADD FULLTEXT INDEX ft_focus_log_title_summary (title, summary) WITH PARSER ngram");
        ensureIndex("session_tag", "ft_session_tag_tag",
                "ALTER TABLE session_tag ADD FULLTEXT INDEX ft_session_tag_tag (tag) WITH PARSER ngram");
    }

    private void ensureIndex(String tableName, String indexName, String createSql) {
        Number count = (Number) em.createNativeQuery("""
                        SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = :t
                          AND INDEX_NAME = :i
                        """)
                .setParameter("t", tableName)
                .setParameter("i", indexName)
                .getSingleResult();
        if (count.intValue() > 0) {
            log.debug("FULLTEXT index already exists: {}.{}", tableName, indexName);
            return;
        }
        em.createNativeQuery(createSql).executeUpdate();
        log.info("FULLTEXT index created: {}.{}", tableName, indexName);
    }
}
