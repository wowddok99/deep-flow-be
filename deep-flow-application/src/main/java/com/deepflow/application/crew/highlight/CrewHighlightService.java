package com.deepflow.application.crew.highlight;

import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.crew.dto.CrewHighlightInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrewHighlightService {

    public static final int MATURE_THRESHOLD = 30;
    public static final Duration RECENT_WINDOW = Duration.ofDays(7);
    public static final int GROWING_CARD_LIMIT = 3;

    private final CrewMemberRepository crewMemberRepository;
    private final CrewHighlightCacheLoader cacheLoader;

    public CrewHighlightInfo getHighlight(Long userId, Long crewId) {
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new NotCrewMemberException();
        }
        return cacheLoader.load(crewId);
    }
}
