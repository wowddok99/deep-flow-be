package com.deepflow.infra.persistence.crew;

import com.deepflow.application.common.SliceResult;
import com.deepflow.application.port.out.persistence.CrewRepository;
import com.deepflow.domain.crew.Crew;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CrewRepositoryImpl implements CrewRepository {

    private final CrewJpaRepository jpaRepository;

    @Override
    public Crew save(Crew crew) {
        return jpaRepository.save(crew);
    }

    @Override
    public Optional<Crew> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Crew> findByInviteCode(String code) {
        return jpaRepository.findByInviteCode(code);
    }

    @Override
    public SliceResult<Crew> searchPublic(String keyword, Long cursorId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<Crew> slice = (cursorId == null)
                ? jpaRepository.searchPublicAll(keyword, pageable)
                : jpaRepository.searchPublicAfterCursor(keyword, cursorId, pageable);

        List<Crew> content = slice.getContent();
        Long nextCursorId = slice.hasNext() && !content.isEmpty()
                ? content.get(content.size() - 1).getId()
                : null;

        return new SliceResult<>(content, nextCursorId, slice.hasNext());
    }

    @Override
    public boolean existsByIdAndNotDeleted(Long id) {
        return jpaRepository.countById(id) > 0;
    }

    @Override
    public List<Crew> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findAllByIdIn(ids);
    }
}
