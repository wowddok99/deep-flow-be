package com.deepflow.application.session;

import com.deepflow.application.common.SliceResult;
import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.exception.session.SessionNotInCrewException;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.SessionTagRepository;
import com.deepflow.application.session.dto.CrewFeedItemInfo;
import com.deepflow.domain.session.FocusSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrewFeedService {

    private final SessionRepository sessionRepository;
    private final SessionTagRepository tagRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final TagNormalizer tagNormalizer;

    /**
     * 크루 피드 — fetch join 으로 N+1 방지, 태그는 sessionIds 단위 batch.
     * tag 파라미터는 정규화 후 매칭 (사용자가 'JPA' 입력해도 'jpa' 로 검색).
     */
    public SliceResult<CrewFeedItemInfo> getFeed(Long userId, Long crewId, Long cursorId, int size, String tag) {
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new NotCrewMemberException();
        }

        SliceResult<FocusSession> slice;
        if (tag == null || tag.isBlank()) {
            slice = sessionRepository.findSharedByCrewWithCursorFetched(crewId, cursorId, size);
        } else {
            String normalized = tagNormalizer.normalize(tag);
            if (normalized.isBlank()) {
                return new SliceResult<>(List.of(), null, false);
            }
            slice = sessionRepository.findSharedByCrewAndTagWithCursorFetched(crewId, normalized, cursorId, size);
        }

        List<Long> sessionIds = slice.content().stream().map(FocusSession::getId).toList();
        if (sessionIds.isEmpty()) {
            return new SliceResult<>(List.of(), slice.nextCursorId(), slice.hasNext());
        }

        Map<Long, List<String>> tagsBySession = tagRepository.findTagsBySessionIds(sessionIds);

        List<CrewFeedItemInfo> items = slice.content().stream()
                .map(s -> CrewFeedItemInfo.from(s, tagsBySession.getOrDefault(s.getId(), List.of())))
                .toList();
        return new SliceResult<>(items, slice.nextCursorId(), slice.hasNext());
    }

    /**
     * 공유 세션 단건 상세 — 크루 멤버이고, 해당 크루로 공유된 세션이어야 함.
     */
    public CrewFeedItemInfo getSharedSession(Long userId, Long crewId, Long sessionId) {
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new NotCrewMemberException();
        }

        FocusSession session = sessionRepository.findSharedByIdAndCrewWithFetch(sessionId, crewId)
                .orElseThrow(SessionNotInCrewException::new);

        List<String> tags = tagRepository.findAllBySessionId(sessionId).stream()
                .map(t -> t.getTag())
                .toList();

        return CrewFeedItemInfo.from(session, tags);
    }
}
