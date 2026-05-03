package com.ascendia.ascendia.session;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StartSessionRequest {
    private UUID userId;
    private TestMode mode;
}