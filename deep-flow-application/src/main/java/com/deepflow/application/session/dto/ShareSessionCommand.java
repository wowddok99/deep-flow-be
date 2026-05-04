package com.deepflow.application.session.dto;

import java.util.List;

public record ShareSessionCommand(Long crewId, List<String> tags) {
}
