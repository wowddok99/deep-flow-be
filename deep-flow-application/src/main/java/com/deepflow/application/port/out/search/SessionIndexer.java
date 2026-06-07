package com.deepflow.application.port.out.search;

import com.deepflow.application.session.share.dto.SessionSharedPayload;

public interface SessionIndexer {

    void index(SessionSharedPayload payload);

    void delete(Long sessionId);
}
