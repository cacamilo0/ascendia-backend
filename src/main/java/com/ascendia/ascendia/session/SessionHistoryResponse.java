package com.ascendia.ascendia.session;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SessionHistoryResponse {
    private List<SessionSummaryResponse> sessions;
}
