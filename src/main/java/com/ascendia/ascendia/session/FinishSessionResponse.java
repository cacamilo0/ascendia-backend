package com.ascendia.ascendia.session;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class FinishSessionResponse {
    private UUID sessionId;
    private boolean finished;
    private OffsetDateTime finishedAt;
    private long durationSeconds;
}