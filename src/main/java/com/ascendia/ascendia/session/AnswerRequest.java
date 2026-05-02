package com.ascendia.ascendia.session;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerRequest {

    @NotNull
    private Long questionId;
    @NotNull
    private Long selectedOptionId;

}
