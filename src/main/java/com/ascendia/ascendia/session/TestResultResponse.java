package com.ascendia.ascendia.session;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestResultResponse {

    private double score;
    private Map<String, ByCategoryResponse> byCategory;
    private List<WeaknessResponse> weaknesses;
    private Map<String, ByCategoryResponse> byDifficulty;

}
