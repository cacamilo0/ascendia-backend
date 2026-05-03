package com.ascendia.ascendia.session;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SessionReviewResponse {
    private UUID sessionId;
    private int totalQuestions;
    private int answered;
    private int omitted;
    private int correct;
    private int incorrect;
    private double score;
    private List<ReviewQuestionResponse> questions;
    private SessionStatsResponse stats;
}
