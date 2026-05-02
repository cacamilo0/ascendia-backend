package com.ascendia.ascendia.question;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SessionQuestionsResponse {
    private UUID sessionId;
    private String area;
    private List<BlockResponse> blocks;
}
