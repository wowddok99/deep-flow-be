package com.deepflow.application.session.share.dto;

import java.util.List;

public record ShareSessionCommand(Long crewId, List<String> tags) {
}
