package com.deepflow.core.repository.log;

import com.deepflow.core.domain.log.FocusLogImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FocusLogImageRepository extends JpaRepository<FocusLogImage, Long> {
    List<FocusLogImage> findByFocusLogId(Long focusLogId);
}
