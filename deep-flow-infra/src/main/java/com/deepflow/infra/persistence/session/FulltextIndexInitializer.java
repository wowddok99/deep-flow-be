package com.deepflow.infra.persistence.session;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * MySQL FULLTEXT INDEX 는 ddl-auto=update 가 자동으로 만들지 않는다.
 * 부팅 시 INFORMATION_SCHEMA 로 존재 여부 확인 후 없으면 CREATE.
 * 운영 적용 시엔 마이그레이션 SQL 로 옮기면 됨 — 본 컴포넌트는 dev/staging 용.
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
