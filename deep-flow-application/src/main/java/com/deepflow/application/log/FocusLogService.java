package com.deepflow.application.log;

import com.deepflow.domain.log.FocusLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FocusLogService {

    @Transactional
    public void updateLogDetails(
        FocusLog focusLog,
        String title,
        String content,
        String summary,
        List<String> imageUrls
    ) {
        focusLog.update(title, content, summary, imageUrls);
    }
}
