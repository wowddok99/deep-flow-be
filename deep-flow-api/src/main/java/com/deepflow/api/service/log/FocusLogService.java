package com.deepflow.api.service.log;

import com.deepflow.core.domain.log.FocusLog;
import com.deepflow.core.domain.log.FocusLogImage;
import com.deepflow.core.domain.log.FocusLogTag;
import com.deepflow.core.domain.tag.Tag;
import com.deepflow.core.repository.log.FocusLogTagRepository;
import com.deepflow.core.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FocusLogService {

    private final TagRepository tagRepository;

    @Transactional
    public void updateLogDetails(
        FocusLog focusLog,
        String content,
        String summary,
        List<String> tagNames,
        List<String> imageUrls
    ) {
        focusLog.update(content, summary);

        updateTags(focusLog, tagNames);
        updateImages(focusLog, imageUrls);
    }

    private void updateTags(FocusLog focusLog, List<String> tagNames) {
        focusLog.getLogTags().clear(); // Or handle existing tags more gracefully

        if (tagNames != null) {
            for (String tagName : tagNames) {
                Tag tag = tagRepository.findByName(tagName)
                .orElseGet(() -> tagRepository.save(
                    Tag.builder()
                        .name(tagName)
                        .build()
                ));

                FocusLogTag logTag = FocusLogTag.builder()
                    .focusLog(focusLog)
                    .tag(tag)
                    .build();

                focusLog.getLogTags().add(logTag);
            }
        }
    }

    private void updateImages(FocusLog focusLog, List<String> imageUrls) {
        focusLog.getImages().clear();

        if (imageUrls != null) {
            for (int i = 0; i < imageUrls.size(); i++) {
                FocusLogImage image = FocusLogImage.builder()
                    .focusLog(focusLog)
                    .imageUrl(imageUrls.get(i))
                    .orderIndex(i)
                    .build();
                focusLog.getImages().add(image);
            }
        }
    }
}
