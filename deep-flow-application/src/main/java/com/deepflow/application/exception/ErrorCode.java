package com.deepflow.application.exception;

public enum ErrorCode {

    // Auth
    DUPLICATE_USERNAME(409, "Username already exists"),
    INVALID_CREDENTIALS(401, "Invalid username or password"),
    INVALID_TOKEN(401, "Invalid or expired token"),

    // Session
    SESSION_ALREADY_EXISTS(409, "An ongoing session already exists"),
    SESSION_NOT_DELETABLE(409, "Cannot delete an ongoing session"),
    SESSION_NOT_FOUND(404, "Session not found"),

    // Crew shared sessions
    SESSION_ALREADY_SHARED(409, "Session is already shared"),
    SESSION_NOT_SHARED(400, "Session is not shared"),
    SESSION_NOT_SHAREABLE(400, "Session must be COMPLETED with non-empty FocusLog content to be shared"),
    SESSION_NOT_IN_CREW(404, "Session is not shared to this crew"),
    TAG_LIMIT_EXCEEDED(400, "Tags must be at most 5"),

    // Lock
    LOCK_ACQUISITION_FAILED(409, "Another request is being processed"),

    // Crew
    CREW_NOT_FOUND(404, "Crew not found"),
    CREW_ACCESS_DENIED(403, "Only crew owner can perform this action"),
    NOT_CREW_MEMBER(403, "You are not a member of this crew"),
    ALREADY_CREW_MEMBER(409, "You are already a member of this crew"),
    INVALID_INVITE_CODE(400, "Invalid or expired invite code"),
    CREW_MEMBER_LIMIT_EXCEEDED(409, "Crew member limit exceeded"),
    CREW_OWNER_CANNOT_LEAVE(409, "Owner cannot leave the crew. Disband instead."),
    INVALID_INVITE_TTL(400, "Invalid invite TTL value"),
    CREW_MAX_MEMBERS_BELOW_CURRENT(409, "Max members cannot be lower than current member count"),
    CREW_NOT_PUBLIC(403, "This crew is not publicly joinable"),

    // Rate limit
    RATE_LIMIT_EXCEEDED(429, "Too many requests"),

    // Common
    RESOURCE_NOT_FOUND(404, "Resource not found"),
    INTERNAL_ERROR(500, "Internal server error");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
