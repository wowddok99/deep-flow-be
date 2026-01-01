package com.deepflow.api.service.log;

import com.deepflow.core.domain.log.FocusLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FocusLogService {



    @Transactional
    public void updateLogDetails(
        FocusLog focusLog,
        String title,
        Map<String, Object> content,
        String summary,
        List<String> imageUrls
    ) {
        focusLog.update(title, content, summary, imageUrls);
    }
}
