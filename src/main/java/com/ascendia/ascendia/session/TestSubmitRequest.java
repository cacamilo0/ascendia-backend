package com.ascendia.ascendia.session;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TestSubmitRequest {

    @NotNull
    private UUID sessionId;

    @NotNull
    @Valid
    @Size(min = 1)
    private List<AnswerRequest> answers;

}
