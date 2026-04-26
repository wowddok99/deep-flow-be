package com.deepflow.application.port.out.search;

import com.deepflow.application.session.dto.SessionSharedPayload;

public interface SessionIndexer {

    void index(SessionSharedPayload payload);

    void delete(Long sessionId);
}
