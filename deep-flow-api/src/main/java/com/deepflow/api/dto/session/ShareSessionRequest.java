package com.deepflow.api.dto.session;

import com.deepflow.application.session.share.dto.ShareSessionCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ShareSessionRequest(
        @NotNull @Min(1) Long crewId,
        @Size(max = 5, message = "Tags must be at most 5") List<String> tags
) {
    public ShareSessionCommand toCommand() {
        return new ShareSessionCommand(crewId, tags == null ? List.of() : tags);
    }
}
