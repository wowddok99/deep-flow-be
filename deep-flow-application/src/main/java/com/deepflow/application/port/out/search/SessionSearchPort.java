package com.deepflow.application.port.out.search;

import com.deepflow.application.session.dto.SearchResultInfo;

import java.util.List;

public interface SessionSearchPort {

    /**
     * 크루 내 공유 세션 검색
     *
     * @param crewId 검색 대상 크루
     * @param q      검색어 (호출자가 길이 검증)
     * @param type   "session" (제목+요약+태그) 또는 "tag" (정확 태그 매칭)
     * @param offset 결과 시작 위치 (관련도순 페이징)
     * @param size   페이지 크기
     */
    List<SearchResultInfo> search(Long crewId, String q, SearchType type, int offset, int size);

    enum SearchType { SESSION, TAG }
}
