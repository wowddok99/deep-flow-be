package com.deepflow.infra.persistence.crew;

import com.deepflow.domain.crew.Crew;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface CrewJpaRepository extends JpaRepository<Crew, Long> {

    Optional<Crew> findByInviteCode(String inviteCode);

    @Query("SELECT c FROM Crew c WHERE c.visibility = 'PUBLIC' " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY c.id DESC")
    Slice<Crew> searchPublicAll(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Crew c WHERE c.visibility = 'PUBLIC' " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND c.id < :cursorId ORDER BY c.id DESC")
    Slice<Crew> searchPublicAfterCursor(@Param("keyword") String keyword,
                                        @Param("cursorId") Long cursorId,
                                        Pageable pageable);

    @Query("SELECT COUNT(c) FROM Crew c WHERE c.id = :id")
    long countById(@Param("id") Long id);

    List<Crew> findAllByIdIn(List<Long> ids);
}
