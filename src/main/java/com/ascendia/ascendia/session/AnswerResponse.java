package com.ascendia.ascendia.session;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerResponse {
    private String mode;
    private Boolean saved;
    private Boolean correct;
    private Long correctOptionId;
    private String explanation;
    private String tip;
}