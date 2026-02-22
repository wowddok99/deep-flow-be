package com.deepflow.application.ai;

import com.deepflow.domain.log.FocusLog;
import com.deepflow.domain.log.FocusLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSummaryService {

    private final Optional<AiClient> aiClient;
    private final FocusLogRepository focusLogRepository;

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    @Transactional
    public void generateSummary(Long sessionId) {
        if (!aiEnabled) {
            log.debug("AI summary is disabled, skipping for session {}", sessionId);
            return;
        }

        if (aiClient.isEmpty()) {
            log.warn("No AiClient implementation found, skipping summary for session {}", sessionId);
            return;
        }

        try {
            FocusLog focusLog = focusLogRepository.findByFocusSessionId(sessionId)
                    .orElse(null);

            if (focusLog == null || focusLog.getContent() == null || focusLog.getContent().isBlank()) {
                log.debug("No content to summarize for session {}", sessionId);
                return;
            }

            String summary = aiClient.get().summarize(focusLog.getContent());

            if (summary != null && !summary.isBlank()) {
                focusLog.updateAiSummary(summary);
                log.info("AI summary generated for session {}", sessionId);
            }
        } catch (Exception e) {
            log.error("Failed to generate AI summary for session {}", sessionId, e);
        }
    }
}
