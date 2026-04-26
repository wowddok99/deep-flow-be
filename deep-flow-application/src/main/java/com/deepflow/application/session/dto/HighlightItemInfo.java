package com.deepflow.application.session.dto;

public record HighlightItemInfo(
        Type type,
        Long sessionId,
        String title,
        String userName,
        Double score,
        Long durationSeconds,
        String tag,
        Integer count
) {
    public enum Type { HOT, LONG, TAG, RECENT }

    public static HighlightItemInfo hot(Long sessionId, String title, String userName, double score) {
        return new HighlightItemInfo(Type.HOT, sessionId, title, userName, score, null, null, null);
    }

    public static HighlightItemInfo longSession(Long sessionId, String title, String userName, long durationSeconds) {
        return new HighlightItemInfo(Type.LONG, sessionId, title, userName, null, durationSeconds, null, null);
    }

    public static HighlightItemInfo tag(String tag, int count) {
        return new HighlightItemInfo(Type.TAG, null, null, null, null, null, tag, count);
    }

    public static HighlightItemInfo recent(Long sessionId, String title, String userName) {
        return new HighlightItemInfo(Type.RECENT, sessionId, title, userName, null, null, null, null);
    }
}
