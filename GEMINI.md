# Output Language
Always respond in Korean (한국어).

# Code Style Guidelines (Strict)

## 0. Project Configuration
- **Forbidden Files**: **DO NOT** create `gradle.properties`. This file is explicitly excluded.

## 1. Indentation & Nesting (The Golden Rule)
- **Unit**: **4 Spaces** (No Tabs).
- **Rule**: Every new scope or wrapped line must be indented **exactly 1 level (+4 spaces)** relative to the parent line.
- **Prohibition**: **DO NOT align** arguments or parameters with the opening parenthesis of the previous line. Always start a new line with a fresh 4-space indent.

## 2. Vertical Formatting (Records, Methods, Constructors)
- **Scope**: Apply to any parameter list with **more than 3 parameters**.
- **Rule**: Place each parameter on a **new line** with 1 level of indentation.
- **Closing**: The closing parenthesis `)` and brace `{` must appear on their own new line, indented to match the start of the declaration.

## 3. Builder Pattern
- **Vertical Chaining**: Always use vertical chaining.
- **Indentation**: Each method call (`.field()`) starts on a new line, indented **1 level** (+4 spaces) from the variable declaration or start of the chain.

## 4. Empty Body
- If the body of a class or record is empty, combine the closing parenthesis and braces: `) {}`.

---

### ✅ Correct Example (DO THIS)

```java
// 1. Normal Record with Builder
@Builder
public record ErrorResponse(
    int status,
    String message,
    LocalDateTime timestamp
) {
    public static ErrorResponse of(int status, String message) {
        return ErrorResponse.builder()
            .status(status)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
}

// 2. Empty Body Record
@Builder
public record LogUpdateRequest(
    String content,
    String summary,
    List<String> tags,
    List<String> imageUrls
) {}

// 3. Method with Vertical Formatting
public void updateLogDetails(
    FocusLog focusLog,
    String content,
    String summary,
    List<String> tagNames,
    List<String> imageUrls
) {
    // Implementation
}
```

### ❌ Forbidden Examples (DO NOT DO THIS)

```java
// ❌ WRONG: Alignment with Parenthesis
public void updateLogDetails(FocusLog focusLog,
                             String content,  // DO NOT ALIGN HERE
                             String summary) {
}

// ❌ WRONG: Deep Indentation in Builder
return ErrorResponse.builder()
                    .status(status)       // DO NOT ALIGN HERE
                    .message(message)
                    .build();

// ❌ WRONG: Parameter on same line as opening parenthesis (if > 3 params)
public void updateLogDetails(FocusLog focusLog,
    String content,
    String summary,
    List<String> tagNames,
    List<String> imageUrls) { // Closing parenthesis on same line is Forbidden
}
```