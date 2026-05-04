package com.deepflow.application.session.dto;

public record MemberSuggestionInfo(
        Long userId,
        String name,
        String username
) {}
