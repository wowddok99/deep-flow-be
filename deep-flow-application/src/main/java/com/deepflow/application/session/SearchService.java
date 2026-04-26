package com.deepflow.application.session;

import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.exception.session.SearchQueryTooShortException;
import com.deepflow.application.exception.session.SearchTypeInvalidException;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.search.SessionSearchPort;
import com.deepflow.application.session.dto.SearchResultInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    public static final int MIN_QUERY_LENGTH = 2;
    public static final int MAX_PAGE_SIZE = 50;

    private final SessionSearchPort searchPort;
    private final TagNormalizer tagNormalizer;
    private final CrewMemberRepository crewMemberRepository;

    public List<SearchResultInfo> search(Long userId, Long crewId, String q, String typeRaw, int offset, int size) {
        if (q == null || q.trim().length() < MIN_QUERY_LENGTH) throw new SearchQueryTooShortException();
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) throw new NotCrewMemberException();

        SessionSearchPort.SearchType type = parseType(typeRaw);
        String query = (type == SessionSearchPort.SearchType.TAG)
                ? tagNormalizer.normalize(q.trim())
                : q.trim();

        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safeOffset = Math.max(offset, 0);

        return searchPort.search(crewId, query, type, safeOffset, pageSize);
    }

    private SessionSearchPort.SearchType parseType(String typeRaw) {
        if (typeRaw == null || typeRaw.isBlank()) return SessionSearchPort.SearchType.SESSION;
        return switch (typeRaw.toLowerCase()) {
            case "session" -> SessionSearchPort.SearchType.SESSION;
            case "tag" -> SessionSearchPort.SearchType.TAG;
            default -> throw new SearchTypeInvalidException();
        };
    }
}
