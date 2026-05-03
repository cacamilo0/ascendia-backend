package com.ascendia.ascendia.session;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class SessionSummaryResponse {
    private UUID sessionId;
    private String mode;
    private String area;
    private Integer score;
    private Integer correct;
    private Integer total;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    private Long durationSeconds;
}