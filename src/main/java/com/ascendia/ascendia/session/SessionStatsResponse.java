package com.ascendia.ascendia.session;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SessionStatsResponse {
    private Map<String, StatEntryResponse> byDifficulty;
    private Map<String, StatEntryResponse> byCategory;
}
