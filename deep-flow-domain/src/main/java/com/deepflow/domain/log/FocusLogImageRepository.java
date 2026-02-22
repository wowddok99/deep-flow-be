package com.deepflow.domain.log;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FocusLogImageRepository extends JpaRepository<FocusLogImage, Long> {
    List<FocusLogImage> findByFocusLogId(Long focusLogId);
}
