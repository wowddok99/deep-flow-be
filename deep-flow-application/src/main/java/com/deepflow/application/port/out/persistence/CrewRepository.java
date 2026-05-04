package com.deepflow.application.port.out.persistence;

import com.deepflow.application.common.SliceResult;
import com.deepflow.domain.crew.Crew;

import java.util.List;
import java.util.Optional;

public interface CrewRepository {

    Crew save(Crew crew);

    Optional<Crew> findById(Long id);

    Optional<Crew> findByInviteCode(String code);

    SliceResult<Crew> searchPublic(String keyword, Long cursorId, int size);

    boolean existsByIdAndNotDeleted(Long id);

    List<Crew> findAllByIds(List<Long> ids);
}
