package com.deepflow.application.session;

import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.exception.session.SessionNotInCrewException;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.SessionCommentRepository;
import com.deepflow.application.port.out.persistence.SessionReactionRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.SessionTagRepository;
import com.deepflow.application.port.out.persistence.SharedFocusSessionSlice;
import com.deepflow.application.session.dto.CrewFeedItemInfo;
import com.deepflow.application.session.dto.CrewSessionDetailInfo;
import com.deepflow.application.session.dto.SharedFeedCursor;
import com.deepflow.application.session.dto.SharedFeedSlice;
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
    private final SessionReactionRepository reactionRepository;
    private final SessionCommentRepository commentRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final TagNormalizer tagNormalizer;

    /**
     * 크루 피드 — fetch join 으로 N+1 방지, 태그는 sessionIds 단위 batch.
     * tag 파라미터는 정규화 후 매칭 (사용자가 'JPA' 입력해도 'jpa' 로 검색).
     * cursor 는 (sharedAt, id) 복합 커서를 인코딩한 불투명 토큰. null 또는 빈 값이면 첫 페이지.
     */
    public SharedFeedSlice<CrewFeedItemInfo> getFeed(Long userId, Long crewId, String cursorToken, int size, String tag) {
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new NotCrewMemberException();
        }

        SharedFeedCursor cursor = SharedFeedCursor.decode(cursorToken);

        SharedFocusSessionSlice slice;
        if (tag == null || tag.isBlank()) {
            slice = sessionRepository.findSharedByCrewWithCursorFetched(crewId, cursor, size);
        } else {
            String normalized = tagNormalizer.normalize(tag);
            if (normalized.isBlank()) {
                return new SharedFeedSlice<>(List.of(), null, false);
            }
            slice = sessionRepository.findSharedByCrewAndTagWithCursorFetched(crewId, normalized, cursor, size);
        }

        List<Long> sessionIds = slice.content().stream().map(FocusSession::getId).toList();
        if (sessionIds.isEmpty()) {
            return new SharedFeedSlice<>(List.of(), null, slice.hasNext());
        }

        Map<Long, List<String>> tagsBySession = tagRepository.findTagsBySessionIds(sessionIds);
        Map<Long, Integer> reactionCounts = reactionRepository.countBySessionIds(sessionIds);
        Map<Long, Integer> commentCounts = commentRepository.countBySessionIds(sessionIds);

        List<CrewFeedItemInfo> items = slice.content().stream()
                .map(s -> CrewFeedItemInfo.from(
                        s,
                        tagsBySession.getOrDefault(s.getId(), List.of()),
                        reactionCounts.getOrDefault(s.getId(), 0),
                        commentCounts.getOrDefault(s.getId(), 0)))
                .toList();

        String nextCursor = slice.hasNext()
                ? new SharedFeedCursor(slice.nextSharedAt(), slice.nextId()).encode()
                : null;

        return new SharedFeedSlice<>(items, nextCursor, slice.hasNext());
    }

    /**
     * 공유 세션 단건 상세 — 크루 멤버이고, 해당 크루로 공유된 세션이어야 함.
     * 본문(content) 포함. 크루 멤버이면 풀 본문을 볼 수 있다는 정책.
     */
    public CrewSessionDetailInfo getSharedSession(Long userId, Long crewId, Long sessionId) {
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new NotCrewMemberException();
        }

        FocusSession session = sessionRepository.findSharedByIdAndCrewWithFetch(sessionId, crewId)
                .orElseThrow(SessionNotInCrewException::new);

        List<String> tags = tagRepository.findAllBySessionId(sessionId).stream()
                .map(t -> t.getTag())
                .toList();
        int reactionCount = reactionRepository.countBySessionIds(List.of(sessionId)).getOrDefault(sessionId, 0);
        int commentCount = commentRepository.countBySessionIds(List.of(sessionId)).getOrDefault(sessionId, 0);

        return CrewSessionDetailInfo.from(session, tags, reactionCount, commentCount);
    }
}
