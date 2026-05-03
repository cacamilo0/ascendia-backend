package com.ascendia.ascendia.session;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewQuestionResponse {
    private Long questionId;
    private boolean correct;
    private boolean omitted;
    private Long selectedOptionId;
    private Long correctOptionId;
    private String explanation;
}
