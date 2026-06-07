package com.deepflow.application.crew.feed;

import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.exception.session.SessionNotInCrewException;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.SessionCommentRepository;
import com.deepflow.application.port.out.persistence.SessionReactionRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.SessionTagRepository;
import com.deepflow.application.port.out.persistence.SharedFocusSessionSlice;
import com.deepflow.application.crew.dto.CrewFeedItemInfo;
import com.deepflow.application.crew.dto.CrewSessionDetailInfo;
import com.deepflow.application.crew.dto.SharedFeedCursor;
import com.deepflow.application.crew.dto.SharedFeedSlice;
import com.deepflow.application.session.tag.TagNormalizer;
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
     * 크루 피드를 불투명 커서 기반으로 조회
     *
     * 세션 본문 정보는 함께 로드하고 태그, 리액션, 댓글 수는 세션 ID 단위로 묶어 조회
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
     * 크루 멤버에게 공유 세션의 전체 본문과 상호작용 집계를 제공
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
