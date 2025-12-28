package com.deepflow.api.service.session;

import com.deepflow.api.dto.*;
import com.deepflow.core.domain.session.FocusSession;
import com.deepflow.api.exception.ResourceNotFoundException;
import com.deepflow.core.repository.session.FocusSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import com.deepflow.api.service.log.FocusLogService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final FocusSessionRepository sessionRepository;
    private final FocusLogService focusLogService;

    @Transactional
    public SessionResponse startSession() {
        LocalDateTime now = LocalDateTime.now();
        FocusSession session = FocusSession.create(now);

        FocusSession savedSession = sessionRepository.save(session);
        return SessionResponse.from(savedSession);
    }

    public List<SessionSummaryResponse> getAllSessions() {
        return sessionRepository.findAllByOrderByStartTimeDesc().stream()
                .map(SessionSummaryResponse::from)
                .toList();
    }

    public SessionDetailResponse getSessionDetail(Long id) {
        FocusSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        return SessionDetailResponse.from(session);
    }

    @Transactional
    public void updateLog(Long id, LogUpdateRequest request) {
        FocusSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        String contentJson = null;
        try {
            contentJson = objectMapper.writeValueAsString(request.content());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize content", e);
        }

        focusLogService.updateLogDetails(
                session.getFocusLog(),
                contentJson,
                request.summary(),
                request.tags(),
                request.imageUrls());
    }

    @Transactional
    public void stopSession(Long id) {
        FocusSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        session.stop(LocalDateTime.now());
    }

    @Transactional
    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Session not found with id: " + id);
        }
        sessionRepository.deleteById(id);
    }
}
