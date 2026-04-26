package com.deepflow.infra.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SessionDocumentRepository extends ElasticsearchRepository<SessionDocument, String> {
}
