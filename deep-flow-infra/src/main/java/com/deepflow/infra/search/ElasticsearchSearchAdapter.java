package com.deepflow.infra.search;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.deepflow.application.port.out.search.SessionSearchPort;
import com.deepflow.application.session.dto.SearchResultInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.search.engine", havingValue = "es")
public class ElasticsearchSearchAdapter implements SessionSearchPort {

    private final ElasticsearchOperations operations;

    @Override
    public List<SearchResultInfo> search(Long crewId, String q, SearchType type, int offset, int size) {
        Query baseQuery = (type == SearchType.TAG)
                ? Query.of(b -> b.term(t -> t.field("tags").value(q)))
                : Query.of(b -> b.multiMatch(m -> m.fields("title", "summary", "tags").query(q)));

        Query crewFilter = Query.of(b -> b.term(t -> t.field("crewId").value(crewId)));

        Query combined = Query.of(b -> b.bool(bool -> bool
                .must(baseQuery)
                .filter(crewFilter)));

        NativeQuery nq = NativeQuery.builder()
                .withQuery(combined)
                .withPageable(org.springframework.data.domain.PageRequest.of(offset / Math.max(size, 1), Math.max(size, 1)))
                .withSort(s -> s.field(f -> f.field("_score").order(SortOrder.Desc)))
                .build();

        SearchHits<SessionDocument> hits = operations.search(nq, SessionDocument.class);

        return hits.getSearchHits().stream()
                .map(h -> {
                    SessionDocument d = h.getContent();
                    String preview = (d.getSummary() == null || d.getSummary().isBlank()) ? null
                            : (d.getSummary().length() <= 100 ? d.getSummary() : d.getSummary().substring(0, 100));
                    return new SearchResultInfo(
                            Long.parseLong(d.getId()),
                            d.getTitle(),
                            preview,
                            null,
                            d.getOwnerUserId(),
                            d.getTags(),
                            d.getSharedAt() != null ? java.time.LocalDateTime.parse(d.getSharedAt()) : null,
                            h.getScore()
                    );
                })
                .toList();
    }
}
