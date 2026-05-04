package com.deepflow.infra.search;

import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.SessionTagRepository;
import com.deepflow.application.port.out.search.SessionIndexer;
import com.deepflow.application.session.dto.SessionSharedPayload;
import com.deepflow.domain.session.FocusSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.outbox.worker.enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchSessionIndexer implements SessionIndexer {

    private final SessionDocumentRepository documentRepository;
    private final SessionRepository sessionRepository;
    private final SessionTagRepository tagRepository;

    @Override
    public void index(SessionSharedPayload payload) {
        Long sessionId = payload.sessionId();
        FocusSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null || session.getSharedCrewId() == null || session.getDeletedAt() != null) {
            log.debug("색인 skip — 세션 미공유 또는 삭제됨: sessionId={}", sessionId);
            documentRepository.deleteById(String.valueOf(sessionId));
            return;
        }

        List<String> tags = tagRepository.findAllBySessionId(sessionId).stream()
                .map(t -> t.getTag())
                .toList();

        SessionDocument doc = SessionDocument.builder()
                .id(String.valueOf(sessionId))
                .crewId(session.getSharedCrewId())
                .ownerUserId(session.getUser() != null ? session.getUser().getId() : null)
                .title(session.getFocusLog() != null ? session.getFocusLog().getTitle() : null)
                .summary(session.getFocusLog() != null ? session.getFocusLog().getSummary() : null)
                .tags(tags)
                .sharedAt(session.getSharedAt() != null ? session.getSharedAt().toString() : null)
                .reactionCount(0)
                .commentCount(0)
                .build();
        documentRepository.save(doc);
    }

    @Override
    public void delete(Long sessionId) {
        documentRepository.deleteById(String.valueOf(sessionId));
    }
}
