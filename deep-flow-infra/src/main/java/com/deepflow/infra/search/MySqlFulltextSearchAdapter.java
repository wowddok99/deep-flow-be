package com.deepflow.infra.search;

import com.deepflow.application.port.out.search.SessionSearchPort;
import com.deepflow.application.session.dto.SearchResultInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(name = "app.search.engine", havingValue = "mysql", matchIfMissing = true)
public class MySqlFulltextSearchAdapter implements SessionSearchPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<SearchResultInfo> search(Long crewId, String q, SearchType type, int offset, int size) {
        return type == SearchType.TAG
                ? searchByTag(crewId, q, offset, size)
                : searchBySession(crewId, q, offset, size);
    }

    @SuppressWarnings("unchecked")
    private List<SearchResultInfo> searchBySession(Long crewId, String q, int offset, int size) {
        // 제목과 요약의 전문 검색 점수에 태그 매칭 점수를 더해 세션 검색 관련도를 보정
        String sql = """
                SELECT fs.id AS sessionId,
                       fl.title AS title,
                       fl.summary AS summary,
                       u.id AS userId,
                       u.name AS userName,
                       fs.shared_at AS sharedAt,
                       (
                         MATCH(fl.title, fl.summary) AGAINST(:q IN BOOLEAN MODE)
                         + COALESCE(
                             (SELECT COUNT(*) FROM session_tag st
                              WHERE st.session_id = fs.id
                                AND MATCH(st.tag) AGAINST(:q IN BOOLEAN MODE)),
                             0
                         )
                       ) AS score
                FROM focus_session fs
                JOIN focus_log fl ON fl.id = fs.focus_log_id
                JOIN users u ON u.id = fs.user_id
                WHERE fs.shared_crew_id = :crewId
                  AND fs.deleted_at IS NULL
                  AND (
                    MATCH(fl.title, fl.summary) AGAINST(:q IN BOOLEAN MODE)
                    OR EXISTS (
                        SELECT 1 FROM session_tag st
                        WHERE st.session_id = fs.id
                          AND MATCH(st.tag) AGAINST(:q IN BOOLEAN MODE)
                    )
                  )
                ORDER BY score DESC, fs.id DESC
                LIMIT :size OFFSET :offset
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("crewId", crewId)
                .setParameter("q", q)
                .setParameter("size", size)
                .setParameter("offset", offset)
                .getResultList();

        return rows.stream().map(this::toResultWithTagFetch).toList();
    }

    @SuppressWarnings("unchecked")
    private List<SearchResultInfo> searchByTag(Long crewId, String q, int offset, int size) {
        // 태그 검색은 자동완성/필터와 같은 정규화 값을 쓰므로 정확히 일치하는 태그만 조회
        String sql = """
                SELECT fs.id AS sessionId,
                       fl.title AS title,
                       fl.summary AS summary,
                       u.id AS userId,
                       u.name AS userName,
                       fs.shared_at AS sharedAt
                FROM focus_session fs
                JOIN focus_log fl ON fl.id = fs.focus_log_id
                JOIN users u ON u.id = fs.user_id
                WHERE fs.shared_crew_id = :crewId
                  AND fs.deleted_at IS NULL
                  AND EXISTS (
                      SELECT 1 FROM session_tag st
                      WHERE st.session_id = fs.id
                        AND st.tag = :q
                  )
                ORDER BY fs.shared_at DESC, fs.id DESC
                LIMIT :size OFFSET :offset
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("crewId", crewId)
                .setParameter("q", q)
                .setParameter("size", size)
                .setParameter("offset", offset)
                .getResultList();

        return rows.stream().map(row -> toResult(row, 1.0)).toList();
    }

    private SearchResultInfo toResultWithTagFetch(Object[] row) {
        double score = ((Number) row[6]).doubleValue();
        return toResult(row, score);
    }

    @SuppressWarnings("unchecked")
    private SearchResultInfo toResult(Object[] row, double score) {
        Long sessionId = ((Number) row[0]).longValue();
        String title = (String) row[1];
        String summary = (String) row[2];
        Long userId = row[3] == null ? null : ((Number) row[3]).longValue();
        String userName = (String) row[4];
        LocalDateTime sharedAt = row[5] == null ? null : ((Timestamp) row[5]).toLocalDateTime();

        List<String> tags = em.createNativeQuery("SELECT tag FROM session_tag WHERE session_id = :sid")
                .setParameter("sid", sessionId)
                .getResultList();

        String summaryPreview = (summary == null || summary.isBlank()) ? null
                : (summary.length() <= 100 ? summary : summary.substring(0, 100));

        return new SearchResultInfo(
                sessionId,
                title,
                summaryPreview,
                userName,
                userId,
                new ArrayList<>(tags),
                sharedAt,
                score
        );
    }
}
