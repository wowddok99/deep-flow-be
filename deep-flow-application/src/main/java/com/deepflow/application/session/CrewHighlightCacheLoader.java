package com.deepflow.application.session;

import com.deepflow.application.port.out.persistence.SessionReactionRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.SessionTagRepository;
import com.deepflow.application.session.dto.CrewHighlightInfo;
import com.deepflow.application.session.dto.HighlightItemInfo;
import com.deepflow.application.session.dto.HighlightMode;
import com.deepflow.domain.session.FocusSession;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.deepflow.application.session.CrewHighlightService.GROWING_CARD_LIMIT;
import static com.deepflow.application.session.CrewHighlightService.MATURE_THRESHOLD;
import static com.deepflow.application.session.CrewHighlightService.RECENT_WINDOW;

/**
 * 외부에서 직접 호출 금지 — CrewHighlightService 가 권한 체크 후 본 빈을 호출.
 * @Cacheable AOP 가 적용되려면 다른 빈 경계를 통해 호출되어야 함 (self-invocation 문제 회피).
 */
@Component
@RequiredArgsConstructor
public class CrewHighlightCacheLoader {

    private final SessionRepository sessionRepository;
    private final SessionReactionRepository reactionRepository;
    private final SessionTagRepository tagRepository;

    @Cacheable(value = "crewHighlight", key = "#crewId")
    @Transactional(readOnly = true)
    public CrewHighlightInfo load(Long crewId) {
        LocalDateTime since = LocalDateTime.now().minus(RECENT_WINDOW);
        int recentCount = sessionRepository.countSharedSince(crewId, since);

        HighlightMode mode = decideMode(recentCount);
        List<HighlightItemInfo> items = switch (mode) {
            case EMPTY -> List.of();
            case GROWING -> buildGrowingItems(crewId, since);
            case MATURE -> buildMatureItems(crewId, since);
        };

        return new CrewHighlightInfo(mode, items, recentCount, MATURE_THRESHOLD);
    }

    private HighlightMode decideMode(int recentCount) {
        if (recentCount == 0) return HighlightMode.EMPTY;
        if (recentCount < MATURE_THRESHOLD) return HighlightMode.GROWING;
        return HighlightMode.MATURE;
    }

    private List<HighlightItemInfo> buildGrowingItems(Long crewId, LocalDateTime since) {
        List<FocusSession> recents = sessionRepository.findRecentSharedCards(crewId, since, GROWING_CARD_LIMIT);
        return recents.stream()
                .map(s -> HighlightItemInfo.recent(
                        s.getId(),
                        s.getFocusLog() != null ? s.getFocusLog().getTitle() : null,
                        s.getUser() != null ? s.getUser().getName() : "알수없음"))
                .toList();
    }

    private List<HighlightItemInfo> buildMatureItems(Long crewId, LocalDateTime since) {
        List<HighlightItemInfo> items = new ArrayList<>(3);

        Optional<FocusSession> hot = sessionRepository.findHottestSharedSince(crewId, since);
        hot.ifPresent(s -> {
            int reactions = reactionRepository.countBySessionIds(List.of(s.getId())).getOrDefault(s.getId(), 0);
            long hours = Math.max(Duration.between(s.getSharedAt(), LocalDateTime.now()).toHours(), 1L);
            double score = (double) reactions / hours;
            items.add(HighlightItemInfo.hot(
                    s.getId(),
                    s.getFocusLog() != null ? s.getFocusLog().getTitle() : null,
                    s.getUser() != null ? s.getUser().getName() : "알수없음",
                    score));
        });

        Optional<FocusSession> longest = sessionRepository.findLongestSharedSince(crewId, since);
        longest.ifPresent(s -> items.add(HighlightItemInfo.longSession(
                s.getId(),
                s.getFocusLog() != null ? s.getFocusLog().getTitle() : null,
                s.getUser() != null ? s.getUser().getName() : "알수없음",
                s.getDurationSeconds())));

        List<SessionTagRepository.TagCount> popular = tagRepository.findPopularTagsByCrewId(crewId, 1);
        popular.stream().findFirst().ifPresent(tc ->
                items.add(HighlightItemInfo.tag(tc.tag(), (int) tc.count())));

        return items;
    }
}
