package com.deepflow.api.dto.session;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReactionToggleRequest(
        @NotBlank @Size(max = 10) String emoji
) {}
